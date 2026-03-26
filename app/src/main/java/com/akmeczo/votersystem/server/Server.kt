package com.akmeczo.votersystem.server

import android.content.Context
import com.google.gson.Gson
import com.akmeczo.votersystem.server.responses.TokensDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

enum class ResponseType {
    Success,
    NotFound,
    InvalidToken,
    InsufficientPermissions,
    NetworkLost,
    Other
}

enum class RequestType(val value: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE")
}

class Server(domain: String, apiEndpoint: String, context: Context? = null) {
    private var apiPart = if (apiEndpoint.isNotEmpty()) "/$apiEndpoint" else ""
    val queryUrl: String = "https://$domain$apiPart"
    private val cookieJar = CustomCookieJar()
    private val sessionStore: SessionStore? = context?.let(::SessionStore)
    private val refreshMutex = Mutex()
    @Volatile
    private var authToken: String? = sessionStore?.getTokens()?.authToken
    val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    val gson = Gson()

    suspend fun checkServerHealth(): Boolean {
        val result = request("health", RequestType.GET, null)
        return when (result.first) {
            ResponseType.Success -> true
            else -> false
        }
    }

    suspend inline fun tryMakeRequest(urlString: String, type: RequestType, body: String?): Boolean {
        val result = request(urlString, type, body)
        return when(result.first) {
            ResponseType.Success -> { true }
            ResponseType.InvalidToken -> { false }
            ResponseType.InsufficientPermissions -> { false }
            ResponseType.NetworkLost -> { false }
            ResponseType.NotFound -> { false }
            ResponseType.Other -> { false }
        }
    }

    suspend inline fun <reified Type> makeRequest(urlString: String, type: RequestType, body: String?, invokesLogin: Boolean = true): Type? {
        val result = request(urlString, type, body)
        return when(result.first) {
            ResponseType.Success -> {
                if(Type::class.java != String::class.java) {
                    gson.fromJson(result.second, Type::class.java)
                } else {
                    result.second as Type
                }
            }
            ResponseType.InvalidToken -> { null }
            ResponseType.InsufficientPermissions -> { null }
            ResponseType.NetworkLost -> { null }
            ResponseType.NotFound -> { null }
            ResponseType.Other -> { null }
        }
    }

    suspend inline fun <reified RespType, reified ReqType> makeRequest(urlString: String, type: RequestType, body: ReqType?, invokesLogin: Boolean = true): RespType? {
        val result = request(urlString, type, gson.toJson(body))
        return when(result.first) {
            ResponseType.Success -> {
                if(RespType::class.java != String::class.java) {
                    gson.fromJson(result.second, RespType::class.java)
                } else {
                    result.second as RespType
                }
            }
            ResponseType.InvalidToken -> { null }
            ResponseType.InsufficientPermissions -> { null }
            ResponseType.NetworkLost -> { null }
            ResponseType.NotFound -> { null }
            ResponseType.Other -> { null }
        }
    }

    suspend inline fun <reified Type> makeRequest(urlString: String, type: RequestType, body: MultipartBody): Type? {
        val result = request(urlString, type, body)
        return when(result.first) {
            ResponseType.Success -> {
                if(Type::class.java != String::class.java) {
                    gson.fromJson(result.second, Type::class.java)
                } else {
                    result.second as Type
                }
            }
            ResponseType.InvalidToken -> {null }
            ResponseType.InsufficientPermissions -> { null }
            ResponseType.NetworkLost -> { null }
            ResponseType.NotFound -> { null }
            ResponseType.Other -> {  null }
        }
    }

    fun saveTokens(tokens: TokensDto) {
        authToken = tokens.authToken
        sessionStore?.saveTokens(tokens)
    }

    fun getStoredTokens(): TokensDto? = sessionStore?.getTokens()

    fun hasStoredSession(): Boolean = getStoredTokens() != null

    fun clearSession() {
        authToken = null
        sessionStore?.clear()
        cookieJar.clear()
    }

    suspend fun executeApiRequest(
        path: String,
        type: RequestType,
        body: String?,
        includeAuthToken: Boolean = true,
        allowTokenRefresh: Boolean = true
    ): ApiResult<String> {
        val initialToken = if (includeAuthToken) authToken else null
        val initialResult = executeRaw(path, type, body, initialToken)
        if (!allowTokenRefresh || initialResult !is ApiResult.Failure || initialResult.code != 401) {
            return initialResult
        }

        val refreshSucceeded = refreshAuthToken(initialToken)
        if (!refreshSucceeded) {
            return initialResult
        }

        return executeRaw(path, type, body, authToken)
    }

    private suspend fun refreshAuthToken(failedToken: String?): Boolean = refreshMutex.withLock {
        val storedTokens = getStoredTokens() ?: return false
        if (failedToken != null && storedTokens.authToken != failedToken) {
            authToken = storedTokens.authToken
            return true
        }

        val refreshBody = ApiJson.encode(storedTokens.refreshToken)
        val refreshResult = executeRaw(
            path = "users/refresh-token",
            type = RequestType.POST,
            body = refreshBody,
            authToken = null
        )

        return when (refreshResult) {
            is ApiResult.Success -> {
                val refreshedTokens = runCatching {
                    ApiJson.decode<TokensDto>(refreshResult.value)
                }.getOrNull()

                if (refreshedTokens == null) {
                    clearSession()
                    false
                } else {
                    saveTokens(refreshedTokens)
                    true
                }
            }
            is ApiResult.Failure -> {
                clearSession()
                false
            }
        }
    }

    private suspend fun executeRaw(
        path: String,
        type: RequestType,
        body: String?,
        authToken: String?
    ): ApiResult<String> {
        val completeUrl = "$queryUrl/$path"
        val requestBody = body?.toRequestBody("application/json; charset=utf-8".toMediaType())
            ?: if (type == RequestType.PATCH) byteArrayOf().toRequestBody(null, 0, 0) else null

        val requestBuilder = Request.Builder()
            .url(completeUrl)
            .method(type.value, requestBody)

        if (!authToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(requestBuilder.build()).execute().use { response ->
                    val responseBody = response.body.string()
                    if (response.isSuccessful) {
                        ApiResult.Success(responseBody)
                    } else {
                        ApiResult.Failure(response.code, responseBody)
                    }
                }
            } catch (exception: IOException) {
                ApiResult.Failure(-1, exception.message ?: "Network error")
            }
        }
    }

    suspend fun request(urlString: String, type: RequestType, body: String?): Pair<ResponseType, String> {
        val completeUrl = "$queryUrl/$urlString"
        println("$type to $completeUrl with $body")
        if (type == RequestType.GET && body != null) {
            return Pair(ResponseType.Other, "")
        }

        val requestBody = body?.toRequestBody("application/json; charset=utf-8".toMediaType())
            ?: if (type == RequestType.PATCH) byteArrayOf().toRequestBody(
                null,
                0,
                0
            ) else null

        val requestBuilder = Request.Builder()
            .url(completeUrl)
            .method(type.value, requestBody)

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(requestBuilder.build()).execute()
                val responseBody = response.body.string()

                val status = when {
                    response.isSuccessful -> ResponseType.Success
                    response.code == 401 -> ResponseType.InvalidToken
                    response.code == 403 -> ResponseType.InsufficientPermissions
                    response.code == 404 -> ResponseType.NotFound
                    else -> ResponseType.Other
                }

                if(status != ResponseType.Success) {
                    println("Response was ${Pair(status, responseBody)} (code: ${response.code}) for $urlString ($type) with $body")
                }

                Pair(status, responseBody)
            } catch (e: IOException) {
                println("Exception is ${e.message}")
                Pair(ResponseType.NetworkLost, "")
            }
        }
    }

    suspend fun request(urlString: String, type: RequestType, body: MultipartBody): Pair<ResponseType, String> {
        val completeUrl = "$queryUrl/$urlString"
        println("$type request to $completeUrl")

        val requestBuilder = Request.Builder()
            .url(completeUrl)
            .method(type.value, body)

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(requestBuilder.build()).execute()
                val responseBody = response.body.string()

                val status = when {
                    response.isSuccessful -> ResponseType.Success
                    response.code == 401 -> ResponseType.InvalidToken
                    response.code == 403 -> ResponseType.InsufficientPermissions
                    response.code == 404 -> ResponseType.NotFound
                    else -> ResponseType.Other
                }

                if(status != ResponseType.Success) {
                    println("Response was ${Pair(status, responseBody)} (code: ${response.code}) for $urlString ($type) with $body")
                }

                Pair(status, responseBody)
            } catch (e: IOException) {
                println("Exception is ${e.message}")
                Pair(ResponseType.NetworkLost, "")
            }
        }
    }
}

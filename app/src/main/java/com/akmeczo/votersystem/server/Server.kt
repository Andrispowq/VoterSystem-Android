package com.akmeczo.votersystem.server

import android.content.Context
import com.akmeczo.votersystem.server.requests.RefreshTokenRequest
import com.akmeczo.votersystem.server.responses.TokensDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

enum class RequestType(val value: String) {
    GET("GET"),
    POST("POST"),
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
    var authToken: String? = sessionStore?.getTokens()?.authToken
        private set
    val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

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

        val body = RefreshTokenRequest(storedTokens.refreshToken)

        val refreshBody = ApiJson.encode(body)
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
}

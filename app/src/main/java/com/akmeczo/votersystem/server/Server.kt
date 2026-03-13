package com.akmeczo.votersystem.server

import com.google.gson.Gson
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

class Server(domain: String, apiEndpoint: String) {
    private var apiPart = if (apiEndpoint.isNotEmpty()) "/$apiEndpoint" else ""
    val queryUrl: String = "https://$domain$apiPart"
    val client = OkHttpClient.Builder()
        .cookieJar(CustomCookieJar())
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

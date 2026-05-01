package com.akmeczo.votersystem.server

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>

    data class Failure(
        val code: Int,
        val content: String
    ) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> =
    when (this) {
        is ApiResult.Failure -> this
        is ApiResult.Success -> ApiResult.Success(transform(value))
    }

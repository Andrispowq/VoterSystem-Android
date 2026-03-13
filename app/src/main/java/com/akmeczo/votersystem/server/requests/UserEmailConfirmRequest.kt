package com.akmeczo.votersystem.server.requests

data class UserEmailConfirmRequest(
    val email: String,
    val token: String
)

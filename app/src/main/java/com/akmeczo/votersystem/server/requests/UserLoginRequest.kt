package com.akmeczo.votersystem.server.requests

data class UserLoginRequest(
    val email: String,
    val password: String
)

package com.akmeczo.votersystem.server.requests

data class UserRegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

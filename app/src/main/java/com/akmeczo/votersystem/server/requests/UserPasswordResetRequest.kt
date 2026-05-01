package com.akmeczo.votersystem.server.requests

data class UserPasswordResetRequest(
    val email: String,
    val token: String,
    val newPassword: String
)

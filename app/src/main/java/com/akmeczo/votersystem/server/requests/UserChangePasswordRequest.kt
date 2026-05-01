package com.akmeczo.votersystem.server.requests

data class UserChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

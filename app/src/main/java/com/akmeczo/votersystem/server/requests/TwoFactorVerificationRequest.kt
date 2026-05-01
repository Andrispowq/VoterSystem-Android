package com.akmeczo.votersystem.server.requests

import java.util.UUID

data class TwoFactorVerificationRequest(
    val userId: UUID,
    val code: String
)

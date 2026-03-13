package com.akmeczo.votersystem.server.requests

import java.util.UUID

data class TwoFactorVerificationRequest(
    val challengeId: UUID,
    val code: String
)

package com.akmeczo.votersystem.server.responses

import java.util.UUID

data class TwoFactorChallengeDto(
    val userId: UUID,
    val message: String
)

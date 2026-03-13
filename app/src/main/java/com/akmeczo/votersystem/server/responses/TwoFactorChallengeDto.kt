package com.akmeczo.votersystem.server.responses

import java.util.UUID

data class TwoFactorChallengeDto(
    val challengeId: UUID,
    val message: String
)

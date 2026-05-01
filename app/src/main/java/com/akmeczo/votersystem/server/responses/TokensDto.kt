package com.akmeczo.votersystem.server.responses

import java.util.UUID

data class TokensDto(
    val authToken: String,
    val refreshToken: UUID,
    val userId: UUID)
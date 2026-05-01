package com.akmeczo.votersystem.server.responses

import java.time.Instant

data class VoteChoiceDto(
    val choiceId: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant
)

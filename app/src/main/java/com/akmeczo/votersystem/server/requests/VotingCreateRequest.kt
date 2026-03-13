package com.akmeczo.votersystem.server.requests

import java.time.Instant

data class VotingCreateRequest(
    val name: String,
    val startsAt: Instant,
    val endsAt: Instant
)

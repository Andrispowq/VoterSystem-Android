package com.akmeczo.votersystem.server.responses

import java.time.Instant

data class VotingDto(
    val votingId: Long,
    val name: String,
    val createdAt: Instant,
    val startsAt: Instant,
    val endsAt: Instant,
    val hasStarted: Boolean,
    val hasEnded: Boolean,
    val isOngoing: Boolean,
    val hasVoted: Boolean?,
    val voteChoices: List<VoteChoiceDto>
)

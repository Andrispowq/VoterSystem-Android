package com.akmeczo.votersystem.server.responses

import java.time.Instant

data class BallotDto(
    val voteChoice: VoteChoiceDto,
    val voting: VotingDto,
    val createdAt: Instant
)

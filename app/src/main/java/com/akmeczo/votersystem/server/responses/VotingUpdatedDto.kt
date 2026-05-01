package com.akmeczo.votersystem.server.responses

data class VotingUpdatedDto(
    val votingId: Long,
    val votingResults: VotingResultsDto
)

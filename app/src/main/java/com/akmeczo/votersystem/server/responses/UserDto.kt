package com.akmeczo.votersystem.server.responses

import java.util.UUID

data class UserDto(
    val id: UUID,
    val name: String,
    val email: String,
    val emailConfirmed: Boolean,
    val twoFactorEnabled: Boolean,
    val role: Role,
    val participations: List<VotingParticipationDto>
)

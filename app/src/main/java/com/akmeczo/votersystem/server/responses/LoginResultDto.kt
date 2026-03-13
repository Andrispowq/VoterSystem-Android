package com.akmeczo.votersystem.server.responses

sealed interface LoginResultDto {
    data class Tokens(val tokens: TokensDto) : LoginResultDto

    data class TwoFactorChallenge(
        val challenge: TwoFactorChallengeDto
    ) : LoginResultDto
}

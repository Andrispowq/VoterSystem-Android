package com.akmeczo.votersystem.server

import android.os.Build
import androidx.annotation.RequiresApi
import com.akmeczo.votersystem.server.requests.TwoFactorVerificationRequest
import com.akmeczo.votersystem.server.requests.UserChangePasswordRequest
import com.akmeczo.votersystem.server.requests.UserEmailConfirmRequest
import com.akmeczo.votersystem.server.requests.UserLoginRequest
import com.akmeczo.votersystem.server.requests.UserPasswordResetRequest
import com.akmeczo.votersystem.server.requests.UserRegisterRequest
import com.akmeczo.votersystem.server.requests.VoteChoiceRequest
import com.akmeczo.votersystem.server.requests.VotingCreateRequest
import com.akmeczo.votersystem.server.responses.BallotDto
import com.akmeczo.votersystem.server.responses.LoginResultDto
import com.akmeczo.votersystem.server.responses.TokensDto
import com.akmeczo.votersystem.server.responses.UserDto
import com.akmeczo.votersystem.server.responses.VoteChoiceDto
import com.akmeczo.votersystem.server.responses.VoteResultDto
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.server.responses.VotingParticipationDto
import com.akmeczo.votersystem.server.responses.VotingResultsDto
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID

object Api {
    object Health {
        suspend fun check(server: Server): Boolean = server.checkServerHealth()
    }

    object Users {
        suspend fun register(server: Server, request: UserRegisterRequest): UserDto? =
            server.parseBody("users/register", RequestType.POST, ApiJson.encode(request))

        suspend fun login(server: Server, request: UserLoginRequest): LoginResultDto? {
            val response = server.request("users/login", RequestType.POST, ApiJson.encode(request))
            if (response.first != ResponseType.Success || response.second.isBlank()) {
                return null
            }

            return when {
                "\"authToken\"" in response.second -> LoginResultDto.Tokens(ApiJson.decode<TokensDto>(response.second))
                "\"challengeId\"" in response.second -> LoginResultDto.TwoFactorChallenge(
                    ApiJson.decode(response.second)
                )
                else -> null
            }
        }

        suspend fun loginTwoFactor(
            server: Server,
            request: TwoFactorVerificationRequest
        ): TokensDto? = server.parseBody("users/login/2fa", RequestType.POST, ApiJson.encode(request))

        suspend fun getAll(server: Server): List<UserDto>? =
            server.parseBody("users/all", RequestType.GET, null)

        suspend fun getCurrent(server: Server): UserDto? =
            server.parseBody("users", RequestType.GET, null)

        suspend fun getById(server: Server, id: UUID): UserDto? =
            server.parseBody("users/$id", RequestType.GET, null)

        suspend fun changePassword(server: Server, request: UserChangePasswordRequest): Boolean =
            server.succeeds("users/change-password", RequestType.PUT, ApiJson.encode(request))

        suspend fun enableTwoFactor(server: Server): Boolean =
            server.succeeds("users/two-factor/enable", RequestType.PATCH, null)

        suspend fun requestEmailConfirmation(server: Server): Boolean =
            server.succeeds("users/confirm-email-request", RequestType.POST, null)

        suspend fun confirmEmail(server: Server, request: UserEmailConfirmRequest): Boolean =
            server.succeeds("users/confirm-email", RequestType.POST, ApiJson.encode(request))

        suspend fun requestPasswordReset(server: Server, email: String): Boolean =
            server.succeeds("users/reset-password-request", RequestType.POST, ApiJson.encode(email))

        suspend fun resetPassword(server: Server, request: UserPasswordResetRequest): Boolean =
            server.succeeds("users/reset-password", RequestType.POST, ApiJson.encode(request))

        suspend fun logout(server: Server): Boolean =
            server.succeeds("users/logout", RequestType.DELETE, null)

        suspend fun promote(server: Server, userId: UUID): Boolean =
            server.succeeds("users/promote?userId=${userId}", RequestType.PATCH, null)

        suspend fun demote(server: Server, userId: UUID): Boolean =
            server.succeeds("users/demote?userId=${userId}", RequestType.PATCH, null)

        suspend fun refreshToken(server: Server, refreshToken: UUID): TokensDto? =
            server.parseBody("users/refresh-token", RequestType.POST, ApiJson.encode(refreshToken))
    }

    object Votings {
        suspend fun getAll(server: Server): List<VotingDto>? =
            server.parseBody("votings", RequestType.GET, null)

        suspend fun getVotable(server: Server): List<VotingDto>? =
            server.parseBody("votings/votable", RequestType.GET, null)

        suspend fun getVoted(server: Server): List<VotingDto>? =
            server.parseBody("votings/voted", RequestType.GET, null)

        suspend fun getById(server: Server, votingId: Long): VotingDto? =
            server.parseBody("votings/$votingId", RequestType.GET, null)

        suspend fun getResults(server: Server, votingId: Long): VotingResultsDto? =
            server.parseBody("votings/$votingId/results", RequestType.GET, null)

        suspend fun create(server: Server, request: VotingCreateRequest): VotingDto? =
            server.parseBody("votings", RequestType.POST, ApiJson.encode(request))

        suspend fun updateStartsAt(server: Server, votingId: Long, startsAt: Instant): VotingDto? =
            server.parseBody("votings/$votingId/starts-at", RequestType.PATCH, ApiJson.encode(startsAt))

        suspend fun updateEndsAt(server: Server, votingId: Long, endsAt: Instant): VotingDto? =
            server.parseBody("votings/$votingId/ends-at", RequestType.PATCH, ApiJson.encode(endsAt))

        suspend fun start(server: Server, votingId: Long): VotingDto? =
            server.parseBody("votings/$votingId/start", RequestType.POST, null)

        suspend fun delete(server: Server, votingId: Long): Boolean =
            server.succeeds("votings/$votingId", RequestType.DELETE, null)

        suspend fun getVotes(server: Server, votingId: Long): List<BallotDto>? =
            server.parseBody("votings/$votingId/votes", RequestType.GET, null)
    }

    object Choices {
        suspend fun getAll(server: Server, votingId: Long): List<VoteChoiceDto>? =
            server.parseBody("votings/$votingId/choices", RequestType.GET, null)

        suspend fun getById(server: Server, votingId: Long, choiceId: Long): VoteChoiceDto? =
            server.parseBody("votings/$votingId/choices/$choiceId", RequestType.GET, null)

        suspend fun create(
            server: Server,
            votingId: Long,
            request: VoteChoiceRequest
        ): VoteChoiceDto? =
            server.parseBody("votings/$votingId/choices", RequestType.POST, ApiJson.encode(request))

        suspend fun delete(server: Server, votingId: Long, choiceId: Long): Boolean =
            server.succeeds("votings/$votingId/choices/$choiceId", RequestType.DELETE, null)
    }

    object Votes {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        suspend fun castVote(server: Server, choiceId: Long): VoteResultDto? =
            server.parseBody("votes/cast-vote?choiceId=${choiceId}", RequestType.POST, null)

        suspend fun getParticipations(server: Server): List<VotingParticipationDto>? =
            server.parseBody("votes", RequestType.GET, null)
    }

    private suspend inline fun <reified T> Server.parseBody(
        path: String,
        requestType: RequestType,
        body: String?
    ): T? {
        val response = request(path, requestType, body)
        return if (response.first == ResponseType.Success && response.second.isNotBlank()) {
            ApiJson.decode(response.second)
        } else {
            null
        }
    }

    private suspend fun Server.succeeds(path: String, requestType: RequestType, body: String?): Boolean =
        request(path, requestType, body).first == ResponseType.Success
}

package com.akmeczo.votersystem.server

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
import com.akmeczo.votersystem.server.responses.TwoFactorChallengeDto
import com.akmeczo.votersystem.server.responses.UserDto
import com.akmeczo.votersystem.server.responses.VoteChoiceDto
import com.akmeczo.votersystem.server.responses.VoteResultDto
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.server.responses.VotingParticipationDto
import com.akmeczo.votersystem.server.responses.VotingResultsDto
import java.time.Instant
import java.util.UUID

object Api {
    object Health {
        suspend fun check(server: Server): ApiResult<Unit> =
            server.executeForUnit("health", RequestType.GET, null)
    }

    object Users {
        fun externalLoginEndpoint(server: Server, provider: ExternalLoginProvider): String {
            return "${server.queryUrl}/users/external-login/${provider.serverName}?frontend=mobile"
        }

        suspend fun requestSigninTokens(server: Server, key: UUID): ApiResult<TokensDto> =
            server.executeForBody("users/request-signin-tokens?id=$key", RequestType.POST, "")

        suspend fun register(server: Server, request: UserRegisterRequest): ApiResult<UserDto> =
            server.executeForBody("users/register", RequestType.POST, ApiJson.encode(request))

        suspend fun login(server: Server, request: UserLoginRequest): ApiResult<LoginResultDto> {
            return when (val result = server.execute("users/login", RequestType.POST, ApiJson.encode(request))) {
                is ApiResult.Failure -> result
                is ApiResult.Success -> {
                    val body = result.value
                    when {
                        body.isBlank() -> ApiResult.Failure(-2, "Empty response body")
                        "\"authToken\"" in body -> server.decodeBody(body) { ApiJson.decode<TokensDto>(body) }
                            .map { LoginResultDto.Tokens(it) }
                        "\"userId\"" in body && "\"message\"" in body ->
                            server.decodeBody(body) { ApiJson.decode<TwoFactorChallengeDto>(body) }
                            .map { LoginResultDto.TwoFactorChallenge(it) }
                        else -> ApiResult.Failure(-2, body)
                    }
                }
            }
        }

        suspend fun loginTwoFactor(
            server: Server,
            request: TwoFactorVerificationRequest
        ): ApiResult<TokensDto> =
            server.executeForBody("users/login/2fa", RequestType.POST, ApiJson.encode(request))

        suspend fun getAll(server: Server): ApiResult<List<UserDto>> =
            server.executeForBody("users/all", RequestType.GET, null)

        suspend fun getCurrent(server: Server): ApiResult<UserDto> =
            server.executeForBody("users", RequestType.GET, null)

        suspend fun getById(server: Server, id: UUID): ApiResult<UserDto> =
            server.executeForBody("users/$id", RequestType.GET, null)

        suspend fun changePassword(server: Server, request: UserChangePasswordRequest): ApiResult<Unit> =
            server.executeForUnit("users/change-password", RequestType.PUT, ApiJson.encode(request))

        suspend fun enableTwoFactor(server: Server): ApiResult<Unit> =
            server.executeForUnit("users/two-factor/enable", RequestType.PATCH, null)

        suspend fun requestEmailConfirmation(server: Server): ApiResult<Unit> =
            server.executeForUnit("users/confirm-email-request", RequestType.POST, null)

        suspend fun confirmEmail(server: Server, request: UserEmailConfirmRequest): ApiResult<Unit> =
            server.executeForUnit("users/confirm-email", RequestType.POST, ApiJson.encode(request))

        suspend fun requestPasswordReset(server: Server, email: String): ApiResult<Unit> =
            server.executeForUnit("users/reset-password-request", RequestType.POST, ApiJson.encode(email))

        suspend fun resetPassword(server: Server, request: UserPasswordResetRequest): ApiResult<Unit> =
            server.executeForUnit("users/reset-password", RequestType.POST, ApiJson.encode(request))

        suspend fun logout(server: Server): ApiResult<Unit> =
            server.executeForUnit("users/logout", RequestType.DELETE, null)

        suspend fun promote(server: Server, userId: UUID): ApiResult<Unit> =
            server.executeForUnit("users/promote?userId=$userId", RequestType.PATCH, null)

        suspend fun demote(server: Server, userId: UUID): ApiResult<Unit> =
            server.executeForUnit("users/demote?userId=$userId", RequestType.PATCH, null)

        suspend fun refreshToken(server: Server, refreshToken: UUID): ApiResult<TokensDto> =
            server.executeForBody("users/refresh-token", RequestType.POST, ApiJson.encode(refreshToken))
    }

    object Votings {
        suspend fun getAll(server: Server): ApiResult<List<VotingDto>> =
            server.executeForBody("votings", RequestType.GET, null)

        suspend fun getVotable(server: Server): ApiResult<List<VotingDto>> =
            server.executeForBody("votings/votable", RequestType.GET, null)

        suspend fun getVoted(server: Server): ApiResult<List<VotingDto>> =
            server.executeForBody("votings/voted", RequestType.GET, null)

        suspend fun getById(server: Server, votingId: Long): ApiResult<VotingDto> =
            server.executeForBody("votings/$votingId", RequestType.GET, null)

        suspend fun getResults(server: Server, votingId: Long): ApiResult<VotingResultsDto> =
            server.executeForBody("votings/$votingId/results", RequestType.GET, null)

        suspend fun create(server: Server, request: VotingCreateRequest): ApiResult<VotingDto> =
            server.executeForBody("votings", RequestType.POST, ApiJson.encode(request))

        suspend fun updateStartsAt(server: Server, votingId: Long, startsAt: Instant): ApiResult<VotingDto> =
            server.executeForBody("votings/$votingId/starts-at", RequestType.PATCH, ApiJson.encode(startsAt))

        suspend fun updateEndsAt(server: Server, votingId: Long, endsAt: Instant): ApiResult<VotingDto> =
            server.executeForBody("votings/$votingId/ends-at", RequestType.PATCH, ApiJson.encode(endsAt))

        suspend fun start(server: Server, votingId: Long): ApiResult<VotingDto> =
            server.executeForBody("votings/$votingId/start", RequestType.POST, null)

        suspend fun delete(server: Server, votingId: Long): ApiResult<Unit> =
            server.executeForUnit("votings/$votingId", RequestType.DELETE, null)

        suspend fun getVotes(server: Server, votingId: Long): ApiResult<List<BallotDto>> =
            server.executeForBody("votings/$votingId/votes", RequestType.GET, null)
    }

    object Choices {
        suspend fun getAll(server: Server, votingId: Long): ApiResult<List<VoteChoiceDto>> =
            server.executeForBody("votings/$votingId/choices", RequestType.GET, null)

        suspend fun getById(server: Server, votingId: Long, choiceId: Long): ApiResult<VoteChoiceDto> =
            server.executeForBody("votings/$votingId/choices/$choiceId", RequestType.GET, null)

        suspend fun create(
            server: Server,
            votingId: Long,
            request: VoteChoiceRequest
        ): ApiResult<VoteChoiceDto> =
            server.executeForBody("votings/$votingId/choices", RequestType.POST, ApiJson.encode(request))

        suspend fun delete(server: Server, votingId: Long, choiceId: Long): ApiResult<Unit> =
            server.executeForUnit("votings/$votingId/choices/$choiceId", RequestType.DELETE, null)
    }

    object Votes {
        suspend fun castVote(server: Server, choiceId: Long): ApiResult<VoteResultDto> =
            server.executeForBody("votes/cast-vote?choiceId=$choiceId", RequestType.POST, "")

        suspend fun getParticipations(server: Server): ApiResult<List<VotingParticipationDto>> =
            server.executeForBody("votes", RequestType.GET, null)
    }

    private suspend fun Server.executeForUnit(
        path: String,
        requestType: RequestType,
        body: String?
    ): ApiResult<Unit> {
        return when (val result = execute(path, requestType, body)) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    private suspend inline fun <reified T> Server.executeForBody(
        path: String,
        requestType: RequestType,
        body: String?
    ): ApiResult<T> {
        return when (val result = execute(path, requestType, body)) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> decodeBody(result.value) { ApiJson.decode<T>(result.value) }
        }
    }

    private inline fun <T> Server.decodeBody(
        body: String,
        decode: () -> T
    ): ApiResult<T> {
        return try {
            ApiResult.Success(decode())
        } catch (_: Exception) {
            ApiResult.Failure(-2, body)
        }
    }

    private suspend fun Server.execute(
        path: String,
        requestType: RequestType,
        body: String?
    ): ApiResult<String> {
        if (requestType == RequestType.GET && body != null) {
            return ApiResult.Failure(-1, "GET requests cannot have a body")
        }

        val usesSession = path !in publicPaths
        return executeApiRequest(
            path = path,
            type = requestType,
            body = body,
            includeAuthToken = usesSession,
            allowTokenRefresh = usesSession
        )
    }

    private val publicPaths = setOf(
        "health",
        "users/login",
        "users/login/2fa",
        "users/register",
        "users/refresh-token",
        "users/reset-password-request",
        "users/reset-password"
    )
}

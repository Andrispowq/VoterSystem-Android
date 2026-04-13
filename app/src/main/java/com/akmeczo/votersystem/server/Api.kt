package com.akmeczo.votersystem.server

import com.akmeczo.votersystem.server.requests.TwoFactorVerificationRequest
import com.akmeczo.votersystem.server.requests.UserLoginRequest
import com.akmeczo.votersystem.server.requests.UserRegisterRequest
import com.akmeczo.votersystem.server.responses.LoginResultDto
import com.akmeczo.votersystem.server.responses.TokensDto
import com.akmeczo.votersystem.server.responses.TwoFactorChallengeDto
import com.akmeczo.votersystem.server.responses.UserDto
import com.akmeczo.votersystem.server.responses.VoteResultDto
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.server.responses.VotingResultsDto
import java.util.UUID

object Api {
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
                        "\"authToken\"" in body -> decodeBody(body) { ApiJson.decode<TokensDto>(body) }
                            .map { LoginResultDto.Tokens(it) }
                        "\"userId\"" in body && "\"message\"" in body ->
                            decodeBody(body) { ApiJson.decode<TwoFactorChallengeDto>(body) }
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

        suspend fun getCurrent(server: Server): ApiResult<UserDto> =
            server.executeForBody("users", RequestType.GET, null)

        suspend fun logout(server: Server): ApiResult<Unit> =
            server.executeForUnit("users/logout", RequestType.DELETE, null)
    }

    object Votings {
        suspend fun getVotable(server: Server): ApiResult<List<VotingDto>> =
            server.executeForBody("votings/votable", RequestType.GET, null)

        suspend fun getVoted(server: Server): ApiResult<List<VotingDto>> =
            server.executeForBody("votings/voted", RequestType.GET, null)

        suspend fun getById(server: Server, votingId: Long): ApiResult<VotingDto> =
            server.executeForBody("votings/$votingId", RequestType.GET, null)

        suspend fun getResults(server: Server, votingId: Long): ApiResult<VotingResultsDto> =
            server.executeForBody("votings/$votingId/results", RequestType.GET, null)
    }

    object Votes {
        suspend fun castVote(server: Server, choiceId: Long): ApiResult<VoteResultDto> =
            server.executeForBody("votes/cast-vote?choiceId=$choiceId", RequestType.POST, "")
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

    private inline fun <T> decodeBody(
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

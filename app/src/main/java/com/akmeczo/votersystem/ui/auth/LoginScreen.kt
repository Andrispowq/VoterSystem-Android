package com.akmeczo.votersystem.ui.auth

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.requests.TwoFactorVerificationRequest
import com.akmeczo.votersystem.server.requests.UserLoginRequest
import com.akmeczo.votersystem.server.responses.LoginResultDto
import com.akmeczo.votersystem.server.responses.TokensDto
import com.akmeczo.votersystem.server.responses.TwoFactorChallengeDto
import com.akmeczo.votersystem.ui.AppTitleText
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.RoundedActionButton
import com.akmeczo.votersystem.ui.RoundedPasswordField
import com.akmeczo.votersystem.ui.RoundedTextField
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import kotlinx.coroutines.launch

@PreviewScreenSizes
@Composable
fun LoginScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.LoginForm)
) {
    var email by remember { mutableStateOf("example@gmail.com") }
    var password by remember { mutableStateOf("test_Str0ng_password") }
    var twoFactorChallenge by remember { mutableStateOf<TwoFactorChallengeDto?>(null) }
    var twoFactorCode by remember { mutableStateOf("") }
    val isValidEmail = EMAIL_ADDRESS.matcher(email).matches()
    val scope = rememberCoroutineScope()

    fun completeLogin(tokens: TokensDto) {
        server.saveTokens(tokens)
        navigator.navigateTo(AppScreen.VotingList)
    }

    AuthScreenLayout {
        AppTitleText()
        Spacer(modifier = Modifier.height(UiTokens.heroGap))
        RoundedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            placeholder = "Email",
            isError = email.isNotEmpty() && !isValidEmail
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedPasswordField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password"
        )
        Spacer(modifier = Modifier.height(UiTokens.largeGap))
        RoundedActionButton(
            text = "Login",
            onClick = {
                if (email.isBlank() || password.isBlank() || !isValidEmail) {
                    navigator.showError(
                        title = "Invalid login",
                        description = "Fill every field and make sure the email is valid."
                    )
                    return@RoundedActionButton
                }

                val request = UserLoginRequest(email = email, password = password)

                scope.launch {
                    when (val response = Api.Users.login(server, request)) {
                        is ApiResult.Success -> {
                            when (response.value) {
                                is LoginResultDto.Tokens -> completeLogin(response.value.tokens)
                                is LoginResultDto.TwoFactorChallenge -> {
                                    twoFactorChallenge = response.value.challenge
                                    twoFactorCode = ""
                                }
                            }
                        }
                        is ApiResult.Failure -> {
                            navigator.showError(
                                title = "Login Failed",
                                description = "The server returned an error while logging in. (Error code: ${response.code}, details: ${response.content})"
                            )
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Back",
            onClick = {
                navigator.navigateTo(AppScreen.AuthLanding)
            }
        )
    }

    twoFactorChallenge?.let { challenge ->
        TwoFactorPopup(
            message = challenge.message,
            code = twoFactorCode,
            onCodeChange = { twoFactorCode = it },
            onDismiss = {
                twoFactorChallenge = null
                twoFactorCode = ""
            },
            onConfirm = {
                if (twoFactorCode.isBlank()) {
                    navigator.showError(
                        title = "Invalid two-factor code",
                        description = "Enter the verification code to continue."
                    )
                    return@TwoFactorPopup
                }

                scope.launch {
                    when (
                        val response = Api.Users.loginTwoFactor(
                            server,
                            TwoFactorVerificationRequest(
                                userId = challenge.userId,
                                code = twoFactorCode
                            )
                        )
                    ) {
                        is ApiResult.Success -> {
                            twoFactorChallenge = null
                            twoFactorCode = ""
                            completeLogin(response.value)
                        }
                        is ApiResult.Failure -> {
                            navigator.showError(
                                title = "Two-factor login failed",
                                description = "The server rejected the verification code. (Error code: ${response.code}, details: ${response.content})"
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun TwoFactorPopup(
    message: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppTitleText() },
        text = {
            Column {
                BodyText(text = message)
                Spacer(modifier = Modifier.height(UiTokens.sectionGap))
                RoundedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    placeholder = "Verification code"
                )
            }
        },
        confirmButton = {
            RoundedActionButton(
                text = "Verify",
                onClick = onConfirm,
                width = 96.dp
            )
        },
        dismissButton = {
            RoundedActionButton(
                text = "Cancel",
                onClick = onDismiss,
                width = 96.dp
            )
        }
    )
}

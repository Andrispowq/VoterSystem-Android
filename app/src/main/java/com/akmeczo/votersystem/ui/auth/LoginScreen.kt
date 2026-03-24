package com.akmeczo.votersystem.ui.auth

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.requests.UserLoginRequest
import com.akmeczo.votersystem.server.responses.LoginResultDto
import com.akmeczo.votersystem.ui.AppTitleText
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
    val isValidEmail = EMAIL_ADDRESS.matcher(email).matches()
    val scope = rememberCoroutineScope()

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
                                is LoginResultDto.Tokens -> navigator.navigateTo(AppScreen.VotingList)
                                is LoginResultDto.TwoFactorChallenge -> {
                                    println("Returned two factor challenge: ${response.value}")
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
}
package com.akmeczo.votersystem.ui.auth

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
import com.akmeczo.votersystem.server.requests.UserRegisterRequest
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
fun RegisterScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.RegisterForm)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AuthScreenLayout {
        AppTitleText()
        Spacer(modifier = Modifier.height(UiTokens.titleToFormGap))
        RoundedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            placeholder = "Email"
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedPasswordField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password"
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedPasswordField(
            value = passwordAgain,
            onValueChange = { passwordAgain = it },
            placeholder = "Password again"
        )
        Spacer(modifier = Modifier.height(UiTokens.largeGap))
        RoundedActionButton(
            text = "Register",
            onClick = {
                if (!email.isNotBlank() || !password.isNotBlank() || password != passwordAgain) {
                    navigator.showError(
                        title = "Invalid Registration",
                        description = "Fill every field and make sure the two passwords match."
                    )
                    return@RoundedActionButton
                }

                val request = UserRegisterRequest(email = email,
                    name = email, password = password)

                scope.launch {
                    when (val response = Api.Users.register(server, request)) {
                        is ApiResult.Success -> navigator.navigateTo(AppScreen.LoginForm)
                        is ApiResult.Failure -> {
                            navigator.showError(
                                title = "Registration Failed",
                                description = "The server returned an error while creating the account. (Error code: ${response.code}, details: ${response.content})"
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

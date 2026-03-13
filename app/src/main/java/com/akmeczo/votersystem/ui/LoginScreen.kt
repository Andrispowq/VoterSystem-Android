package com.akmeczo.votersystem.ui

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Server

@PreviewScreenSizes
@Composable
fun AuthLandingScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.AuthLanding)
) {
    AuthScreenLayout {
        Spacer(modifier = Modifier.height(UiTokens.screenVerticalPadding))
        AppTitleText()
        Spacer(modifier = Modifier.height(UiTokens.heroGap))
        RoundedActionButton(
            text = "Login",
            onClick = { navigator.navigateTo(AppScreen.LoginForm) }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Register",
            onClick = { navigator.navigateTo(AppScreen.RegisterForm) }
        )
        Spacer(modifier = Modifier.height(28.dp))
        RoundedActionButton(
            text = "Log in with Google",
            onClick = { navigator.navigateTo(AppScreen.VotingList) }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Log in with Neptun",
            onClick = { navigator.navigateTo(AppScreen.VotingList) }
        )
    }
}

@PreviewScreenSizes
@Composable
fun LoginScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.LoginForm)
) {
    var email by remember { mutableStateOf("example@gmail.com") }
    var password by remember { mutableStateOf("test_Str0ng_password") }
    val isValidEmail = EMAIL_ADDRESS.matcher(email).matches()

    AuthScreenLayout {
        Spacer(modifier = Modifier.height(UiTokens.screenVerticalPadding))
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
                if (email.isNotBlank() && password.isNotBlank() && isValidEmail) {
                    navigator.navigateTo(AppScreen.VotingList)
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

@PreviewScreenSizes
@Composable
fun RegisterScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.RegisterForm)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }

    AuthScreenLayout {
        Spacer(modifier = Modifier.height(UiTokens.screenVerticalPadding))
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
                if (email.isNotBlank() && password.isNotBlank() && password == passwordAgain) {
                    navigator.navigateTo(AppScreen.LoginForm)
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

@Composable
private fun AuthScreenLayout(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
            .padding(
                horizontal = UiTokens.screenHorizontalPadding,
                vertical = UiTokens.screenVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        content = content
    )
}

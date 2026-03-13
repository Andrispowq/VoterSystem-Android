package com.akmeczo.votersystem.ui.auth

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
import com.akmeczo.votersystem.ui.AppTitleText
import com.akmeczo.votersystem.ui.RoundedActionButton
import com.akmeczo.votersystem.ui.RoundedPasswordField
import com.akmeczo.votersystem.ui.RoundedTextField
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.appBackground
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen

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
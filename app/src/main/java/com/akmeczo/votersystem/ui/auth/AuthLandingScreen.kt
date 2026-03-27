package com.akmeczo.votersystem.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.ui.AppTitleText
import com.akmeczo.votersystem.ui.RoundedActionButton
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen

@PreviewScreenSizes
@Composable
fun AuthLandingScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.AuthLanding)
) {
    LaunchedEffect(Unit) {
        if (!server.hasStoredSession()) {
            return@LaunchedEffect
        }

        when (Api.Users.getCurrent(server)) {
            is ApiResult.Success -> navigator.navigateTo(AppScreen.VotingList)
            is ApiResult.Failure -> {}
        }
    }

    AuthScreenLayout {
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
            onClick = {
                navigator.navigateTo(AppScreen.VotingList)
            }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Log in with Neptun",
            onClick = {
                navigator.navigateTo(AppScreen.VotingList)
            }
        )
    }
}

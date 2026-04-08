package com.akmeczo.votersystem.ui.auth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.ExternalLoginProvider
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.ui.AppTitleText
import com.akmeczo.votersystem.ui.RoundedActionButton
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import androidx.core.net.toUri

@PreviewScreenSizes
@Composable
fun AuthLandingScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.AuthLanding)
) {
    val context = LocalContext.current

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
                openExternalLogin(context, navigator,
                    server, ExternalLoginProvider.Google)
            }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Log in with Facebook",
            onClick = {
                openExternalLogin(context, navigator,
                    server, ExternalLoginProvider.Facebook)
            }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Log in with Neptun",
            onClick = {
                openExternalLogin(context, navigator,
                    server, ExternalLoginProvider.Neptun)
            }
        )
    }
}

fun openExternalLogin(context: Context, navigator: AppNavigator, server: Server, provider: ExternalLoginProvider) {
    val authUrl = Api.Users.externalLoginEndpoint(server, provider)

    val intent = Intent(Intent.ACTION_VIEW, authUrl.toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        navigator.showError(
            title = "Failed to open browser",
            description = "No default browser found to handle the external login provider's request. Message: ${e.message}."
        )
    }
}

package com.akmeczo.votersystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.lifecycleScope
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.Role
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.ErrorPopup
import com.akmeczo.votersystem.ui.auth.AuthLandingScreen
import com.akmeczo.votersystem.ui.auth.LoginScreen
import com.akmeczo.votersystem.ui.auth.RegisterScreen
import com.akmeczo.votersystem.ui.main.VotingDetailScreen
import com.akmeczo.votersystem.ui.main.VotingHistoryScreen
import com.akmeczo.votersystem.ui.main.VotingListScreen
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val deepLinkScheme = "com.akmeczo.votersystem"
    private val deepLinkHost = "signin-callback"
    private val deepLinkTag = "AuthCallback"

    private lateinit var server: Server
    private val navigator = AppNavigator(AppScreen.AuthLanding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        server = Server("andris.picidolgok.hu", "api/v1", applicationContext)
        enableEdgeToEdge()
        setContent {
            VoterSystemApp(server = server, navigator = navigator)
        }
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        setIntent(Intent(intent).apply { data = null })
        Log.d(deepLinkTag, "Received URI = $uri")

        if (uri.scheme == deepLinkScheme && uri.host == deepLinkHost) {
            val key = uri.getQueryParameter("key")
            val message = uri.getQueryParameter("message")
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")

            Log.d(
                deepLinkTag,
                "Matched sign-in callback: code=$code error=$error key=$key message=$message"
            )

            if (!error.isNullOrBlank()) {
                navigator.showError(
                    title = "External sign-in failed",
                    description = message ?: error
                )
                return
            }

            val signinKey = runCatching { key?.let(UUID::fromString) }.getOrNull()
            if (signinKey == null) {
                navigator.showError(
                    title = "External sign-in failed",
                    description = "The callback did not include a valid sign-in key."
                )
                return
            }

            lifecycleScope.launch {
                when (val response = Api.Users.requestSigninTokens(server, signinKey)) {
                    is ApiResult.Success -> {
                        val user = Api.Users.getCurrent(server)
                        Log.d("MainActivity", "User is $user")
                        if (user is ApiResult.Success && user.value.role == Role.Admin) {
                            navigator.navigateTo(AppScreen.AuthLanding)
                            navigator.showError(
                                title = "Invalid Login",
                                description = "Your account is an Admin account, so you are not allowed to use the mobile version. Please use the Admin website instead."
                            )
                            return@launch
                        }

                        server.saveTokens(response.value)
                        navigator.navigateTo(AppScreen.VotingList)
                    }
                    is ApiResult.Failure -> {
                        navigator.showError(
                            title = "External sign-in failed",
                            description = "The server could not complete the sign-in. (Error code: ${response.code}, details: ${response.content})"
                        )
                    }
                }
            }

        } else {
            Log.w(
                deepLinkTag,
                "Ignored URI because it did not match expected callback: expected $deepLinkScheme://$deepLinkHost"
            )
        }
    }
}

@PreviewScreenSizes
@Composable
fun VoterSystemApp(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.AuthLanding)
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val screen = navigator.currentScreen) {
            AppScreen.AuthLanding -> AuthLandingScreen(server = server, navigator = navigator)
            AppScreen.LoginForm -> LoginScreen(server = server, navigator = navigator)
            AppScreen.RegisterForm -> RegisterScreen(server = server, navigator = navigator)
            AppScreen.VotingList -> VotingListScreen(server = server, navigator = navigator)
            is AppScreen.VotingDetail -> VotingDetailScreen(
                votingId = screen.votingId,
                server = server,
                navigator = navigator
            )
            AppScreen.VotingHistory -> VotingHistoryScreen(server = server, navigator = navigator)
        }

        navigator.errorPopup?.let { error ->
            ErrorPopup(
                title = error.title,
                description = error.description,
                onDismiss = navigator::dismissError
            )
        }
    }
}

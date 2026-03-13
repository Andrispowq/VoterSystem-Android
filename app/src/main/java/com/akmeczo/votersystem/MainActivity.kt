package com.akmeczo.votersystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.ui.AppScreen
import com.akmeczo.votersystem.ui.AuthLandingScreen
import com.akmeczo.votersystem.ui.LoginScreen
import com.akmeczo.votersystem.ui.RegisterScreen
import com.akmeczo.votersystem.ui.VotingDetailScreen
import com.akmeczo.votersystem.ui.VotingHistoryScreen
import com.akmeczo.votersystem.ui.VotingListScreen
import com.akmeczo.votersystem.ui.rememberAppNavigator
import com.akmeczo.votersystem.server.Server

class MainActivity : ComponentActivity() {
    private val server: Server = Server("andris.picidolgok.hu", "api/v1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoterSystemApp(server)
        }
    }
}

@PreviewScreenSizes
@Composable
fun VoterSystemApp(server: Server = Server("", "")) {
    val navigator = rememberAppNavigator()

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
    }
}

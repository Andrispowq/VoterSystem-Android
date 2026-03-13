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
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.ui.LoginScreen

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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoginScreen(server)
    }
}

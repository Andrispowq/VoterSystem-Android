package com.akmeczo.votersystem.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.UserDto


@PreviewScreenSizes
@Composable
fun MainScreen(server: Server = Server("", ""),
               navigator: AppNavigator = AppNavigator(initialScreen = AppScreen.Main)
) {
    var user by remember { mutableStateOf(null as UserDto?) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        user = Api.Users.getCurrent(server)
    }

    Column {
        Text(
            text = "Voter System",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 100.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = user?.name ?: "No user",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 100.dp),
        )
    }
}
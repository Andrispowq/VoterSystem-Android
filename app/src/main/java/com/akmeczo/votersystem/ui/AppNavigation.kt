package com.akmeczo.votersystem.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed interface AppScreen {
    data object AuthLanding : AppScreen
    data object LoginForm : AppScreen
    data object RegisterForm : AppScreen
    data object VotingList : AppScreen
    data class VotingDetail(val votingId: Long) : AppScreen
    data object VotingHistory : AppScreen
}

class AppNavigator(initialScreen: AppScreen) {
    var currentScreen by mutableStateOf(initialScreen)
        private set

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
    }
}

@Composable
fun rememberAppNavigator(initialScreen: AppScreen = AppScreen.AuthLanding): AppNavigator =
    remember {
        AppNavigator(initialScreen)
    }

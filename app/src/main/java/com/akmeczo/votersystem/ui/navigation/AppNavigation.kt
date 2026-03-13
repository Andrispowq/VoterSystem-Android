package com.akmeczo.votersystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

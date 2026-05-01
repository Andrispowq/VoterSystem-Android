package com.akmeczo.votersystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class ErrorPopupData(
    val title: String,
    val description: String
)

class AppNavigator(initialScreen: AppScreen) {
    var currentScreen by mutableStateOf(initialScreen)
        private set
    var errorPopup by mutableStateOf<ErrorPopupData?>(null)
        private set

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
    }

    fun showError(title: String, description: String) {
        errorPopup = ErrorPopupData(title = title, description = description)
    }

    fun dismissError() {
        errorPopup = null
    }
}

@Composable
fun rememberAppNavigator(initialScreen: AppScreen = AppScreen.AuthLanding): AppNavigator =
    remember {
        AppNavigator(initialScreen)
    }

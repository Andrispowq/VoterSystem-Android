package com.akmeczo.votersystem.ui.navigation

sealed interface AppScreen {
    data object AuthLanding : AppScreen
    data object LoginForm : AppScreen
    data object RegisterForm : AppScreen
    data object VotingList : AppScreen
    data class VotingDetail(val votingId: Long) : AppScreen
    data object VotingHistory : AppScreen
}
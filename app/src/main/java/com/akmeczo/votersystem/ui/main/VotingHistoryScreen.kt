package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.realtime.SignalRClient
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.server.responses.VotingResultsDto
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.BottomActionButtons
import com.akmeczo.votersystem.ui.ScreenTitleText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.appBackground
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@PreviewScreenSizes
@Composable
fun VotingHistoryScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingHistory)
) {
    var history by remember { mutableStateOf<List<VotingDto>>(emptyList()) }
    var liveResultsEnabledVotingIds by remember { mutableStateOf(setOf<Long>()) }
    val results = remember { mutableStateMapOf<Long, VotingResultsDto>() }
    val scope = rememberCoroutineScope()
    val signalR = remember(server) { SignalRClient("https://andris.picidolgok.hu/Hubs/VotesHub", server) }

    LaunchedEffect(signalR) {
        signalR.registerResultCallback(SignalRClient.UPDATED_RESULTS_CALLBACK_NAME) {
            scope.launch {
                results[it.votingId] = it.votingResults
            }
        }

        try {
            when (val result = Api.Votings.getVoted(server)) {
                is ApiResult.Success -> {
                    history = result.value

                    for (item in history) {
                        when (val result = Api.Votings.getResults(server, item.votingId)) {
                            is ApiResult.Success -> results[item.votingId] = result.value
                            else -> {}
                        }
                    }
                }
                is ApiResult.Failure -> {
                    navigator.showError(
                        title = "Failed to load",
                        description = "Failed to load votings. Please try again later."
                    )
                }
            }

            awaitCancellation()
        } finally {
            signalR.deregisterCallback(SignalRClient.UPDATED_RESULTS_CALLBACK_NAME)
            withContext(NonCancellable) {
                signalR.disconnect()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
            .padding(horizontal = UiTokens.listHorizontalPadding, vertical = UiTokens.listVerticalPadding)
    ) {
        ScreenTitleText(text = "Szavazz rám!")
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        if (history.any()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(items = history, key = { it.votingId }) { voting ->
                    val result = results[voting.votingId]
                    val isLiveEnabled = voting.votingId in liveResultsEnabledVotingIds
                    VotingOverviewCard(
                        voting = voting,
                        showResults = result != null,
                        headerAction = {
                            FilterChip(
                                selected = isLiveEnabled,
                                onClick = {
                                    scope.launch {
                                        val toggleSucceeded = if (isLiveEnabled) {
                                            signalR.unsubscribeFromVoting(voting.votingId)
                                        } else {
                                            signalR.subscribeToVoting(voting.votingId)
                                        }

                                        if (toggleSucceeded) {
                                            val updatedVotingIds = if (isLiveEnabled) {
                                                liveResultsEnabledVotingIds - voting.votingId
                                            } else {
                                                liveResultsEnabledVotingIds + voting.votingId
                                            }
                                            liveResultsEnabledVotingIds = updatedVotingIds
                                            if (updatedVotingIds.isEmpty()) {
                                                signalR.disconnect()
                                            }
                                        } else {
                                            navigator.showError(
                                                title = "Live results unavailable",
                                                description = "Could not update the live results subscription right now. Please try again."
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(if (isLiveEnabled) "Live on" else "Live off")
                                }
                            )
                        },
                        resultsContent = {
                            if (result != null) {
                                VotingResultsContent(voting = voting, results = result)
                            }
                        }
                    )
                }
            }
        } else {
            Text("No votings found",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        BottomActionButtons(
            leftText = "Voting",
            rightText = "Logout",
            onLeftClick = {
                scope.launch {
                    signalR.disconnect()
                    liveResultsEnabledVotingIds = emptySet()
                    navigator.navigateTo(AppScreen.VotingList)
                }
            },
            onRightClick = {
                scope.launch {
                    signalR.disconnect()
                    liveResultsEnabledVotingIds = emptySet()
                    Api.Users.logout(server)
                    server.clearSession()
                    navigator.navigateTo(AppScreen.AuthLanding)
                }
            }
        )
    }
}

@Composable
private fun VotingResultsContent(voting: VotingDto, results: VotingResultsDto) {
    val totalVotes = results.choiceResults.sumOf { it.voteCount }

    Column {
        results.choiceResults.forEach { choiceResult ->
            val choiceName = voting.voteChoices.find { it.choiceId == choiceResult.choiceId }?.name ?: "unknown"
            val percent = if (totalVotes == 0L) 0f else choiceResult.voteCount / totalVotes.toFloat() * 100
            val percentText = String.format(Locale.getDefault(), "%.2f", percent)
            BodyText("$choiceName: ${choiceResult.voteCount} ($percentText%)")
        }
    }
}

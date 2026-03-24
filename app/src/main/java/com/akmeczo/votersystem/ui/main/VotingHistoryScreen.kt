package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.server.responses.VotingResultsDto
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.BottomActionButtons
import com.akmeczo.votersystem.ui.ScreenTitleText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.appBackground
import java.util.Locale

@PreviewScreenSizes
@Composable
fun VotingHistoryScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingHistory)
) {
    var history by remember { mutableStateOf<List<VotingDto>>(emptyList()) }
    val results = remember { mutableStateMapOf<Long, VotingResultsDto>() }

    LaunchedEffect(Unit) {
        when (val result = Api.Votings.getVoted(server)) {
            is ApiResult.Success -> {
                println("Loaded stuff for history: ${result.value}")

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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
            .padding(horizontal = UiTokens.listHorizontalPadding, vertical = UiTokens.listVerticalPadding)
    ) {
        ScreenTitleText(text = "Szavazz rám!")
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(history) { voting ->
                VotingOverviewCard(
                    voting = voting,
                    resultsContent = {
                        val result = results[voting.votingId]
                        if (result != null) {
                            val total = result.choiceResults.sumOf { it.voteCount }

                            Column {
                                result.choiceResults.forEach { result ->
                                    val name =
                                        voting.voteChoices.find { it.choiceId == result.choiceId }
                                    val count = result.voteCount
                                    val percent = count.div(total.toFloat())
                                    val percentS = String.format(Locale.getDefault(), "%.2f", percent)
                                    BodyText("${name?.name ?: "unknown"}: ${result.voteCount} (${percentS}%)")
                                }
                            }
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        BottomActionButtons(
            leftText = "Voting",
            rightText = "Logout",
            onLeftClick = { navigator.navigateTo(AppScreen.VotingList) },
            onRightClick = { navigator.navigateTo(AppScreen.AuthLanding) }
        )
    }
}

package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.ui.AppCard
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.CardDivider
import com.akmeczo.votersystem.ui.CardTitleText
import com.akmeczo.votersystem.ui.MetaText
import com.akmeczo.votersystem.ui.MockVotingData
import com.akmeczo.votersystem.ui.RoundedActionButton
import com.akmeczo.votersystem.ui.ScreenTitleText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.VerticalSectionDivider
import com.akmeczo.votersystem.ui.appBackground
import kotlinx.coroutines.launch

@PreviewScreenSizes
@Composable
fun VotingDetailScreen(
    votingId: Long = MockVotingData.availableVotings.first().id,
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingDetail(votingId))
) {
    var voting by remember(votingId) { mutableStateOf<VotingDto?>(null) }
    var selectedChoiceId by remember(votingId) { mutableLongStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val result = Api.Votings.getById(server, votingId)) {
            is ApiResult.Success -> {
                voting = result.value

                val firstChoice = result.value.voteChoices.firstOrNull()
                if (firstChoice != null) {
                    selectedChoiceId = firstChoice.choiceId
                } else {
                    navigator.showError(
                        title = "No choice",
                        description = "This voting has no choices."
                    )
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
            .padding(horizontal = UiTokens.listHorizontalPadding, vertical = UiTokens.listVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitleText(text = "Szavazz rám!")
        Spacer(modifier = Modifier.height(UiTokens.detailGap))
        AppCard(modifier = Modifier.width(UiTokens.detailCardWidth)) {
            val obj = voting;
            if (obj == null) {
                return@AppCard
            }

            CardTitleText(obj.name)
            Spacer(modifier = Modifier.height(UiTokens.cardInnerGap))
            MetaText("Started: ${obj.startsAt}")
            MetaText("Ends: ${obj.endsAt}")
            CardDivider()
            Spacer(modifier = Modifier.height(UiTokens.sectionLabelGap))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    obj.voteChoices.forEach { choice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedChoiceId = choice.choiceId },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = choice.choiceId == selectedChoiceId,
                                onClick = { selectedChoiceId = choice.choiceId }
                            )
                            BodyText(choice.name)
                        }
                    }
                }
                VerticalSectionDivider()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = UiTokens.resultsHorizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    BodyText(
                        text = "No results available before voting",
                        centered = true
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(UiTokens.detailGap))
        RoundedActionButton(
            text = "Vote",
            onClick = {
                if (selectedChoiceId == 0L) {
                    navigator.showError(
                        title = "No choice selected",
                        description = "You have not yet selected a choice to cast your vote to."
                    )
                    return@RoundedActionButton
                }

                scope.launch {
                    when (val result = Api.Votes.castVote(server, selectedChoiceId)) {
                        is ApiResult.Success -> {
                            navigator.navigateTo(AppScreen.VotingHistory)
                        }
                        is ApiResult.Failure -> {
                            navigator.showError(
                                title = "Failed to cast vote",
                                description = "Error code: ${result.code}, details: ${result.content}"
                            )
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        RoundedActionButton(
            text = "Back",
            onClick = {
                navigator.navigateTo(AppScreen.VotingList)
            }
        )
    }
}
package com.akmeczo.votersystem.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Server

@PreviewScreenSizes
@Composable
fun VotingListScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingList)
) {
    val votings = remember { MockVotingData.availableVotings }

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
            items(votings) { voting ->
                VotingOverviewCard(
                    voting = voting,
                    resultsContent = {
                        BodyText(
                            text = "No results available before voting",
                            centered = true
                        )
                    },
                    modifier = Modifier.clickable {
                        navigator.navigateTo(AppScreen.VotingDetail(voting.id))
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(UiTokens.sectionGap))
        BottomActionButtons(
            leftText = "History",
            rightText = "Logout",
            onLeftClick = { navigator.navigateTo(AppScreen.VotingHistory) },
            onRightClick = { navigator.navigateTo(AppScreen.AuthLanding) }
        )
    }
}

@PreviewScreenSizes
@Composable
fun VotingDetailScreen(
    votingId: Long = MockVotingData.availableVotings.first().id,
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingDetail(votingId))
) {
    val voting = remember(votingId) { MockVotingData.findVoting(votingId) ?: MockVotingData.availableVotings.first() }
    var selectedChoiceId by remember(votingId) { mutableLongStateOf(voting.choices.first().id) }

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
            CardTitleText(voting.title)
            Spacer(modifier = Modifier.height(UiTokens.cardInnerGap))
            MetaText("Started: ${voting.startDate}")
            MetaText("Ends: ${voting.endDate}")
            CardDivider()
            Spacer(modifier = Modifier.height(UiTokens.sectionLabelGap))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    voting.choices.forEach { choice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedChoiceId = choice.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = choice.id == selectedChoiceId,
                                onClick = { selectedChoiceId = choice.id }
                            )
                            BodyText(choice.text)
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
            onClick = { navigator.navigateTo(AppScreen.VotingHistory) }
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

@PreviewScreenSizes
@Composable
fun VotingHistoryScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingHistory)
) {
    val history = remember { MockVotingData.historyVotings }

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
                        Column {
                            voting.results.forEach { result ->
                                BodyText("${result.label}: ${result.percent}%")
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

@Composable
private fun VotingOverviewCard(
    voting: VotingCardModel,
    resultsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        CardTitleText(voting.title)
        Spacer(modifier = Modifier.height(UiTokens.cardInnerGap))
        MetaText("Started: ${voting.startDate}")
        MetaText("Ends: ${voting.endDate}")
        CardDivider()
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                SectionLabelText("Choices:")
                voting.choices.forEach { choice ->
                    BodyText(text = "- ${choice.text}")
                }
            }
            VerticalSectionDivider()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = UiTokens.resultsHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                resultsContent()
            }
        }
    }
}

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Server
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
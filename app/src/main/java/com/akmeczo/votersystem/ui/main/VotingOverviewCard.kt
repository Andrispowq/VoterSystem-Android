package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.ui.AppCard
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.CardDivider
import com.akmeczo.votersystem.ui.CardTitleText
import com.akmeczo.votersystem.ui.MetaText
import com.akmeczo.votersystem.ui.SectionLabelText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.VerticalSectionDivider

@Composable
fun VotingOverviewCard(
    voting: VotingDto,
    resultsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        CardTitleText(voting.name)
        Spacer(modifier = Modifier.height(UiTokens.cardInnerGap))
        MetaText("Started: ${voting.startsAt}")
        MetaText("Ends: ${voting.endsAt}")
        CardDivider()
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                SectionLabelText("Choices:")
                voting.voteChoices.forEach { choice ->
                    BodyText(text = "- ${choice.name}")
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
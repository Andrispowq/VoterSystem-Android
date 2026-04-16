package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.ui.AppCard
import com.akmeczo.votersystem.ui.AppCardWhite
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.CardDivider
import com.akmeczo.votersystem.ui.CardTitleText
import com.akmeczo.votersystem.ui.MetaText
import com.akmeczo.votersystem.ui.SectionLabelText
import com.akmeczo.votersystem.ui.UiTokens

@Composable
fun VotingOverviewCard(
    voting: VotingDto,
    showResults: Boolean,
    resultsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null,
) {
    AppCard(
        modifier = modifier,
        containerColor = AppCardWhite,
        cornerRadius = 18.dp
    ) {
        CardTitleText(voting.name)
        if (headerAction != null) {
            Spacer(modifier = Modifier.height(UiTokens.smallGap))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                headerAction()
            }
        }
        Spacer(modifier = Modifier.height(UiTokens.cardInnerGap))
        MetaText("Started: ${formatVotingDateTime(voting.startsAt)}")
        MetaText("Ends: ${formatVotingDateTime(voting.endsAt)}")
        MetaText("Time left: ${formatTimeUntilVotingEnds(voting.endsAt)}")
        CardDivider()
        Spacer(modifier = Modifier.height(10.dp))
        SectionLabelText("Choices")
        voting.voteChoices.forEach { choice ->
            BodyText(text = "- ${choice.name}")
        }

        if (showResults) {
            CardDivider()
            Spacer(modifier = Modifier.height(UiTokens.sectionLabelGap))
            SectionLabelText("Results")
            Spacer(modifier = Modifier.height(UiTokens.smallGap))
            resultsContent()
        }
    }
}

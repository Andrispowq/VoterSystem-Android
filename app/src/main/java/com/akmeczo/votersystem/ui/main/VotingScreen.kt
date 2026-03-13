package com.akmeczo.votersystem.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.BottomActionButtons
import com.akmeczo.votersystem.ui.MockVotingData
import com.akmeczo.votersystem.ui.ScreenTitleText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.appBackground

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

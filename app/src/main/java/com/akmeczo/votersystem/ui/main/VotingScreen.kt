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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.ApiResult
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.VotingDto
import com.akmeczo.votersystem.ui.navigation.AppNavigator
import com.akmeczo.votersystem.ui.navigation.AppScreen
import com.akmeczo.votersystem.ui.BodyText
import com.akmeczo.votersystem.ui.BottomActionButtons
import com.akmeczo.votersystem.ui.CardTitleText
import com.akmeczo.votersystem.ui.ScreenTitleText
import com.akmeczo.votersystem.ui.UiTokens
import com.akmeczo.votersystem.ui.appBackground
import kotlinx.coroutines.launch

@PreviewScreenSizes
@Composable
fun VotingListScreen(
    server: Server = Server("", ""),
    navigator: AppNavigator = AppNavigator(AppScreen.VotingList)
) {
    var votings by remember { mutableStateOf<List<VotingDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val result = Api.Votings.getVotable(server)) {
            is ApiResult.Success -> votings = result.value
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
        if (votings.any()) {
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
                        showResults = false,
                        modifier = Modifier.clickable {
                            navigator.navigateTo(AppScreen.VotingDetail(voting.votingId))
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
            leftText = "History",
            rightText = "Logout",
            onLeftClick = { navigator.navigateTo(AppScreen.VotingHistory) },
            onRightClick = {
                scope.launch {
                    Api.Users.logout(server)
                    server.clearSession()
                    navigator.navigateTo(AppScreen.AuthLanding)
                }
            }
        )
    }
}

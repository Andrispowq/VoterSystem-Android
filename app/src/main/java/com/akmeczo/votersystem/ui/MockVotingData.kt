package com.akmeczo.votersystem.ui

data class VotingChoiceModel(
    val id: Long,
    val text: String
)

data class ResultSliceModel(
    val label: String,
    val percent: Int
)

data class VotingCardModel(
    val id: Long,
    val title: String,
    val startDate: String,
    val endDate: String,
    val choices: List<VotingChoiceModel>,
    val results: List<ResultSliceModel>
)

object MockVotingData {
    val availableVotings = listOf(
        VotingCardModel(
            id = 1,
            title = "Best Spring Festival Theme",
            startDate = "2026-03-20",
            endDate = "2026-03-25",
            choices = listOf(
                VotingChoiceModel(101, "Classic campus"),
                VotingChoiceModel(102, "Retro city lights"),
                VotingChoiceModel(103, "Pastel garden")
            ),
            results = emptyList()
        ),
        VotingCardModel(
            id = 2,
            title = "Student Union Poster Pick",
            startDate = "2026-03-22",
            endDate = "2026-03-28",
            choices = listOf(
                VotingChoiceModel(201, "Bold typography"),
                VotingChoiceModel(202, "Photo collage"),
                VotingChoiceModel(203, "Minimal line art")
            ),
            results = emptyList()
        )
    )

    val historyVotings = listOf(
        VotingCardModel(
            id = 3,
            title = "Winter Event Headliner",
            startDate = "2026-02-01",
            endDate = "2026-02-06",
            choices = listOf(
                VotingChoiceModel(301, "Local indie band"),
                VotingChoiceModel(302, "Jazz quartet"),
                VotingChoiceModel(303, "DJ night")
            ),
            results = listOf(
                ResultSliceModel("Local indie band", 42),
                ResultSliceModel("Jazz quartet", 33),
                ResultSliceModel("DJ night", 25)
            )
        ),
        VotingCardModel(
            id = 4,
            title = "Library Lounge Refresh",
            startDate = "2026-01-12",
            endDate = "2026-01-18",
            choices = listOf(
                VotingChoiceModel(401, "More plants"),
                VotingChoiceModel(402, "Reading pods"),
                VotingChoiceModel(403, "Standing desks")
            ),
            results = listOf(
                ResultSliceModel("More plants", 29),
                ResultSliceModel("Reading pods", 47),
                ResultSliceModel("Standing desks", 24)
            )
        )
    )

    fun findVoting(votingId: Long): VotingCardModel? =
        (availableVotings + historyVotings).firstOrNull { it.id == votingId }
}

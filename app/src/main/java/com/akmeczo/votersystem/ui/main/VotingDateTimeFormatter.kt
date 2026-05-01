package com.akmeczo.votersystem.ui.main

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val votingDateTimeFormatterPattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatVotingDateTime(instant: Instant): String =
    votingDateTimeFormatterPattern
        .withZone(ZoneId.systemDefault())
        .format(instant)

fun formatTimeUntilVotingEnds(
    endsAt: Instant,
    now: Instant = Instant.now()
): String {
    if (!endsAt.isAfter(now)) {
        return "Ended"
    }

    val remaining = Duration.between(now, endsAt)
    val totalMinutes = remaining.toMinutes()
    val totalHours = remaining.toHours()
    val totalDays = remaining.toDays()
    val totalWeeks = totalDays / 7

    return when {
        totalWeeks > 0 -> "$totalWeeks ${pluralize(totalWeeks, "week")}"
        totalDays > 0 -> "$totalDays ${pluralize(totalDays, "day")}"
        totalHours > 0 -> "$totalHours ${pluralize(totalHours, "hour")}"
        totalMinutes > 0 -> "$totalMinutes ${pluralize(totalMinutes, "minute")}"
        else -> "Less than a minute"
    }
}

private fun pluralize(value: Long, unit: String): String =
    if (value == 1L) unit else "${unit}s"

package com.akmeczo.votersystem.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class VotingDateTimeFormatterTest {
    @Test
    fun formatVotingDateTime_usesExpectedPattern() {
        val originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        try {
            val formatted = formatVotingDateTime(Instant.parse("2026-05-06T17:33:21Z"))

            assertEquals("2026-05-06 17:33:21", formatted)
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun formatTimeUntilVotingEnds_returnsLargestWholeUnit() {
        val now = Instant.parse("2026-05-01T12:00:00Z")

        assertEquals("2 weeks", formatTimeUntilVotingEnds(now.plusSeconds(14 * 24 * 60 * 60), now))
        assertEquals("6 days", formatTimeUntilVotingEnds(now.plusSeconds(6 * 24 * 60 * 60), now))
        assertEquals("5 hours", formatTimeUntilVotingEnds(now.plusSeconds(5 * 60 * 60), now))
        assertEquals("42 minutes", formatTimeUntilVotingEnds(now.plusSeconds(42 * 60), now))
    }

    @Test
    fun formatTimeUntilVotingEnds_handlesExpiredAndSubMinuteDurations() {
        val now = Instant.parse("2026-05-01T12:00:00Z")

        assertEquals("Ended", formatTimeUntilVotingEnds(now, now))
        assertEquals("Less than a minute", formatTimeUntilVotingEnds(now.plusSeconds(30), now))
    }
}

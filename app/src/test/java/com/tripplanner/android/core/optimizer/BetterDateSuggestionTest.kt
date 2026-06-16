package com.tripplanner.android.core.optimizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the calendar's "more efficient nearby" suggestion: even with no public
 * holiday, a mid-week selection should be nudged toward a weekend-aligned window
 * that costs fewer leave days.
 */
class BetterDateSuggestionTest {

    // 2026 with no holidays: only weekends reduce leave. Jan 1 2026 is a Thursday,
    // so Jan 2 is Friday and Jan 6 is Tuesday.
    private val scores = LeaveOptimization.calculateOptimizationScores(
        tripDays = 3,
        year = 2026,
        holidays = emptyList(),
    )

    @Test
    fun `mid-week selection is offered a cheaper weekend-aligned window`() {
        val tuesday = LocalDate.of(2026, 1, 6) // Tue–Thu = 3 leave
        val better = LeaveOptimization.findBetterDateRange(tuesday, scores)
        assertNotNull(better)
        better!!
        assertEquals(1, better.leaveDaysNeeded)
        assertEquals(2, better.savedDays)
    }

    @Test
    fun `an already-efficient selection yields no suggestion`() {
        // Fri–Sun already costs just 1 leave day; nothing nearby beats it.
        val friday = LocalDate.of(2026, 1, 2)
        assertNull(LeaveOptimization.findBetterDateRange(friday, scores))
    }
}

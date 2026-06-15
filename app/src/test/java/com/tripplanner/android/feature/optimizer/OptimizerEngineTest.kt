package com.tripplanner.android.feature.optimizer

import com.tripplanner.android.core.optimizer.DateOptimization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OptimizerEngineTest {

    private fun score(
        date: LocalDate,
        efficiency: Double,
        leave: Int,
        near: List<String> = emptyList(),
    ) = DateOptimization(date, efficiency, leave, near)

    @Test
    fun `filters out windows that do not beat a plain leave block`() {
        val scores = listOf(
            score(LocalDate.of(2026, 1, 5), efficiency = 1.0, leave = 5),
            score(LocalDate.of(2026, 3, 2), efficiency = 0.8, leave = 6),
        )
        assertTrue(OptimizerEngine.pickTopSuggestions(scores, tripDays = 5).isEmpty())
    }

    @Test
    fun `greedily picks the most efficient non-overlapping windows`() {
        val scores = listOf(
            score(LocalDate.of(2026, 8, 6), efficiency = 2.0, leave = 2),  // Aug 6–10
            score(LocalDate.of(2026, 8, 8), efficiency = 1.8, leave = 3),  // overlaps Aug 6–10
            score(LocalDate.of(2026, 12, 24), efficiency = 1.5, leave = 3),
            score(LocalDate.of(2026, 1, 5), efficiency = 1.0, leave = 5),  // filtered
        )
        val result = OptimizerEngine.pickTopSuggestions(scores, tripDays = 5)

        assertEquals(2, result.size)
        // Sorted chronologically for display.
        assertEquals(LocalDate.of(2026, 8, 6), result[0].startDate)
        assertEquals(LocalDate.of(2026, 12, 24), result[1].startDate)
    }

    @Test
    fun `respects the limit`() {
        val scores = listOf(
            score(LocalDate.of(2026, 2, 2), efficiency = 2.5, leave = 2),
            score(LocalDate.of(2026, 5, 4), efficiency = 2.0, leave = 2),
            score(LocalDate.of(2026, 9, 7), efficiency = 1.5, leave = 3),
        )
        assertEquals(1, OptimizerEngine.pickTopSuggestions(scores, tripDays = 5, limit = 1).size)
    }

    @Test
    fun `suggestion derives daysOff and savedDays`() {
        val scores = listOf(score(LocalDate.of(2026, 8, 6), efficiency = 2.5, leave = 2))
        val s = OptimizerEngine.pickTopSuggestions(scores, tripDays = 5).single()
        assertEquals(5, s.daysOff)
        assertEquals(3, s.savedDays)              // 5 days off for 2 leave
        assertEquals(LocalDate.of(2026, 8, 10), s.endDate)
    }

    @Test
    fun `defaultYear prefers the current year when available`() {
        val years = listOf(2025, 2026, 2027)
        assertEquals(2026, OptimizerEngine.defaultYear(years, LocalDate.of(2026, 6, 15)))
    }

    @Test
    fun `defaultYear falls back to the first available year`() {
        val years = listOf(2025, 2026, 2027)
        assertEquals(2025, OptimizerEngine.defaultYear(years, LocalDate.of(2030, 1, 1)))
    }
}

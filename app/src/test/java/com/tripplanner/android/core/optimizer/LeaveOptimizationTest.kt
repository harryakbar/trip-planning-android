package com.tripplanner.android.core.optimizer

import com.tripplanner.android.core.holidays.ResolvedHoliday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Ports `utils/__tests__/leaveOptimization.test.ts`. */
class LeaveOptimizationTest {

    private val year = 2026

    // Clean non-holiday Monday.
    private val monJan5 = LocalDate.of(2026, 1, 5)

    // Observed National Day (Aug 9 Sunday -> Aug 10 Monday).
    private val monAug10 = LocalDate.of(2026, 8, 10)

    private val sgNationalDayObserved = ResolvedHoliday(
        date = LocalDate.of(2026, 8, 10),
        name = "National Day (observed)",
    )

    private val idHoliday = ResolvedHoliday(LocalDate.of(2026, 1, 1), "New Year")
    private val idCutiBersama = ResolvedHoliday(
        date = LocalDate.of(2026, 1, 2),
        name = "Cuti Bersama (New Year)",
        isCutiBersama = true,
    )

    private fun makeHolidays(dates: List<LocalDate>): List<ResolvedHoliday> =
        dates.mapIndexed { i, d -> ResolvedHoliday(d, "Holiday ${i + 1}") }

    // --- buildWorkingDayPrefix ---

    @Test
    fun `prefix starts at zero`() {
        assertEquals(0, LeaveOptimization.buildWorkingDayPrefix(year, emptyList()).prefix[0])
    }

    @Test
    fun `weekend days do not increment prefix`() {
        val sat = LocalDate.of(2026, 1, 3)
        val sun = LocalDate.of(2026, 1, 4)
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, makeHolidays(listOf(sat, sun))).prefix
        // Jan 1 (Thu) + Jan 2 (Fri) working; Sat/Sun not.
        assertEquals(2, prefix[3])
        assertEquals(2, prefix[4])
    }

    @Test
    fun `public holiday does not increment prefix`() {
        val newYear = ResolvedHoliday(LocalDate.of(2026, 1, 1), "New Year")
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, listOf(newYear)).prefix
        assertEquals(0, prefix[1])
    }

    @Test
    fun `holiday index maps to name`() {
        val newYear = ResolvedHoliday(LocalDate.of(2026, 1, 1), "New Year")
        val map = LeaveOptimization.buildWorkingDayPrefix(year, listOf(newYear)).holidayIndexToName
        assertEquals(listOf("New Year"), map[0])
    }

    @Test
    fun `cuti bersama days are non-working`() {
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, listOf(idCutiBersama)).prefix
        // Jan 2 (index 1) would be a working Friday; cuti bersama makes it non-working.
        assertEquals(prefix[1], prefix[2])
    }

    // --- calculateWorkingDaysNeeded ---

    @Test
    fun `weekend-only range needs zero leave`() {
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, emptyList())
        val sat = LocalDate.of(2026, 1, 3)
        assertEquals(0, LeaveOptimization.calculateWorkingDaysNeeded(sat, 2, prefix))
    }

    @Test
    fun `Mon-Fri range needs five leave`() {
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, emptyList())
        assertEquals(5, LeaveOptimization.calculateWorkingDaysNeeded(monJan5, 5, prefix))
    }

    @Test
    fun `trip spanning a public holiday needs fewer leave`() {
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, listOf(sgNationalDayObserved))
        // 5-day trip from Mon Aug 10 (observed holiday): Mon free, Tue-Fri = 4 leave.
        assertEquals(4, LeaveOptimization.calculateWorkingDaysNeeded(monAug10, 5, prefix))
    }

    @Test
    fun `out-of-year start returns zero`() {
        val prefix = LeaveOptimization.buildWorkingDayPrefix(year, emptyList())
        val outOfYear = LocalDate.of(2025, 12, 31)
        assertEquals(0, LeaveOptimization.calculateWorkingDaysNeeded(outOfYear, 5, prefix))
    }

    // --- calculateOptimizationScores ---

    @Test
    fun `returns scores for most of the year`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, emptyList())
        assertTrue(scores.size > 300)
    }

    @Test
    fun `excludes start dates whose trip extends beyond the year`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, emptyList())
        val lastAllowedStart = LocalDate.of(year, 12, 31).minusDays(4)
        assertTrue(!scores.last().date.isAfter(lastAllowedStart))
    }

    @Test
    fun `near-holiday efficiency beats far-from-holiday`() {
        val scores = LeaveOptimization.calculateOptimizationScores(
            5, year, listOf(sgNationalDayObserved), OptimizationConfig(proximityWindowDays = 5),
        )
        val near = scores.first { it.date == monAug10 }
        val far = scores.first { it.date == monJan5 }
        assertTrue(near.efficiency > far.efficiency)
    }

    @Test
    fun `weekend plus holiday yields highest efficiency`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, listOf(sgNationalDayObserved))
        val holidayMonStart = scores.first { it.date == monAug10 }
        assertTrue(holidayMonStart.efficiency > 1.0)
        assertTrue(holidayMonStart.leaveDaysNeeded < 5)
    }

    @Test
    fun `cuti bersama days are counted in scoring`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, listOf(idHoliday, idCutiBersama))
        val jan1 = scores.first { it.date == LocalDate.of(2026, 1, 1) }
        // Jan 1 (holiday) + Jan 2 (cuti bersama) + Jan 3-4 (weekend) free; only Jan 5 working.
        assertTrue(jan1.leaveDaysNeeded < 3)
    }

    @Test
    fun `wider proximity window collects at least as many near holidays`() {
        val narrow = LeaveOptimization.calculateOptimizationScores(
            5, year, listOf(sgNationalDayObserved), OptimizationConfig(proximityWindowDays = 1),
        )
        val wide = LeaveOptimization.calculateOptimizationScores(
            5, year, listOf(sgNationalDayObserved), OptimizationConfig(proximityWindowDays = 7),
        )
        val threeBefore = sgNationalDayObserved.date.minusDays(3)
        val n = narrow.firstOrNull { it.date == threeBefore }?.nearHolidays?.size ?: 0
        val w = wide.firstOrNull { it.date == threeBefore }?.nearHolidays?.size ?: 0
        assertTrue(n <= w)
    }

    @Test
    fun `does not throw at year boundary`() {
        LeaveOptimization.calculateOptimizationScores(5, year, emptyList())
    }

    // --- findBetterDateRange ---

    @Test
    fun `suggests a nearby date that saves leave days`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, listOf(sgNationalDayObserved))
        // Mon Aug 3: 5-day trip Mon-Fri = 5 leave (worst case); a date near Aug 10 is better.
        val monBefore = LocalDate.of(2026, 8, 3)
        val result = LeaveOptimization.findBetterDateRange(monBefore, scores, OptimizationConfig(searchWindowDays = 14))
        assertNotNull(result)
        assertTrue(result!!.savedDays > 0)
        assertTrue(result.leaveDaysNeeded < 5)
    }

    @Test
    fun `suggested date is within the search window`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, listOf(sgNationalDayObserved))
        val friday = LocalDate.of(2026, 8, 7)
        val result = LeaveOptimization.findBetterDateRange(friday, scores, OptimizationConfig(searchWindowDays = 14))
        if (result != null) {
            val diff = kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(friday, result.date))
            assertTrue(diff <= 14)
        }
    }

    @Test
    fun `savedDays is positive when a better date exists`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, listOf(sgNationalDayObserved))
        val result = LeaveOptimization.findBetterDateRange(LocalDate.of(2026, 8, 3), scores)
        assertNotNull(result)
        assertTrue(result!!.savedDays >= 1)
    }

    @Test
    fun `returns null when start date not in scores`() {
        val scores = LeaveOptimization.calculateOptimizationScores(5, year, emptyList())
        assertNull(LeaveOptimization.findBetterDateRange(LocalDate.of(2025, 1, 1), scores))
    }
}

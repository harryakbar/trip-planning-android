package com.tripplanner.android.core.optimizer

import com.tripplanner.android.core.holidays.ResolvedHoliday
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/** A candidate trip start date with its leave efficiency. */
data class DateOptimization(
    val date: LocalDate,
    val efficiency: Double,
    val leaveDaysNeeded: Int,
    val nearHolidays: List<String>,
)

/** A nearby alternative start date that saves leave days versus the chosen one. */
data class BetterDateSuggestion(
    val date: LocalDate,
    val leaveDaysNeeded: Int,
    val savedDays: Int,
    val nearHolidays: List<String>,
)

/** Tunables for the optimizer. Defaults match the web app. */
data class OptimizationConfig(
    val proximityWindowDays: Int = 5,
    val searchWindowDays: Int = 14,
)

/**
 * Prefix-sum of working days across a year, enabling O(1) "leave days needed"
 * queries for any date range. `prefix[i]` = number of working days in
 * `[yearStart, yearStart + i)`.
 */
class WorkingDayPrefix(
    val yearStart: LocalDate,
    val daysInYear: Int,
    val prefix: IntArray,
    val holidayIndexToName: Map<Int, List<String>>,
)

/**
 * Leave optimizer ported from `utils/leaveOptimization.ts`. Pure logic over
 * [java.time.LocalDate] — no Android dependencies, fully unit-testable.
 */
object LeaveOptimization {

    fun buildWorkingDayPrefix(year: Int, holidays: List<ResolvedHoliday>): WorkingDayPrefix {
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        val daysInYear = ChronoUnit.DAYS.between(yearStart, yearEnd).toInt() + 1

        val holidayIndexToName = mutableMapOf<Int, MutableList<String>>()
        for (h in holidays) {
            if (h.date.year != year) continue
            val idx = ChronoUnit.DAYS.between(yearStart, h.date).toInt()
            if (idx < 0 || idx >= daysInYear) continue
            holidayIndexToName.getOrPut(idx) { mutableListOf() }.add(h.name)
        }

        val prefix = IntArray(daysInYear + 1)
        for (i in 0 until daysInYear) {
            val date = yearStart.plusDays(i.toLong())
            val isHoliday = holidayIndexToName.containsKey(i)
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val isWorking = !isHoliday && !isWeekend
            prefix[i + 1] = prefix[i] + if (isWorking) 1 else 0
        }

        return WorkingDayPrefix(yearStart, daysInYear, prefix, holidayIndexToName)
    }

    fun calculateWorkingDaysNeeded(
        startDate: LocalDate,
        tripDays: Int,
        prefixData: WorkingDayPrefix,
    ): Int {
        val startIdx = ChronoUnit.DAYS.between(prefixData.yearStart, startDate).toInt()
        val endIdx = startIdx + tripDays - 1
        if (startIdx < 0 || endIdx >= prefixData.daysInYear) return 0
        return prefixData.prefix[endIdx + 1] - prefixData.prefix[startIdx]
    }

    fun calculateOptimizationScores(
        tripDays: Int,
        year: Int,
        holidays: List<ResolvedHoliday>,
        config: OptimizationConfig = OptimizationConfig(),
    ): List<DateOptimization> {
        val proximityWindowDays = config.proximityWindowDays
        val prefixData = buildWorkingDayPrefix(year, holidays)

        val lastStartIdx = prefixData.daysInYear - tripDays
        if (lastStartIdx < 0) return emptyList()

        val scores = ArrayList<DateOptimization>(lastStartIdx + 1)
        for (startIdx in 0..lastStartIdx) {
            val endIdx = startIdx + tripDays - 1
            val leaveDaysNeeded = prefixData.prefix[endIdx + 1] - prefixData.prefix[startIdx]
            if (leaveDaysNeeded <= 0) continue

            val nearStart = maxOf(0, startIdx - proximityWindowDays)
            val nearEnd = minOf(prefixData.daysInYear - 1, endIdx + proximityWindowDays)
            val nearHolidays = LinkedHashSet<String>()
            for (i in nearStart..nearEnd) {
                prefixData.holidayIndexToName[i]?.let { nearHolidays.addAll(it) }
            }

            scores.add(
                DateOptimization(
                    date = prefixData.yearStart.plusDays(startIdx.toLong()),
                    efficiency = tripDays.toDouble() / leaveDaysNeeded,
                    leaveDaysNeeded = leaveDaysNeeded,
                    nearHolidays = nearHolidays.toList(),
                ),
            )
        }

        return scores
    }

    fun findBetterDateRange(
        startDate: LocalDate,
        scores: List<DateOptimization>,
        config: OptimizationConfig = OptimizationConfig(),
    ): BetterDateSuggestion? {
        val searchWindowDays = config.searchWindowDays

        val currentScore = scores.firstOrNull { it.date == startDate } ?: return null

        val best = scores
            .filter { s ->
                val diff = abs(ChronoUnit.DAYS.between(startDate, s.date))
                diff > 0 && diff <= searchWindowDays &&
                    s.efficiency > currentScore.efficiency &&
                    s.leaveDaysNeeded < currentScore.leaveDaysNeeded
            }
            .minByOrNull { it.leaveDaysNeeded }
            ?: return null

        return BetterDateSuggestion(
            date = best.date,
            leaveDaysNeeded = best.leaveDaysNeeded,
            savedDays = currentScore.leaveDaysNeeded - best.leaveDaysNeeded,
            nearHolidays = best.nearHolidays,
        )
    }
}

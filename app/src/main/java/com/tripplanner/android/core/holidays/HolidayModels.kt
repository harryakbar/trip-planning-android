package com.tripplanner.android.core.holidays

import java.time.LocalDate

/**
 * Supported countries. Mirrors `CountryCode`, `COUNTRY_LABELS` and
 * `DEFAULT_ANNUAL_LEAVE` from the web app (`data/holidays/types.ts`).
 */
enum class CountryCode(val label: String, val defaultAnnualLeave: Int) {
    SG("Singapore", 14),
    ID("Indonesia", 12),
}

/** Indonesia-specific collective-leave day tied to a holiday. */
data class CutiBersamaDay(val month: Int, val day: Int)

/** A raw holiday definition for a given year (month/day in that year). */
data class HolidayEntry(
    val month: Int,
    val day: Int,
    val name: String,
    val cutiBersama: List<CutiBersamaDay> = emptyList(),
)

/** All raw holiday definitions for one country in one year. */
data class YearHolidays(
    val year: Int,
    val country: CountryCode,
    val holidays: List<HolidayEntry>,
)

/**
 * A concrete holiday resolved to a calendar date, after applying
 * country-specific rules (SG weekend substitution, ID cuti bersama).
 */
data class ResolvedHoliday(
    val date: LocalDate,
    val name: String,
    val isCutiBersama: Boolean = false,
)

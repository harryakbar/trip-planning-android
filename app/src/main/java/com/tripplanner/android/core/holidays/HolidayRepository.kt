package com.tripplanner.android.core.holidays

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Resolves raw holiday definitions into concrete dates, applying
 * country-specific rules. Ported from `data/holidays/index.ts`.
 */
object HolidayRepository {

    // LinkedHashMap preserves insertion order (SG, then ID).
    private val registry: Map<CountryCode, List<YearHolidays>> = linkedMapOf(
        CountryCode.SG to SG_HOLIDAYS,
        CountryCode.ID to ID_HOLIDAYS,
    )

    /** Resolved holidays for a country/year, sorted by date. Empty if the year isn't known. */
    fun getHolidaysForYear(country: CountryCode, year: Int): List<ResolvedHoliday> {
        val yearData = registry[country]?.firstOrNull { it.year == year } ?: return emptyList()
        return resolveEntries(year, yearData.holidays, country)
    }

    /** Years with data for the country, ascending. */
    fun getAvailableYears(country: CountryCode): List<Int> =
        registry.getValue(country).map { it.year }.sorted()

    /** Supported countries, in registry order. */
    fun getSupportedCountries(): List<CountryCode> = registry.keys.toList()

    private fun resolveEntries(
        year: Int,
        entries: List<HolidayEntry>,
        country: CountryCode,
    ): List<ResolvedHoliday> {
        val result = mutableListOf<ResolvedHoliday>()

        for (entry in entries) {
            val date = LocalDate.of(year, entry.month, entry.day)

            if (country == CountryCode.SG) {
                result.add(adjustForWeekendSG(date, entry.name))
            } else {
                result.add(ResolvedHoliday(date, entry.name))
            }

            for (cb in entry.cutiBersama) {
                result.add(
                    ResolvedHoliday(
                        date = LocalDate.of(year, cb.month, cb.day),
                        name = "Cuti Bersama (${entry.name})",
                        isCutiBersama = true,
                    ),
                )
            }
        }

        return result.sortedBy { it.date }
    }

    /**
     * Singapore observes a substitute holiday on Monday when a public holiday
     * falls on a Sunday. Saturday holidays are not substituted.
     */
    private fun adjustForWeekendSG(date: LocalDate, name: String): ResolvedHoliday =
        if (date.dayOfWeek == DayOfWeek.SUNDAY) {
            ResolvedHoliday(date.plusDays(1), "$name (observed)")
        } else {
            ResolvedHoliday(date, name)
        }
}

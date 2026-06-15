package com.tripplanner.android.core.holidays

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/** Ports `data/holidays/__tests__/holidays.test.ts`. */
class HolidayRepositoryTest {

    @Test
    fun `returns 11 holidays for SG 2026`() {
        assertEquals(11, HolidayRepository.getHolidaysForYear(CountryCode.SG, 2026).size)
    }

    @Test
    fun `returns holidays sorted by date`() {
        val holidays = HolidayRepository.getHolidaysForYear(CountryCode.SG, 2026)
        for (i in 1 until holidays.size) {
            assertTrue(!holidays[i].date.isBefore(holidays[i - 1].date))
        }
    }

    @Test
    fun `returns empty list for unsupported year`() {
        assertTrue(HolidayRepository.getHolidaysForYear(CountryCode.SG, 9999).isEmpty())
    }

    @Test
    fun `applies Singapore weekend substitution Sunday to Monday`() {
        // 2027-02-07 is a Sunday (Chinese New Year) -> observed Monday Feb 8.
        val holidays = HolidayRepository.getHolidaysForYear(CountryCode.SG, 2027)
        val observed = holidays.firstOrNull {
            it.name.contains("Chinese New Year") && it.name.contains("observed")
        }
        assertNotNull(observed)
        assertEquals(DayOfWeek.MONDAY, observed!!.date.dayOfWeek)
        assertEquals(8, observed.date.dayOfMonth)
    }

    @Test
    fun `does not substitute Saturday holidays in SG`() {
        // 2025-08-09 (National Day) is a Saturday.
        val holidays = HolidayRepository.getHolidaysForYear(CountryCode.SG, 2025)
        val nationalDay = holidays.firstOrNull { it.name == "National Day" }
        assertNotNull(nationalDay)
        assertEquals(DayOfWeek.SATURDAY, nationalDay!!.date.dayOfWeek)
        assertFalse(nationalDay.name.contains("observed"))
    }

    @Test
    fun `includes cuti bersama for Indonesia`() {
        val cutiBersama = HolidayRepository.getHolidaysForYear(CountryCode.ID, 2025)
            .filter { it.isCutiBersama }
        assertTrue(cutiBersama.isNotEmpty())
        assertTrue(cutiBersama.first().name.contains("Cuti Bersama"))
    }

    @Test
    fun `returns 16 base holidays for ID 2026`() {
        val base = HolidayRepository.getHolidaysForYear(CountryCode.ID, 2026)
            .filter { !it.isCutiBersama }
        assertEquals(16, base.size)
    }

    @Test
    fun `does not apply weekend substitution for Indonesia`() {
        val observed = HolidayRepository.getHolidaysForYear(CountryCode.ID, 2026)
            .filter { it.name.contains("observed") }
        assertTrue(observed.isEmpty())
    }

    @Test
    fun `getAvailableYears returns sorted years for SG and ID`() {
        assertEquals(listOf(2025, 2026, 2027), HolidayRepository.getAvailableYears(CountryCode.SG))
        assertEquals(listOf(2025, 2026, 2027), HolidayRepository.getAvailableYears(CountryCode.ID))
    }

    @Test
    fun `getSupportedCountries returns SG and ID`() {
        val countries = HolidayRepository.getSupportedCountries()
        assertEquals(2, countries.size)
        assertTrue(countries.contains(CountryCode.SG))
        assertTrue(countries.contains(CountryCode.ID))
    }
}

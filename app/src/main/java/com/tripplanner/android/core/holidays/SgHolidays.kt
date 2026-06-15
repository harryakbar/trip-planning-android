package com.tripplanner.android.core.holidays

/** Singapore public holidays. Ported from `data/holidays/sg.ts`. */
internal val SG_HOLIDAYS: List<YearHolidays> = listOf(
    YearHolidays(
        year = 2025,
        country = CountryCode.SG,
        holidays = listOf(
            HolidayEntry(1, 1, "New Year's Day"),
            HolidayEntry(1, 29, "Chinese New Year"),
            HolidayEntry(1, 30, "Chinese New Year"),
            HolidayEntry(3, 31, "Hari Raya Puasa"),
            HolidayEntry(4, 18, "Good Friday"),
            HolidayEntry(5, 1, "Labour Day"),
            HolidayEntry(5, 12, "Vesak Day"),
            HolidayEntry(6, 7, "Hari Raya Haji"),
            HolidayEntry(8, 9, "National Day"),
            HolidayEntry(11, 1, "Deepavali"),
            HolidayEntry(12, 25, "Christmas Day"),
        ),
    ),
    YearHolidays(
        year = 2026,
        country = CountryCode.SG,
        holidays = listOf(
            HolidayEntry(1, 1, "New Year's Day"),
            HolidayEntry(2, 17, "Chinese New Year"),
            HolidayEntry(2, 18, "Chinese New Year"),
            HolidayEntry(3, 21, "Hari Raya Puasa"),
            HolidayEntry(4, 3, "Good Friday"),
            HolidayEntry(5, 1, "Labour Day"),
            HolidayEntry(5, 27, "Hari Raya Haji"),
            HolidayEntry(5, 31, "Vesak Day"),
            HolidayEntry(8, 9, "National Day"),
            HolidayEntry(11, 8, "Deepavali"),
            HolidayEntry(12, 25, "Christmas Day"),
        ),
    ),
    YearHolidays(
        year = 2027,
        country = CountryCode.SG,
        holidays = listOf(
            HolidayEntry(1, 1, "New Year's Day"),
            HolidayEntry(2, 6, "Chinese New Year"),
            HolidayEntry(2, 7, "Chinese New Year"),
            HolidayEntry(3, 10, "Hari Raya Puasa"),
            HolidayEntry(3, 26, "Good Friday"),
            HolidayEntry(5, 1, "Labour Day"),
            HolidayEntry(5, 16, "Hari Raya Haji"),
            HolidayEntry(5, 20, "Vesak Day"),
            HolidayEntry(8, 9, "National Day"),
            HolidayEntry(10, 28, "Deepavali"),
            HolidayEntry(12, 25, "Christmas Day"),
        ),
    ),
)

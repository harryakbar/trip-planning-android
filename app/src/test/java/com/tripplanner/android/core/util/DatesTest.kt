package com.tripplanner.android.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Ports `utils/__tests__/dates.test.ts`. */
class DatesTest {

    @Test
    fun `overlapping ranges return true`() {
        assertTrue(
            datesOverlap(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 15),
            ),
        )
    }

    @Test
    fun `touching ranges return true`() {
        assertTrue(
            datesOverlap(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10),
            ),
        )
    }

    @Test
    fun `disjoint ranges return false`() {
        assertFalse(
            datesOverlap(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 10),
            ),
        )
    }
}

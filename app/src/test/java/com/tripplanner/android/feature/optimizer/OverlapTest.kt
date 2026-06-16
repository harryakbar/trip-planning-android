package com.tripplanner.android.feature.optimizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class OverlapTest {

    // A trip spanning Jan 10–14, 2026.
    private val state = OptimizerUiState(
        trips = listOf(
            PlannedTrip(
                id = 1,
                destination = "Bali",
                startDate = LocalDate.of(2026, 1, 10),
                tripDays = 5,
                leaveDaysNeeded = 3,
            ),
        ),
    )

    @Test
    fun `detects an overlapping range`() {
        val hit = state.overlappingTrip(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 16))
        assertEquals("Bali", hit?.destination)
    }

    @Test
    fun `touching boundary counts as overlap`() {
        val hit = state.overlappingTrip(LocalDate.of(2026, 1, 14), LocalDate.of(2026, 1, 18))
        assertEquals(1L, hit?.id)
    }

    @Test
    fun `disjoint range does not overlap`() {
        assertNull(state.overlappingTrip(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25)))
    }

    @Test
    fun `no trips means no overlap`() {
        val empty = OptimizerUiState()
        assertNull(empty.overlappingTrip(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)))
    }
}

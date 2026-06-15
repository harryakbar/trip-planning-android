package com.tripplanner.android.core.util

import java.time.LocalDate

/** True if the inclusive ranges [start1, end1] and [start2, end2] overlap. Ported from `utils/dates.ts`. */
fun datesOverlap(
    start1: LocalDate,
    end1: LocalDate,
    start2: LocalDate,
    end2: LocalDate,
): Boolean = start1 <= end2 && end1 >= start2

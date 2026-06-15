package com.tripplanner.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Base size 16 / medium 500 / normal 400 to match the web design system.
val Typography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
)

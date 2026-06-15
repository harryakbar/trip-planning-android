package com.tripplanner.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TripCategory(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onColor: Color = Color.White,
) {
    Food(
        label = "Food & Drink",
        icon = Icons.Default.Restaurant,
        color = Color(0xFFE8622C),   // chart-1 orange
    ),
    Sightseeing(
        label = "Sightseeing",
        icon = Icons.Default.PhotoCamera,
        color = Color(0xFF1AAD9A),   // chart-2 teal
    ),
    Transport(
        label = "Transport",
        icon = Icons.Default.DirectionsBus,
        color = Color(0xFF2B5C8B),   // chart-3 dark blue
    ),
    Accommodation(
        label = "Accommodation",
        icon = Icons.Default.Hotel,
        color = Color(0xFFCDB820),   // chart-4 yellow
        onColor = Color(0xFF1A1A00),
    ),
    Activity(
        label = "Activity",
        icon = Icons.Default.SportsKabaddi,
        color = Color(0xFFC8961C),   // chart-5 amber
        onColor = Color(0xFF1A0E00),
    ),
}

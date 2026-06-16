package com.tripplanner.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tripplanner.android.R

enum class TripCategory(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val color: Color,
    val onColor: Color = Color.White,
) {
    Food(
        labelRes = R.string.category_food,
        icon = Icons.Default.Restaurant,
        color = Color(0xFFE8622C),   // chart-1 orange
    ),
    Sightseeing(
        labelRes = R.string.category_sightseeing,
        icon = Icons.Default.PhotoCamera,
        color = Color(0xFF1AAD9A),   // chart-2 teal
    ),
    Transport(
        labelRes = R.string.category_transport,
        icon = Icons.Default.DirectionsBus,
        color = Color(0xFF2B5C8B),   // chart-3 dark blue
    ),
    Accommodation(
        labelRes = R.string.category_accommodation,
        icon = Icons.Default.Hotel,
        color = Color(0xFFCDB820),   // chart-4 yellow
        onColor = Color(0xFF1A1A00),
    ),
    Activity(
        labelRes = R.string.category_activity,
        icon = Icons.Default.SportsKabaddi,
        color = Color(0xFFC8961C),   // chart-5 amber
        onColor = Color(0xFF1A0E00),
    ),
}

package com.tripplanner.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Base radius 10 dp ≈ 0.625 rem from --radius.
// Scale mirrors the CSS radius-sm/md/lg/xl aliases.
val TripShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // --radius-sm: radius - 4px
    small = RoundedCornerShape(8.dp),        // --radius-md: radius - 2px
    medium = RoundedCornerShape(10.dp),      // --radius-lg: radius
    large = RoundedCornerShape(14.dp),       // --radius-xl: radius + 4px
    extraLarge = RoundedCornerShape(20.dp),
)

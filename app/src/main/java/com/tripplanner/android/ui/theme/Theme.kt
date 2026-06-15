package com.tripplanner.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightPrimaryForeground,
    primaryContainer = LightSecondary,
    onPrimaryContainer = LightSecondaryForeground,
    secondary = LightAccent,
    onSecondary = LightAccentForeground,
    secondaryContainer = LightMuted,
    onSecondaryContainer = LightForeground,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightCard,
    onSurface = LightCardForeground,
    surfaceVariant = LightMuted,
    onSurfaceVariant = LightMutedForeground,
    error = LightDestructive,
    onError = LightDestructiveForeground,
    outline = LightBorder,
    outlineVariant = LightSwitchBackground,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkPrimaryForeground,
    primaryContainer = DarkSecondary,
    onPrimaryContainer = DarkSecondaryForeground,
    secondary = DarkAccent,
    onSecondary = DarkAccentForeground,
    secondaryContainer = DarkMuted,
    onSecondaryContainer = DarkForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkCardForeground,
    surfaceVariant = DarkMuted,
    onSurfaceVariant = DarkMutedForeground,
    error = DarkDestructive,
    onError = DarkDestructiveForeground,
    outline = DarkBorder,
    outlineVariant = DarkRing,
)

data class TripExtendedColors(
    val inputBackground: Color,
    val chart1: Color,
    val chart2: Color,
    val chart3: Color,
    val chart4: Color,
    val chart5: Color,
)

val LocalTripColors = staticCompositionLocalOf {
    TripExtendedColors(
        inputBackground = LightInputBackground,
        chart1 = Chart1, chart2 = Chart2, chart3 = Chart3, chart4 = Chart4, chart5 = Chart5,
    )
}

@Composable
fun TripPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extended = if (darkTheme) {
        TripExtendedColors(
            inputBackground = DarkInputBackground,
            chart1 = DarkChart1, chart2 = DarkChart2, chart3 = DarkChart3,
            chart4 = DarkChart4, chart5 = DarkChart5,
        )
    } else {
        TripExtendedColors(
            inputBackground = LightInputBackground,
            chart1 = Chart1, chart2 = Chart2, chart3 = Chart3, chart4 = Chart4, chart5 = Chart5,
        )
    }

    CompositionLocalProvider(LocalTripColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = TripShapes,
            content = content,
        )
    }
}

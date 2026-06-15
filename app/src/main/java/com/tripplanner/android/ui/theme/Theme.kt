package com.tripplanner.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = OnInk,
    background = Surface,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = MutedLight,
    onSurfaceVariant = MutedFg,
    error = Destructive,
)

private val DarkColors = darkColorScheme(
    primary = OnInk,
    onPrimary = Ink,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkMuted,
    error = Destructive,
)

@Composable
fun TripPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

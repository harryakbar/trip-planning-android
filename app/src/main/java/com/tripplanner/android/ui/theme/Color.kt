package com.tripplanner.android.ui.theme

import androidx.compose.ui.graphics.Color

// ── Light tokens (ported from :root in theme.css) ───────────────────────────

val LightBackground = Color(0xFFFFFFFF)
val LightForeground = Color(0xFF0A0A0A)
val LightCard = Color(0xFFFFFFFF)
val LightCardForeground = Color(0xFF0A0A0A)
val LightPrimary = Color(0xFF030213)
val LightPrimaryForeground = Color(0xFFFFFFFF)
val LightSecondary = Color(0xFFEDEEF2)     // oklch(0.95 0.0058 264.53)
val LightSecondaryForeground = Color(0xFF030213)
val LightMuted = Color(0xFFECECF0)         // --muted
val LightMutedForeground = Color(0xFF717182)
val LightAccent = Color(0xFFE9EBEF)        // --accent
val LightAccentForeground = Color(0xFF030213)
val LightDestructive = Color(0xFFD4183D)
val LightDestructiveForeground = Color(0xFFFFFFFF)
val LightBorder = Color(0x1A000000)        // rgba(0,0,0,0.1)
val LightInputBackground = Color(0xFFF3F3F5)
val LightSwitchBackground = Color(0xFFCBCED4)
val LightRing = Color(0xFFA1A1A1)          // oklch(0.708 0 0)

// ── Dark tokens (ported from .dark in theme.css) ─────────────────────────────

val DarkBackground = Color(0xFF1C1C1C)     // oklch(0.145 0 0)
val DarkForeground = Color(0xFFFAFAFA)     // oklch(0.985 0 0)
val DarkCard = Color(0xFF1C1C1C)
val DarkCardForeground = Color(0xFFFAFAFA)
val DarkPrimary = Color(0xFFFAFAFA)        // inverted in dark mode
val DarkPrimaryForeground = Color(0xFF171717)  // oklch(0.205 0 0)
val DarkSecondary = Color(0xFF262626)      // oklch(0.269 0 0)
val DarkSecondaryForeground = Color(0xFFFAFAFA)
val DarkMuted = Color(0xFF262626)
val DarkMutedForeground = Color(0xFFA1A1A1)   // oklch(0.708 0 0)
val DarkAccent = Color(0xFF262626)
val DarkAccentForeground = Color(0xFFFAFAFA)
val DarkDestructive = Color(0xFF7F1D1D)
val DarkDestructiveForeground = Color(0xFFFCA5A5)
val DarkBorder = Color(0xFF262626)
val DarkInputBackground = Color(0xFF262626)
val DarkRing = Color(0xFF525252)           // oklch(0.439 0 0)

// ── Chart palette — light ─────────────────────────────────────────────────────

val Chart1 = Color(0xFFE8622C)   // oklch(0.646 0.222 41.116) — orange
val Chart2 = Color(0xFF1AAD9A)   // oklch(0.600 0.118 184.704) — teal
val Chart3 = Color(0xFF2B5C8B)   // oklch(0.398 0.070 227.392) — dark blue
val Chart4 = Color(0xFFCDB820)   // oklch(0.828 0.189  84.429) — yellow
val Chart5 = Color(0xFFC8961C)   // oklch(0.769 0.188  70.080) — amber

// ── Chart palette — dark ──────────────────────────────────────────────────────

val DarkChart1 = Color(0xFF4B6BF5)   // oklch(0.488 0.243 264.376) — blue-purple
val DarkChart2 = Color(0xFF2DB88A)   // oklch(0.696 0.170 162.480) — green
val DarkChart3 = Color(0xFFC8961C)   // oklch(0.769 0.188  70.080) — amber
val DarkChart4 = Color(0xFF9B4BD6)   // oklch(0.627 0.265 303.900) — purple
val DarkChart5 = Color(0xFFE05050)   // oklch(0.645 0.246  16.439) — red-orange

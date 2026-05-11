package com.rooknews.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.rooknews.domain.model.ReadingMode

/**
 * "Warm Paper" design system — RookNews color palette.
 *
 * Three reading modes: Light, Sepia, Dark.
 * Amber accent for AI indicators and interactive elements.
 */

// -- Accent (shared across all modes) --
val Amber50 = Color(0xFFFFFBEB)
val Amber100 = Color(0xFFFEEEBA)
val Amber200 = Color(0xFFFED68D)
val Amber300 = Color(0xFFFDB84E)
val Amber400 = Color(0xFFF79009)
val Amber500 = Color(0xFFFC7900)
val Amber600 = Color(0xFFCA5900)
val Amber700 = Color(0xFF994409)
val Amber800 = Color(0xFF76340A)
val Amber900 = Color(0xFF5B2709)

// -- Light mode --
object LightColors {
    val background = Color(0xFFFFFAF5)       // warm white
    val surface = Color(0xFFFFFFFF)          // pure white cards
    val surfaceVariant = Color(0xFFF5F0EB)   // subtle elevation
    val onBackground = Color(0xFF1A1A1A)     // near-black text
    val onSurface = Color(0xFF2D2D2D)        // body text
    val onSurfaceVariant = Color(0xFF6B6B6B) // secondary text
    val primary = Amber600
    val primaryContainer = Amber100
    val outline = Color(0xFFE0D8CF)
    val outlineVariant = Color(0xFFF0EBE4)
    val error = Color(0xFFB3261E)
    val success = Color(0xFF2E7D32)
    val biasLow = Color(0xFF2E7D32)
    val biasMedium = Color(0xFFF57F17)
    val biasHigh = Color(0xFFB3261E)
}

// -- Sepia mode --
object SepiaColors {
    val background = Color(0xFFF5ECD7)       // parchment
    val surface = Color(0xFFFDF6E3)          // cream cards
    val surfaceVariant = Color(0xFFEDE0C8)   // subtle elevation
    val onBackground = Color(0xFF433422)     // dark brown text
    val onSurface = Color(0xFF5C4B37)        // body text
    val onSurfaceVariant = Color(0xFF8B7355) // secondary text
    val primary = Amber700
    val primaryContainer = Amber200
    val outline = Color(0xFFD4C4A8)
    val outlineVariant = Color(0xFFE8DCC4)
    val error = Color(0xFF8B3A2B)
    val success = Color(0xFF3E6B34)
    val biasLow = Color(0xFF3E6B34)
    val biasMedium = Color(0xFFD4880F)
    val biasHigh = Color(0xFF8B3A2B)
}

// -- Dark mode --
object DarkColors {
    val background = Color(0xFF1A1A1A)       // dark gray
    val surface = Color(0xFF242424)          // slightly lighter cards
    val surfaceVariant = Color(0xFF2E2E2E)   // subtle elevation
    val onBackground = Color(0xFFE8E0D8)     // warm light text
    val onSurface = Color(0xFFD4CCC4)        // body text
    val onSurfaceVariant = Color(0xFF9B9490) // secondary text
    val primary = Amber400
    val primaryContainer = Amber900
    val outline = Color(0xFF3E3E3E)
    val outlineVariant = Color(0xFF333333)
    val error = Color(0xFFEF5350)
    val success = Color(0xFF66BB6A)
    val biasLow = Color(0xFF66BB6A)
    val biasMedium = Color(0xFFFFA726)
    val biasHigh = Color(0xFFEF5350)
}

/**
 * Resolve colors for a given reading mode.
 */
fun colorsFor(mode: ReadingMode): ColorPalette = when (mode) {
    ReadingMode.LIGHT -> LightColorPalette
    ReadingMode.SEPIA -> SepiaColorPalette
    ReadingMode.DARK -> DarkColorPalette
}

/**
 * Resolved color palette for current theme.
 */
data class ColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val primaryContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val success: Color,
    val biasLow: Color,
    val biasMedium: Color,
    val biasHigh: Color,
)

val LightColorPalette = ColorPalette(
    background = LightColors.background,
    surface = LightColors.surface,
    surfaceVariant = LightColors.surfaceVariant,
    onBackground = LightColors.onBackground,
    onSurface = LightColors.onSurface,
    onSurfaceVariant = LightColors.onSurfaceVariant,
    primary = LightColors.primary,
    primaryContainer = LightColors.primaryContainer,
    outline = LightColors.outline,
    outlineVariant = LightColors.outlineVariant,
    error = LightColors.error,
    success = LightColors.success,
    biasLow = LightColors.biasLow,
    biasMedium = LightColors.biasMedium,
    biasHigh = LightColors.biasHigh,
)

val SepiaColorPalette = ColorPalette(
    background = SepiaColors.background,
    surface = SepiaColors.surface,
    surfaceVariant = SepiaColors.surfaceVariant,
    onBackground = SepiaColors.onBackground,
    onSurface = SepiaColors.onSurface,
    onSurfaceVariant = SepiaColors.onSurfaceVariant,
    primary = SepiaColors.primary,
    primaryContainer = SepiaColors.primaryContainer,
    outline = SepiaColors.outline,
    outlineVariant = SepiaColors.outlineVariant,
    error = SepiaColors.error,
    success = SepiaColors.success,
    biasLow = SepiaColors.biasLow,
    biasMedium = SepiaColors.biasMedium,
    biasHigh = SepiaColors.biasHigh,
)

val DarkColorPalette = ColorPalette(
    background = DarkColors.background,
    surface = DarkColors.surface,
    surfaceVariant = DarkColors.surfaceVariant,
    onBackground = DarkColors.onBackground,
    onSurface = DarkColors.onSurface,
    onSurfaceVariant = DarkColors.onSurfaceVariant,
    primary = DarkColors.primary,
    primaryContainer = DarkColors.primaryContainer,
    outline = DarkColors.outline,
    outlineVariant = DarkColors.outlineVariant,
    error = DarkColors.error,
    success = DarkColors.success,
    biasLow = DarkColors.biasLow,
    biasMedium = DarkColors.biasMedium,
    biasHigh = DarkColors.biasHigh,
)

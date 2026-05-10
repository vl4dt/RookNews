package com.rooknews.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.rooknews.domain.model.ReadingMode

/**
 * RookNews theme holder.
 * Provides colors and typography throughout the app.
 */

private val LocalRookNewsColors = staticCompositionLocalOf { LightColorPalette }
private val LocalReadingMode = staticCompositionLocalOf { ReadingMode.LIGHT }

/**
 * Main theme composable. Wrap your app content with this.
 */
@Composable
fun RookNewsTheme(
    readingMode: ReadingMode = ReadingMode.LIGHT,
    content: @Composable () -> Unit
) {
    val colors = colorsFor(readingMode)

    CompositionLocalProvider(
        LocalRookNewsColors provides colors,
        LocalReadingMode provides readingMode,
        content = content
    )
}

/**
 * Access current color palette.
 */
object RookNewsTheme {
    val colors: ColorPalette
        @Composable get() = LocalRookNewsColors.current

    val readingMode: ReadingMode
        @Composable get() = LocalReadingMode.current

    val typography: RookNewsTypography
        @Composable @ReadOnlyComposable get() = RookNewsTypography
}

package com.gkk.mppsc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

// ── Composition locals so any composable can access GKK-specific colors ──
val LocalGKKColors = staticCompositionLocalOf { gkkColorsFor(GKKTheme.DEFAULT) }
val LocalGKKTheme  = staticCompositionLocalOf { GKKTheme.DEFAULT }

@Composable
fun GKKThemeWrapper(
    theme:   GKKTheme  = GKKTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val gkkColors   = gkkColorsFor(theme)
    val colorScheme = gkkColors.toColorScheme()

    CompositionLocalProvider(
        LocalGKKColors provides gkkColors,
        LocalGKKTheme  provides theme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = GKKTypography,
            content     = content
        )
    }
}

/** Shortcut to access GKK colours from any composable */
val gkkColors: GKKColors
    @Composable get() = LocalGKKColors.current

package com.noteapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colorfulScheme = lightColorScheme(
    primary = accentColor,
    background = backgroundColorful,
    surface = surfaceColorful,
    onSurface = onSurfaceColorful,
    onSurfaceVariant = onSurfaceVariantColorful,
    secondary = Color(0xFF6366F1)
)

// Reserved: full dark palette to be implemented when DARK mode is built
private val darkScheme = darkColorScheme(
    primary = accentColor
)

@Composable
fun AppTheme(
    mode: ThemeMode = ThemeMode.COLORFUL,
    content: @Composable () -> Unit
) {
    val colorScheme = when (mode) {
        ThemeMode.COLORFUL -> colorfulScheme
        ThemeMode.DARK -> darkScheme  // placeholder — returns default dark scheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CalmGreenPrimaryDark,
    onPrimary = CalmGreenOnPrimaryDark,
    secondary = DifficultRedSecondaryDark,
    onSecondary = DifficultRedOnSecondaryDark,
    background = JournalBackgroundDark,
    onBackground = JournalTextDark,
    surface = JournalSurfaceDark,
    onSurface = JournalTextDark,
    outline = GrayOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CalmGreenPrimary,
    onPrimary = CalmGreenOnPrimary,
    secondary = DifficultRedSecondary,
    onSecondary = DifficultRedOnSecondary,
    background = JournalBackgroundLight,
    onBackground = JournalTextLight,
    surface = JournalSurfaceLight,
    onSurface = JournalTextLight,
    outline = GrayOutlineLight
)

@Composable
fun BetterThanYesterdayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

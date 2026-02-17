package com.catchingclouds.marstimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = White,
    tertiary = White,
    background = Black,
    surface = Black,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    secondary = Black,
    tertiary = Black,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun MarsTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our style
    content: @Composable () -> Unit
) {
    // Always use DarkColorScheme for "Mars Launcher" look (Black background)
    // Or we can support light mode if desired, but user screenshot shows pure black.
    // Let's stick to the requested "look" which implies the dark aesthetic.
    // However, to be safe, I'll allow system toggle but default to the Dark scheme if unsure,
    // OR just force DarkColorScheme since the requirement is "orientieren an Mars Launcher" which is black.
    // I will force the dark scheme content for the "Mars" look.

    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.jimmy.photocleaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFFB7299), // B站粉
    secondary = Color(0xFFFFA5C3),
    tertiary = Color(0xFFF04E83),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFAFAFA),
    error = Color(0xFFB00020)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFB7299), // B站粉
    secondary = Color(0xFFFFA5C3),
    tertiary = Color(0xFFF04E83),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Color(0xFFCF6679)
)

@Composable
fun PhotoCleanerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
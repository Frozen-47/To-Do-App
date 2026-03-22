package com.example.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    secondary = LightGray,
    background = PureBlack,
    surface = DarkGray,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = PureBlack,
    onSurfaceVariant = MidGray
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    secondary = DarkGray,
    background = PureWhite,
    surface = LightGray,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
    surfaceVariant = PureWhite,
    onSurfaceVariant = MidGray
)

@Composable
fun TodoTheme(
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
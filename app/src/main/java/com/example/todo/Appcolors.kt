package com.example.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val bg: Color,
    val fg: Color,
    val mutedFg: Color,
    val border: Color,
)

@Composable
fun rememberAppColors(): AppColors {
    val isDark = isSystemInDarkTheme()
    return remember(isDark) {
        if (isDark) {
            AppColors(
                bg = Color(0xFF000000),
                fg = Color(0xFFFFFFFF),
                mutedFg = Color(0xFF888888),
                border = Color(0xFF222222),
            )
        } else {
            AppColors(
                bg = Color(0xFFFFFFFF),
                fg = Color(0xFF000000),
                mutedFg = Color(0xFF666666),
                border = Color(0xFFE0E0E0),
            )
        }
    }
}
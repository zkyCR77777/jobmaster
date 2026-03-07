package com.example.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AppPrimary,
    onPrimary = White,
    secondary = Blue500,
    onSecondary = White,
    tertiary = Emerald500,
    onTertiary = White,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = Slate100,
    outline = AppBorder,
    error = AppDanger,
    onError = White,
)

private val DarkColors = darkColorScheme(
    primary = Blue400,
    onPrimary = Slate900,
    secondary = Teal400,
    onSecondary = Slate900,
    tertiary = Amber500,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = White,
    surface = Slate800,
    onSurface = White,
    outline = Slate600,
    error = Red400,
    onError = Slate900,
)

@Composable
fun ClientTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme && dynamicColor) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

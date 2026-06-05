package com.example.spin_and_earn_money.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C1FC9),
    onPrimary = Color(0xFFF0F0FF),
    primaryContainer = Color(0xFF2D1050),
    onPrimaryContainer = Color(0xFFE0C8FF),
    secondary = Color(0xFFFFD700),
    onSecondary = Color(0xFF0A0A1A),
    secondaryContainer = Color(0xFF3A2D00),
    onSecondaryContainer = Color(0xFFFFE87A),
    tertiary = Color(0xFFE91E8C),
    onTertiary = Color(0xFFF0F0FF),
    background = Color(0xFF0A0A1A),
    onBackground = Color(0xFFF0F0FF),
    surface = Color(0xFF1A1A2E),
    onSurface = Color(0xFFF0F0FF),
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFF8888AA),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF0A0A1A),
    outline = Color(0xFF3A3A5A)
)

@Composable
fun Spin_and_earn_moneyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography,
        content = content
    )
}
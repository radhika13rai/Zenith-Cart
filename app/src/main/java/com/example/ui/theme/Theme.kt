package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CoralLight,
    onPrimary = SlateNavyDeep,
    primaryContainer = CoralDark,
    onPrimaryContainer = Color.White,
    secondary = AmberSecondary,
    onSecondary = SlateNavyDeep,
    secondaryContainer = OnAmberContainer,
    onSecondaryContainer = AmberLight,
    tertiary = CoralPrimary,
    onTertiary = Color.White,
    background = SlateNavyDeep,
    onBackground = SurfaceCloudLight,
    surface = SlateNavySurface,
    onSurface = SurfaceCloudLight,
    surfaceVariant = SlateNavyDark,
    onSurfaceVariant = Color(0xFFC7C9D6),
    outline = Color(0xFF4A4E68)
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnCoralContainer,
    secondary = AmberSecondary,
    onSecondary = SlateNavyDark,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = SlateNavyDark,
    onTertiary = Color.White,
    background = SurfaceCloudLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFECECF2),
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorder
)

@Composable
fun OmniCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Preserve custom branding colors across all devices
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

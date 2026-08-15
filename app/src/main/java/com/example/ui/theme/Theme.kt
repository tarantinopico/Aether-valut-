package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetherDarkColorScheme = darkColorScheme(
    primary = NeonIndigo,
    onPrimary = Color.Black,
    primaryContainer = NeonIndigoDim,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = ElectricCyanDim,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonRose,
    onTertiary = Color.Black,
    background = AetherVoid,
    onBackground = TextPrimary,
    surface = AetherSurfaceDeep,
    onSurface = TextPrimary,
    surfaceVariant = AetherSurfaceContainer,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = AetherSurfaceContainer,
    surfaceContainerHigh = AetherSurfaceContainerHigh,
    outline = AetherBorderGlass,
    outlineVariant = AetherBorderSubtle
)

@Composable
fun AetherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetherDarkColorScheme,
        typography = Typography,
        content = content
    )
}

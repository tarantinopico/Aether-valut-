package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val AetherVoid = Color(0xFF070A10)
val AetherSurfaceDeep = Color(0xFF0D121F)
val AetherSurfaceContainer = Color(0xFF131A2B)
val AetherSurfaceContainerHigh = Color(0xFF1B243B)
val AetherSurfaceGlass = Color(0x99131A2B)
val AetherBorderGlass = Color(0x336366F1)
val AetherBorderSubtle = Color(0x22FFFFFF)

val NeonIndigo = Color(0xFF818CF8)
val NeonIndigoDim = Color(0xFF4F46E5)
val ElectricCyan = Color(0xFF22D3EE)
val ElectricCyanDim = Color(0xFF0891B2)
val NeonRose = Color(0xFFFB7185)
val NeonEmerald = Color(0xFF34D399)
val NeonAmber = Color(0xFFFBBF24)
val NeonViolet = Color(0xFFA78BFA)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Color helpers for tags & types
fun parseColorHex(hex: String?, fallback: Color = NeonIndigo): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> "FF$clean".toLong(16)
            8 -> clean.toLong(16)
            else -> return fallback
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

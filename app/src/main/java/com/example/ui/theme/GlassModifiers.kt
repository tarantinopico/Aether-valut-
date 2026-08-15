package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x18FFFFFF),
    borderColor: Color = Color(0x28FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(borderWidth, borderColor, shape)

fun Modifier.glassCardGradient(
    shape: Shape = RoundedCornerShape(18.dp),
    startColor: Color = Color(0x221E293B),
    endColor: Color = Color(0x120F172A),
    borderColor: Color = Color(0x30FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(startColor, endColor)
        )
    )
    .border(borderWidth, borderColor, shape)

fun Modifier.glowingPill(
    accentColor: Color,
    shape: Shape = RoundedCornerShape(100.dp)
): Modifier = this
    .clip(shape)
    .background(accentColor.copy(alpha = 0.15f))
    .border(1.dp, accentColor.copy(alpha = 0.35f), shape)

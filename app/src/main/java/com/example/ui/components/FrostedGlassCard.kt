package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceGlass

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = AetherSurfaceGlass,
    borderColor: Color = AetherBorderGlass,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .border(
                border = BorderStroke(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        listOf(borderColor, AetherBorderSubtle, borderColor.copy(alpha = 0.1f))
                    )
                ),
                shape = shape
            )
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        tonalElevation = 4.dp
    ) {
        Box(content = content)
    }
}

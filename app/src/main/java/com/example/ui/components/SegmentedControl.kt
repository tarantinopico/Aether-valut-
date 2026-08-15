package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.GlassWhite20
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class CalendarViewMode {
    MONTH,
    WEEK
}

@Composable
fun SegmentedCalendarViewToggle(
    currentMode: CalendarViewMode,
    onModeSelected: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassWhite10)
            .padding(3.dp)
            .testTag("calendar_view_segmented_toggle")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarViewMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                val targetBg = if (isSelected) CyanAccent else Color.Transparent
                val targetText = if (isSelected) ObsidianBlack else TextMuted
                val animatedBg by animateColorAsState(
                    targetValue = targetBg,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "segment_bg"
                )
                val animatedText by animateColorAsState(
                    targetValue = targetText,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "segment_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(animatedBg)
                        .clickable { onModeSelected(mode) }
                        .testTag("segment_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mode == CalendarViewMode.MONTH) "Month" else "Week",
                        color = animatedText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

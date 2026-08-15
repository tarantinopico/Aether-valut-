package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.domain.model.CalendarEvent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun CalendarWeekRibbon(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val mondayOfSelectedWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { mondayOfSelectedWeek.plusDays(it.toLong()) }
    val eventsByDate = events.groupBy { it.startDate }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SlateCard.copy(alpha = 0.5f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(20.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .testTag("calendar_week_ribbon")
    ) {
        // Top Week Navigator Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("week_prev_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Week",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "${mondayOfSelectedWeek.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${mondayOfSelectedWeek.dayOfMonth} - ${weekDays.last().month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${weekDays.last().dayOfMonth}, ${weekDays.last().year}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = onNextWeek,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("week_next_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Week",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weekday Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                val dayEvents = eventsByDate[date] ?: emptyList()

                val bgColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> CyanAccent
                        isToday -> GlassWhite10
                        else -> Color.Transparent
                    },
                    label = "week_day_bg"
                )

                val textColor = when {
                    isSelected -> ObsidianBlack
                    isToday -> CyanAccent
                    else -> TextPrimary
                }

                val subTextColor = when {
                    isSelected -> ObsidianBlack.copy(alpha = 0.8f)
                    else -> TextMuted
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .then(
                            if (isToday && !isSelected) {
                                Modifier.border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            } else Modifier
                        )
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 8.dp)
                        .testTag("week_day_${date}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase().take(3),
                            color = subTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Event Load Dots
                        if (dayEvents.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                dayEvents.take(3).forEach { ev ->
                                    val dotColor = parseHexColor(ev.categoryColor, if (isSelected) ObsidianBlack else CyanAccent)
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ObsidianBlack.copy(alpha = 0.8f) else dotColor)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalendarEvent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    
    // Day of week index (Monday = 1, Sunday = 7)
    val startDayOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    val prevMonth = yearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    // Map events by date for quick lookup
    val eventsByDate = events.groupBy { it.startDate }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SlateCard.copy(alpha = 0.5f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(20.dp))
            .padding(12.dp)
            .testTag("calendar_month_view")
    ) {
        // Weekday header (Mon..Sun)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            weekDayLabels.forEach { label ->
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total cells in 6 rows x 7 cols
        val totalCells = 42
        val weeks = totalCells / 7

        for (week in 0 until weeks) {
            // Check if entire week belongs to next month to avoid extra empty row
            val firstDayOfThisWeekIndex = week * 7
            if (firstDayOfThisWeekIndex - startDayOffset >= daysInMonth && week >= 5) {
                break
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayNumber = cellIndex - startDayOffset + 1

                    val (date, isCurrentMonth) = when {
                        dayNumber < 1 -> {
                            val prevDate = prevMonth.atDay(daysInPrevMonth + dayNumber)
                            Pair(prevDate, false)
                        }
                        dayNumber > daysInMonth -> {
                            val nextMonth = yearMonth.plusMonths(1)
                            val nextDate = nextMonth.atDay(dayNumber - daysInMonth)
                            Pair(nextDate, false)
                        }
                        else -> {
                            Pair(yearMonth.atDay(dayNumber), true)
                        }
                    }

                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val dayEvents = eventsByDate[date] ?: emptyList()

                    DayCell(
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isSelected,
                        isToday = isToday,
                        events = dayEvents,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> CyanAccent
            isToday -> GlassWhite10
            else -> Color.Transparent
        },
        label = "day_cell_bg"
    )

    val textColor = when {
        isSelected -> ObsidianBlack
        !isCurrentMonth -> TextMuted.copy(alpha = 0.4f)
        isToday -> CyanAccent
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, CyanAccent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                } else Modifier
            )
            .clickable { onClick() }
            .testTag("day_cell_${date}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )

            // Event Dots (up to 3 distinct category dots)
            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayEvents = events.take(3)
                    displayEvents.forEach { ev ->
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
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

fun parseHexColor(hex: String?, fallback: Color = IndigoAccent): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

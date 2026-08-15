package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalendarEvent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DayAgendaList(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayEvents = events.filter { it.startDate == selectedDate }
    val formattedDateHeader = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_agenda_panel")
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Agenda",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$formattedDateHeader · ${dayEvents.size} ${if (dayEvents.size == 1) "Event" else "Events"}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onAddEventClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape)
                    .testTag("add_event_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Event",
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (dayEvents.isEmpty()) {
            EmptyAgendaState(
                selectedDate = selectedDate,
                onAddEventClick = onAddEventClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayEvents, key = { it.id }) { event ->
                    AgendaEventCard(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaEventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    val catColor = parseHexColor(event.categoryColor, IndigoAccent)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCard.copy(alpha = 0.7f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("agenda_event_card_${event.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Category Color Bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(catColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = event.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Category pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(catColor.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.category,
                        color = catColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = event.formattedTimeSpan,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Linked Note Badge
                if (!event.linkedNoteTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GlassWhite10)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = event.linkedNoteTitle,
                            color = CyanAccent,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAgendaState(
    selectedDate: LocalDate,
    onAddEventClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SlateElevated.copy(alpha = 0.4f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(18.dp))
            .padding(24.dp)
            .testTag("empty_agenda_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No events scheduled",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Keep track of meetings, deadlines, and daily goals.",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            ElevatedButton(
                onClick = onAddEventClick,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = CyanAccent,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("empty_add_event_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Schedule Event",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NoteEntity
import com.example.ui.components.AddEventBottomSheet
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceContainerHigh
import com.example.ui.theme.AetherSurfaceDeep
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseColorHex
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEventSheet by remember { mutableStateOf(false) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayHeaderFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AetherVoid,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventSheet = true },
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Event",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Calendar Top Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Aether Calendar",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = dayHeaderFormat.format(uiState.selectedDate),
                            fontSize = 13.sp,
                            color = ElectricCyan
                        )
                    }

                    // Daily Note Shortcut
                    Button(
                        onClick = { viewModel.openOrCreateDailyNote(onNavigateToNote) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonIndigo.copy(alpha = 0.2f),
                            contentColor = NeonIndigo
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("daily_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Daily Note",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Daily Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Month Navigation Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = TextSecondary
                        )
                    }

                    Text(
                        text = monthYearFormat.format(uiState.currentMonth.time),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Month Grid
            MonthCalendarGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                eventDays = uiState.eventDaysInMonth,
                onDateSelected = { viewModel.setSelectedDate(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Agenda / Events List for Selected Date
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(AetherSurfaceDeep)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${uiState.selectedDateEvents.size} linked items",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.selectedDateEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "No events",
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No events scheduled for this day",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddEventSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan.copy(alpha = 0.15f),
                                    contentColor = ElectricCyan
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ Schedule Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        items(uiState.selectedDateEvents, key = { it.id }) { eventNote ->
                            EventAgendaCard(
                                eventNote = eventNote,
                                onClick = { onNavigateToNote(eventNote.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddEventSheet) {
        AddEventBottomSheet(
            initialDate = uiState.selectedDate,
            onDismiss = { showAddEventSheet = false },
            onCreateEvent = { title, startIso, endIso, isAllDay, location, colorHex, tags ->
                showAddEventSheet = false
                viewModel.createEvent(
                    title = title,
                    startIso = startIso,
                    endIso = endIso,
                    isAllDay = isAllDay,
                    location = location,
                    colorHex = colorHex,
                    tags = tags,
                    onCreated = onNavigateToNote
                )
            }
        )
    }
}

@Composable
fun MonthCalendarGrid(
    currentMonth: Calendar,
    selectedDate: Date,
    eventDays: Set<String>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { dayKeyFormat.format(Date()) }
    val selectedStr = remember(selectedDate) { dayKeyFormat.format(selectedDate) }

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    val cal = currentMonth.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sunday
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = AetherSurfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar weeks
            var currentDay = 1
            val totalCells = ((firstDayOfWeek + maxDaysInMonth + 6) / 7) * 7

            for (week in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (dayCol in 0..6) {
                        val cellIndex = week * 7 + dayCol
                        if (cellIndex < firstDayOfWeek || currentDay > maxDaysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val dayNumber = currentDay
                            val dateCal = cal.clone() as Calendar
                            dateCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                            val thisDayStr = dayKeyFormat.format(dateCal.time)
                            val isSelected = thisDayStr == selectedStr
                            val isToday = thisDayStr == todayStr
                            val hasEvents = eventDays.contains(thisDayStr)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> ElectricCyan.copy(alpha = 0.2f)
                                            isToday -> NeonIndigo.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) ElectricCyan else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDateSelected(dateCal.time) }
                                    .testTag("cal_day_$thisDayStr"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> ElectricCyan
                                            isToday -> NeonIndigo
                                            else -> TextPrimary
                                        }
                                    )
                                    if (hasEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) ElectricCyan else NeonRose)
                                        )
                                    }
                                }
                            }
                            currentDay++
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun EventAgendaCard(
    eventNote: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHex(eventNote.colorHex, fallback = ElectricCyan)
    val timeLabel = if (eventNote.isAllDay) {
        "All Day"
    } else {
        val start = eventNote.eventStart?.substringAfter("T")?.take(5) ?: "09:00"
        val end = eventNote.eventEnd?.substringAfter("T")?.take(5) ?: ""
        if (end.isNotBlank()) "$start - $end" else start
    }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agenda_event_${eventNote.id}"),
        backgroundColor = AetherSurfaceContainerHigh,
        borderColor = accentColor.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Event Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventNote.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (!eventNote.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = eventNote.location,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Note",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

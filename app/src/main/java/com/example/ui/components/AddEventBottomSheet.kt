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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseColorHex
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventBottomSheet(
    initialDate: Date = Date(),
    onDismiss: () -> Unit,
    onCreateEvent: (title: String, startIso: String, endIso: String?, isAllDay: Boolean, location: String?, colorHex: String, tags: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tagsString by remember { mutableStateOf("event, meeting") }
    var isAllDay by remember { mutableStateOf(false) }

    val dateFormater = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    var selectedDateString by remember { mutableStateOf(dateFormater.format(initialDate)) }
    var startHour by remember { mutableStateOf("10:00") }
    var endHour by remember { mutableStateOf("11:00") }

    val colorOptions = listOf("#6366F1", "#06B6D4", "#EC4899", "#10B981", "#F59E0B", "#8B5CF6")
    var selectedColorHex by remember { mutableStateOf(colorOptions[1]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AetherSurfaceContainer,
        modifier = modifier.testTag("add_event_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Calendar Event",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Will create an event note with full Markdown frontmatter",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g. Design Review Sync") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date & All Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedDateString,
                    onValueChange = { selectedDateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = ElectricCyan
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = AetherBorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_date_input")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "All day", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isAllDay,
                        onCheckedChange = { isAllDay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            if (!isAllDay) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Start Time (HH:mm)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Start",
                                tint = NeonIndigo
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = AetherBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("End Time (HH:mm)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "End",
                                tint = NeonIndigo
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = AetherBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Link") },
                placeholder = { Text("e.g. Conference Room A or Zoom link") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = NeonRose
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonRose,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color picker
            Text(text = "Event Color", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colorOptions.forEach { hex ->
                    val color = parseColorHex(hex)
                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            OutlinedTextField(
                value = tagsString,
                onValueChange = { tagsString = it },
                label = { Text("Tags (comma separated)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Create button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val startIso = if (isAllDay) {
                            "${selectedDateString}T00:00:00Z"
                        } else {
                            val time = if (startHour.contains(":")) startHour else "10:00"
                            "${selectedDateString}T${time}:00Z"
                        }

                        val endIso = if (isAllDay) {
                            "${selectedDateString}T23:59:59Z"
                        } else {
                            val time = if (endHour.contains(":")) endHour else "11:00"
                            "${selectedDateString}T${time}:00Z"
                        }

                        val tags = tagsString.split(",")
                            .map { it.trim().removePrefix("#") }
                            .filter { it.isNotBlank() }

                        onCreateEvent(
                            title.trim(),
                            startIso,
                            endIso,
                            isAllDay,
                            location.trim().ifBlank { null },
                            selectedColorHex,
                            tags
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_create_event_button")
            ) {
                Text("Create Event Note", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

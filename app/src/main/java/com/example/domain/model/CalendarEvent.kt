package com.example.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val noteId: String? = null,
    val title: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val isAllDay: Boolean = false,
    val category: String = "General",
    val categoryColor: String = "#6366F1", // Indigo default
    val linkedNoteTitle: String? = null,
    val noteSnippet: String? = null
) {
    val startDate: LocalDate
        get() = startDateTime.toLocalDate()

    val formattedTimeSpan: String
        get() {
            if (isAllDay) return "All Day"
            val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
            return "${startDateTime.format(timeFmt)} - ${endDateTime.format(timeFmt)}"
        }
}

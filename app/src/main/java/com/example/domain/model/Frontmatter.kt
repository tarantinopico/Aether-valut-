package com.example.domain.model

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Frontmatter(
    val id: String = UUID.randomUUID().toString(),
    val type: NoteType = NoteType.TEXT,
    val title: String = "Untitled Note",
    val created: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    val updated: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    val tags: List<String> = emptyList(),
    val folder: String = "",
    val links: List<String> = emptyList(),
    // Bookmark specific fields
    val url: String? = null,
    val previewImage: String? = null,
    val domain: String? = null,
    val summary: String? = null,
    // Event specific fields
    val startDateTime: String? = null,
    val endDateTime: String? = null,
    val isAllDay: Boolean? = false,
    val category: String? = null,
    val categoryColor: String? = null,
    // Database specific fields
    val columns: List<String> = emptyList(),
    val rowCount: Int = 0
)

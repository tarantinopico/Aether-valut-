package com.example.domain.model

import java.util.UUID

enum class NoteType(val rawValue: String, val displayName: String) {
    TEXT("text", "Note"),
    BOOKMARK("bookmark", "Bookmark"),
    DATABASE("database", "Database"),
    EVENT("event", "Event");

    companion object {
        fun fromString(value: String?): NoteType {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: TEXT
        }
    }
}

data class NoteEntity(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled",
    val type: NoteType = NoteType.TEXT,
    val createdAt: String = "",
    val updatedAt: String = "",
    val tags: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val eventStart: String? = null,
    val eventEnd: String? = null,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val bodyContent: String = "",
    val fileName: String = ""
) {
    val isEvent: Boolean
        get() = type == NoteType.EVENT || !eventStart.isNullOrBlank()

    fun getCleanExcerpt(maxLength: Int = 120): String {
        val clean = bodyContent
            .replace(Regex("#+\\s+"), "")
            .replace(Regex("\\[\\[(.*?)\\]\\]")) { match ->
                val content = match.groupValues[1]
                if (content.contains("|")) content.substringAfter("|") else content
            }
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("[*_`~]"), "")
            .trim()
        return if (clean.length > maxLength) clean.take(maxLength) + "..." else clean
    }
}

data class Backlink(
    val sourceNoteId: String,
    val sourceTitle: String,
    val sourceFileName: String,
    val excerpt: String
)

data class GraphNode(
    val id: String,
    val label: String,
    val type: NoteType,
    val colorHex: String,
    val connectionCount: Int,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f
)

data class GraphLink(
    val sourceId: String,
    val targetId: String
)

data class GraphData(
    val nodes: List<GraphNode>,
    val links: List<GraphLink>
)

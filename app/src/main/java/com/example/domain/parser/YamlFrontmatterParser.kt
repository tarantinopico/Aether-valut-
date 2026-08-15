package com.example.domain.parser

import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object YamlFrontmatterParser {

    private val isoDateFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    fun nowIsoString(): String = isoDateFormat.format(Date())

    /**
     * Parses a raw markdown file containing YAML frontmatter into a NoteEntity.
     */
    fun parse(rawMarkdown: String, fileName: String = ""): NoteEntity {
        val trimmed = rawMarkdown.trimStart()
        if (!trimmed.startsWith("---")) {
            // No frontmatter found, whole text is body
            val firstLineTitle = rawMarkdown.lines().firstOrNull { it.isNotBlank() }
                ?.replace(Regex("^#+\\s*"), "")?.trim()
                ?: fileName.removeSuffix(".md").ifBlank { "Untitled" }
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = firstLineTitle,
                type = NoteType.TEXT,
                createdAt = nowIsoString(),
                updatedAt = nowIsoString(),
                bodyContent = rawMarkdown,
                fileName = fileName
            )
        }

        // Split frontmatter from body
        val lines = trimmed.lines()
        val endDelimiterIndex = lines.drop(1).indexOfFirst { it.trim() == "---" }

        if (endDelimiterIndex == -1) {
            // Malformed frontmatter
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = fileName.removeSuffix(".md").ifBlank { "Untitled" },
                type = NoteType.TEXT,
                createdAt = nowIsoString(),
                updatedAt = nowIsoString(),
                bodyContent = rawMarkdown,
                fileName = fileName
            )
        }

        val frontmatterLines = lines.subList(1, endDelimiterIndex + 1)
        val bodyLines = lines.subList(endDelimiterIndex + 2, lines.size)
        val bodyContent = bodyLines.joinToString("\n").trimStart('\n')

        var id = UUID.randomUUID().toString()
        var title = fileName.removeSuffix(".md").ifBlank { "Untitled" }
        var type = NoteType.TEXT
        var createdAt = ""
        var updatedAt = ""
        val tags = mutableListOf<String>()
        val aliases = mutableListOf<String>()
        var eventStart: String? = null
        var eventEnd: String? = null
        var isAllDay = false
        var location: String? = null
        var isPinned = false
        var colorHex: String? = null
        val customFields = mutableMapOf<String, String>()

        var currentListKey: String? = null

        for (line in frontmatterLines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) continue

            // List item under a multi-line list (e.g. - item)
            if (trimmedLine.startsWith("-") && currentListKey != null) {
                val item = trimmedLine.removePrefix("-").trim().removeSurrounding("\"").removeSurrounding("'")
                if (item.isNotEmpty()) {
                    when (currentListKey) {
                        "tags" -> tags.add(item)
                        "aliases" -> aliases.add(item)
                    }
                }
                continue
            }

            // Key-value line
            val colonIndex = trimmedLine.indexOf(':')
            if (colonIndex != -1) {
                val key = trimmedLine.substring(0, colonIndex).trim().lowercase()
                val value = trimmedLine.substring(colonIndex + 1).trim()

                currentListKey = if (value.isEmpty()) key else null

                val cleanVal = value.removeSurrounding("\"").removeSurrounding("'")

                when (key) {
                    "id" -> if (cleanVal.isNotEmpty()) id = cleanVal
                    "title" -> if (cleanVal.isNotEmpty()) title = cleanVal
                    "type" -> type = NoteType.fromString(cleanVal)
                    "created_at", "createdat", "created" -> createdAt = cleanVal
                    "updated_at", "updatedat", "updated" -> updatedAt = cleanVal
                    "event_start", "eventstart", "start" -> eventStart = cleanVal.ifBlank { null }
                    "event_end", "eventend", "end" -> eventEnd = cleanVal.ifBlank { null }
                    "all_day", "allday" -> isAllDay = cleanVal.toBooleanStrictOrNull() ?: false
                    "location" -> location = cleanVal.ifBlank { null }
                    "pinned", "is_pinned" -> isPinned = cleanVal.toBooleanStrictOrNull() ?: false
                    "color", "color_hex" -> colorHex = cleanVal.ifBlank { null }
                    "tags" -> {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            val items = value.removeSurrounding("[", "]")
                                .split(",")
                                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                                .filter { it.isNotEmpty() }
                            tags.addAll(items)
                            currentListKey = null
                        }
                    }
                    "aliases" -> {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            val items = value.removeSurrounding("[", "]")
                                .split(",")
                                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                                .filter { it.isNotEmpty() }
                            aliases.addAll(items)
                            currentListKey = null
                        }
                    }
                    else -> {
                        if (cleanVal.isNotEmpty()) {
                            customFields[key] = cleanVal
                        }
                    }
                }
            }
        }

        if (createdAt.isBlank()) createdAt = nowIsoString()
        if (updatedAt.isBlank()) updatedAt = createdAt

        return NoteEntity(
            id = id,
            title = title,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.distinct(),
            aliases = aliases.distinct(),
            eventStart = eventStart,
            eventEnd = eventEnd,
            isAllDay = isAllDay,
            location = location,
            isPinned = isPinned,
            colorHex = colorHex,
            customFields = customFields,
            bodyContent = bodyContent,
            fileName = fileName
        )
    }

    /**
     * Serializes a NoteEntity into full Markdown text with YAML frontmatter.
     */
    fun serialize(note: NoteEntity): String {
        val sb = StringBuilder()
        sb.append("---\n")
        sb.append("id: \"${escapeYaml(note.id)}\"\n")
        sb.append("title: \"${escapeYaml(note.title)}\"\n")
        sb.append("type: ${note.type.rawValue}\n")
        sb.append("created_at: \"${note.createdAt.ifBlank { nowIsoString() }}\"\n")
        sb.append("updated_at: \"${note.updatedAt.ifBlank { nowIsoString() }}\"\n")

        if (note.tags.isNotEmpty()) {
            sb.append("tags:\n")
            note.tags.forEach { tag ->
                sb.append("  - \"${escapeYaml(tag)}\"\n")
            }
        }

        if (note.aliases.isNotEmpty()) {
            sb.append("aliases:\n")
            note.aliases.forEach { alias ->
                sb.append("  - \"${escapeYaml(alias)}\"\n")
            }
        }

        if (!note.eventStart.isNullOrBlank()) {
            sb.append("event_start: \"${note.eventStart}\"\n")
        }
        if (!note.eventEnd.isNullOrBlank()) {
            sb.append("event_end: \"${note.eventEnd}\"\n")
        }
        if (note.isAllDay) {
            sb.append("all_day: true\n")
        }
        if (!note.location.isNullOrBlank()) {
            sb.append("location: \"${escapeYaml(note.location)}\"\n")
        }
        if (note.isPinned) {
            sb.append("pinned: true\n")
        }
        if (!note.colorHex.isNullOrBlank()) {
            sb.append("color: \"${note.colorHex}\"\n")
        }
        for ((k, v) in note.customFields) {
            sb.append("${escapeYamlKey(k)}: \"${escapeYaml(v)}\"\n")
        }

        sb.append("---\n\n")
        sb.append(note.bodyContent)
        return sb.toString()
    }

    private fun escapeYaml(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private fun escapeYamlKey(key: String): String {
        return key.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }
}

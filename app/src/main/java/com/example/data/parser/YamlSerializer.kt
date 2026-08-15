package com.example.data.parser

import com.example.domain.model.Frontmatter
import com.example.domain.model.NoteType
import java.util.UUID

object YamlSerializer {

    fun parse(yamlContent: String): Frontmatter {
        val lines = yamlContent.lines()
        val map = mutableMapOf<String, Any>()
        var currentListKey: String? = null
        val currentListItems = mutableListOf<String>()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            if (line.startsWith("- ") && currentListKey != null) {
                val item = line.removePrefix("- ").trim().trim('"', '\'')
                if (item.isNotEmpty()) {
                    currentListItems.add(item)
                }
                continue
            } else if (currentListKey != null) {
                map[currentListKey] = currentListItems.toList()
                currentListKey = null
                currentListItems.clear()
            }

            val colonIndex = line.indexOf(':')
            if (colonIndex != -1) {
                val key = line.substring(0, colonIndex).trim()
                val rawValue = line.substring(colonIndex + 1).trim()

                if (rawValue.isEmpty()) {
                    // Start of multi-line list
                    currentListKey = key
                    currentListItems.clear()
                } else if (rawValue.startsWith("[") && rawValue.endsWith("]")) {
                    // Inline list e.g. [a, b, c]
                    val content = rawValue.substring(1, rawValue.length - 1).trim()
                    val items = if (content.isEmpty()) {
                        emptyList()
                    } else {
                        content.split(",").map { it.trim().trim('"', '\'') }.filter { it.isNotEmpty() }
                    }
                    map[key] = items
                } else {
                    val unquoted = rawValue.trim('"', '\'')
                    map[key] = unquoted
                }
            }
        }

        if (currentListKey != null) {
            map[currentListKey] = currentListItems.toList()
        }

        fun getString(k: String): String? = (map[k] as? String)?.takeIf { it.isNotBlank() }
        fun getBoolean(k: String): Boolean? {
            val v = map[k] ?: return null
            return when (v.toString().trim().lowercase()) {
                "true", "yes", "1" -> true
                "false", "no", "0" -> false
                else -> null
            }
        }
        fun getInt(k: String): Int? = (map[k] as? String)?.toIntOrNull()

        @Suppress("UNCHECKED_CAST")
        fun getList(k: String): List<String> {
            val raw = map[k] ?: return emptyList()
            return when (raw) {
                is List<*> -> raw.mapNotNull { it?.toString() }
                is String -> if (raw.isBlank()) emptyList() else listOf(raw)
                else -> emptyList()
            }
        }

        val id = getString("id") ?: UUID.randomUUID().toString()
        val type = NoteType.fromString(getString("type"))
        val title = getString("title") ?: "Untitled Note"
        val created = getString("created") ?: ""
        val updated = getString("updated") ?: ""
        val tags = getList("tags")
        val folder = getString("folder") ?: ""
        val links = getList("links")

        val url = getString("url")
        val previewImage = getString("previewImage")
        val domain = getString("domain")
        val summary = getString("summary")

        val startDateTime = getString("startDateTime")
        val endDateTime = getString("endDateTime")
        val isAllDay = getBoolean("isAllDay")
        val category = getString("category")
        val categoryColor = getString("categoryColor")

        val columns = getList("columns")
        val rowCount = getInt("rowCount") ?: 0

        return Frontmatter(
            id = id,
            type = type,
            title = title,
            created = created,
            updated = updated,
            tags = tags,
            folder = folder,
            links = links,
            url = url,
            previewImage = previewImage,
            domain = domain,
            summary = summary,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            isAllDay = isAllDay,
            category = category,
            categoryColor = categoryColor,
            columns = columns,
            rowCount = rowCount
        )
    }

    fun serialize(frontmatter: Frontmatter): String {
        val sb = StringBuilder()
        sb.appendLine("---")
        sb.appendLine("id: \"${frontmatter.id}\"")
        sb.appendLine("type: \"${frontmatter.type.rawValue}\"")
        sb.appendLine("title: \"${frontmatter.title.replace("\"", "\\\"")}\"")
        sb.appendLine("created: \"${frontmatter.created}\"")
        sb.appendLine("updated: \"${frontmatter.updated}\"")

        if (frontmatter.folder.isNotEmpty()) {
            sb.appendLine("folder: \"${frontmatter.folder.replace("\"", "\\\"")}\"")
        }

        if (frontmatter.tags.isNotEmpty()) {
            sb.appendLine("tags:")
            frontmatter.tags.forEach { tag ->
                sb.appendLine("  - \"${tag.replace("\"", "\\\"")}\"")
            }
        } else {
            sb.appendLine("tags: []")
        }

        if (frontmatter.links.isNotEmpty()) {
            sb.appendLine("links:")
            frontmatter.links.forEach { link ->
                sb.appendLine("  - \"${link.replace("\"", "\\\"")}\"")
            }
        }

        when (frontmatter.type) {
            NoteType.BOOKMARK -> {
                frontmatter.url?.let { sb.appendLine("url: \"$it\"") }
                frontmatter.domain?.let { sb.appendLine("domain: \"$it\"") }
                frontmatter.previewImage?.let { sb.appendLine("previewImage: \"$it\"") }
                frontmatter.summary?.let { sb.appendLine("summary: \"${it.replace("\"", "\\\"")}\"") }
            }
            NoteType.EVENT -> {
                frontmatter.startDateTime?.let { sb.appendLine("startDateTime: \"$it\"") }
                frontmatter.endDateTime?.let { sb.appendLine("endDateTime: \"$it\"") }
                sb.appendLine("isAllDay: ${frontmatter.isAllDay == true}")
                frontmatter.category?.let { sb.appendLine("category: \"${it.replace("\"", "\\\"")}\"") }
                frontmatter.categoryColor?.let { sb.appendLine("categoryColor: \"$it\"") }
            }
            NoteType.DATABASE -> {
                if (frontmatter.columns.isNotEmpty()) {
                    sb.appendLine("columns:")
                    frontmatter.columns.forEach { col ->
                        sb.appendLine("  - \"${col.replace("\"", "\\\"")}\"")
                    }
                }
                sb.appendLine("rowCount: ${frontmatter.rowCount}")
            }
            NoteType.TEXT -> {
                // Text note specific metadata if any
            }
        }

        sb.appendLine("---")
        return sb.toString()
    }
}

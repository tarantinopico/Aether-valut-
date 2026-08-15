package com.example.data.parser

import android.net.Uri
import com.example.domain.model.Frontmatter
import com.example.domain.model.Note
import com.example.domain.model.NoteType
import com.example.domain.model.SymlinkRef
import com.example.domain.model.SymlinkType
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

object MarkdownParser {

    private val FRONTMATTER_REGEX = Regex("""^---\r?\n([\s\S]*?)\r?\n---\r?\n?""")
    private val SYMLINK_REGEX = Regex("""\[\[(.*?)\]\]""")
    private val CHECKBOX_REGEX = Regex("""^(\s*[-*+]\s+\[)([ xX])(\]\s+.*)$""")

    fun parse(
        content: String,
        fileName: String = "untitled.md",
        relativePath: String = "",
        uri: Uri? = null
    ): Note {
        val match = FRONTMATTER_REGEX.find(content)
        val (frontmatter, body) = if (match != null) {
            val yamlContent = match.groupValues[1]
            val parsedFm = YamlSerializer.parse(yamlContent)
            val parsedBody = content.substring(match.range.last + 1)
            Pair(parsedFm, parsedBody)
        } else {
            // No frontmatter header found - create default frontmatter
            val titleFromFirstLine = content.lines().firstOrNull { it.isNotBlank() }
                ?.removePrefix("#")
                ?.trim()
                ?.ifBlank { null }
                ?: fileName.removeSuffix(".md")

            val fallbackFm = Frontmatter(
                id = UUID.randomUUID().toString(),
                type = NoteType.TEXT,
                title = titleFromFirstLine,
                created = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                updated = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                tags = emptyList(),
                folder = relativePath.substringBeforeLast('/', "")
            )
            Pair(fallbackFm, content)
        }

        val outboundLinks = extractSymlinks(body)

        return Note(
            id = frontmatter.id,
            frontmatter = frontmatter,
            body = body,
            fileName = fileName,
            relativePath = relativePath,
            uri = uri,
            outboundLinks = outboundLinks,
            backlinks = emptyList()
        )
    }

    fun serialize(note: Note): String {
        val updatedFm = note.frontmatter.copy(
            updated = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
        val frontmatterBlock = YamlSerializer.serialize(updatedFm)
        return "$frontmatterBlock${note.body}"
    }

    fun extractSymlinks(text: String): List<SymlinkRef> {
        val results = mutableListOf<SymlinkRef>()
        SYMLINK_REGEX.findAll(text).forEach { match ->
            val raw = match.value
            val inner = match.groupValues[1].trim()
            if (inner.isNotEmpty()) {
                val parts = inner.split("|")
                val target = parts[0].trim()
                val alias = if (parts.size > 1) parts[1].trim() else target

                val (type, cleanTarget) = when {
                    target.startsWith("event:", ignoreCase = true) -> {
                        Pair(SymlinkType.EVENT_ID, target.substringAfter(":").trim())
                    }
                    target.startsWith("note:", ignoreCase = true) -> {
                        Pair(SymlinkType.NOTE_ID, target.substringAfter(":").trim())
                    }
                    else -> {
                        Pair(SymlinkType.NOTE_TITLE, target)
                    }
                }

                results.add(
                    SymlinkRef(
                        rawMatch = raw,
                        target = cleanTarget,
                        type = type,
                        displayText = alias.ifBlank { cleanTarget }
                    )
                )
            }
        }
        return results
    }

    /**
     * Toggles the N-th checkbox in the markdown body between `- [ ]` and `- [x]`.
     */
    fun toggleCheckboxAtIndex(body: String, targetCheckboxIndex: Int): String {
        var currentIndex = 0
        val lines = body.lines().toMutableList()

        for (i in lines.indices) {
            val line = lines[i]
            val match = CHECKBOX_REGEX.find(line)
            if (match != null) {
                if (currentIndex == targetCheckboxIndex) {
                    val prefix = match.groupValues[1]
                    val checkState = match.groupValues[2]
                    val suffix = match.groupValues[3]
                    val newCheckState = if (checkState.equals("x", ignoreCase = true)) " " else "x"
                    lines[i] = "$prefix$newCheckState$suffix"
                    break
                }
                currentIndex++
            }
        }

        return lines.joinToString("\n")
    }

    /**
     * Inserts a symlink reference at a given cursor position or appends it.
     */
    fun insertSymlink(body: String, target: String, isEvent: Boolean = false): String {
        val symlinkStr = if (isEvent) "[[event:$target]]" else "[[$target]]"
        return if (body.isBlank()) symlinkStr else "$body\n$symlinkStr"
    }
}

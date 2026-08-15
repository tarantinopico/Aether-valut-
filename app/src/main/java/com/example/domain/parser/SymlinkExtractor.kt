package com.example.domain.parser

import com.example.domain.model.Backlink
import com.example.domain.model.GraphData
import com.example.domain.model.GraphLink
import com.example.domain.model.GraphNode
import com.example.domain.model.NoteEntity
import kotlin.math.cos
import kotlin.math.sin

data class ParsedSymlink(
    val raw: String,
    val targetRef: String, // ID, Title, or Alias
    val displayText: String,
    val heading: String? = null
)

object SymlinkExtractor {

    // Regex for matching [[target]] or [[target|display]] or [[target#heading|display]]
    private val symlinkRegex = Regex("\\[\\[([^\\]|#]+)(?:#([^\\]|]+))?(?:\\|([^\\]]+))?\\]\\]")

    /**
     * Extracts all symlinks found in a markdown text.
     */
    fun extractSymlinks(text: String): List<ParsedSymlink> {
        val results = mutableListOf<ParsedSymlink>()
        symlinkRegex.findAll(text).forEach { match ->
            val raw = match.value
            val target = match.groupValues[1].trim()
            val heading = match.groupValues[2].ifBlank { null }?.trim()
            val display = match.groupValues[3].ifBlank { null }?.trim() ?: target
            if (target.isNotEmpty()) {
                results.add(ParsedSymlink(raw, target, display, heading))
            }
        }
        return results
    }

    /**
     * Finds target NoteEntity matching a given link target (by ID, Title, or Alias case-insensitive).
     */
    fun resolveTarget(target: String, allNotes: List<NoteEntity>): NoteEntity? {
        val trimmed = target.trim()
        // 1. Direct ID match
        allNotes.find { it.id.equals(trimmed, ignoreCase = true) }?.let { return it }

        // 2. Direct title match (case insensitive)
        allNotes.find { it.title.equals(trimmed, ignoreCase = true) }?.let { return it }

        // 3. Match by filename without extension
        allNotes.find { it.fileName.removeSuffix(".md").equals(trimmed, ignoreCase = true) }?.let { return it }

        // 4. Match in aliases
        allNotes.find { note ->
            note.aliases.any { it.equals(trimmed, ignoreCase = true) }
        }?.let { return it }

        return null
    }

    /**
     * Extracts an excerpt surrounding the first occurrence of a target reference in text.
     */
    fun extractContextSnippet(text: String, targetRef: String, maxLen: Int = 100): String {
        val lowerText = text.lowercase()
        val lowerTarget = targetRef.lowercase()
        val index = lowerText.indexOf(lowerTarget)
        if (index == -1) {
            return text.take(maxLen).trim()
        }

        val start = (index - 30).coerceAtLeast(0)
        val end = (index + targetRef.length + 50).coerceAtMost(text.length)
        var snippet = text.substring(start, end).replace("\n", " ").trim()
        if (start > 0) snippet = "...$snippet"
        if (end < text.length) snippet = "$snippet..."
        return snippet
    }

    /**
     * Builds full map of Note ID -> List of incoming Backlinks across entire vault.
     */
    fun buildBacklinksMap(allNotes: List<NoteEntity>): Map<String, List<Backlink>> {
        val backlinksMap = mutableMapOf<String, MutableList<Backlink>>()
        for (note in allNotes) {
            backlinksMap[note.id] = mutableListOf()
        }

        for (sourceNote in allNotes) {
            val links = extractSymlinks(sourceNote.bodyContent)
            for (link in links) {
                val targetNote = resolveTarget(link.targetRef, allNotes)
                if (targetNote != null && targetNote.id != sourceNote.id) {
                    val snippet = extractContextSnippet(sourceNote.bodyContent, link.raw)
                    val backlink = Backlink(
                        sourceNoteId = sourceNote.id,
                        sourceTitle = sourceNote.title,
                        sourceFileName = sourceNote.fileName,
                        excerpt = snippet
                    )
                    val list = backlinksMap.getOrPut(targetNote.id) { mutableListOf() }
                    if (list.none { it.sourceNoteId == sourceNote.id }) {
                        list.add(backlink)
                    }
                }
            }
        }

        return backlinksMap
    }

    /**
     * Builds visual graph structure (nodes + links) for the entire note network.
     */
    fun buildGraphData(allNotes: List<NoteEntity>): GraphData {
        val links = mutableListOf<GraphLink>()
        val connectionCounts = mutableMapOf<String, Int>()

        for (note in allNotes) {
            connectionCounts[note.id] = 0
        }

        for (sourceNote in allNotes) {
            val symlinks = extractSymlinks(sourceNote.bodyContent)
            for (link in symlinks) {
                val target = resolveTarget(link.targetRef, allNotes)
                if (target != null && target.id != sourceNote.id) {
                    links.add(GraphLink(sourceId = sourceNote.id, targetId = target.id))
                    connectionCounts[sourceNote.id] = (connectionCounts[sourceNote.id] ?: 0) + 1
                    connectionCounts[target.id] = (connectionCounts[target.id] ?: 0) + 1
                }
            }
        }

        val totalNotes = allNotes.size
        val radius = 250f + (totalNotes * 20f).coerceAtMost(600f)

        val nodes = allNotes.mapIndexed { index, note ->
            val angle = (2 * Math.PI * index / totalNotes.coerceAtLeast(1)).toFloat()
            val x = (cos(angle.toDouble()) * radius).toFloat()
            val y = (sin(angle.toDouble()) * radius).toFloat()
            val count = connectionCounts[note.id] ?: 0

            val color = note.colorHex ?: when (note.type) {
                com.example.domain.model.NoteType.TEXT -> "#6366F1"
                com.example.domain.model.NoteType.EVENT -> "#06B6D4"
                com.example.domain.model.NoteType.BOOKMARK -> "#EC4899"
                com.example.domain.model.NoteType.DATABASE -> "#10B981"
            }

            GraphNode(
                id = note.id,
                label = note.title,
                type = note.type,
                colorHex = color,
                connectionCount = count,
                x = x,
                y = y
            )
        }

        return GraphData(nodes = nodes, links = links)
    }
}

package com.example

import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import com.example.domain.parser.SymlinkExtractor
import com.example.domain.parser.YamlFrontmatterParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AetherDomainUnitTest {

    @Test
    fun testYamlFrontmatterSerializationAndParsing() {
        val originalNote = NoteEntity(
            id = "test-uuid-1234",
            title = "Architecture Sprint",
            type = NoteType.EVENT,
            createdAt = "2026-08-15T09:00:00Z",
            updatedAt = "2026-08-15T10:00:00Z",
            tags = listOf("sprint", "mobile", "architecture"),
            aliases = listOf("Sprint 14", "Roadmap"),
            eventStart = "2026-08-15T14:00:00Z",
            eventEnd = "2026-08-15T15:30:00Z",
            isAllDay = false,
            location = "Virtual Room A",
            isPinned = true,
            colorHex = "#06B6D4",
            bodyContent = "# Architecture Sprint\n\nDiscussion regarding [[Database Schema]] and [[Frontend State]].\n\n- [ ] Finalize models\n- [x] Create parsers\n"
        )

        // Serialize to Markdown
        val markdownText = YamlFrontmatterParser.serialize(originalNote)
        assertTrue(markdownText.startsWith("---\n"))
        assertTrue(markdownText.contains("id: \"test-uuid-1234\""))
        assertTrue(markdownText.contains("title: \"Architecture Sprint\""))
        assertTrue(markdownText.contains("type: event"))
        assertTrue(markdownText.contains("pinned: true"))

        // Parse back
        val parsedNote = YamlFrontmatterParser.parse(markdownText, "Architecture Sprint.md")
        assertEquals(originalNote.id, parsedNote.id)
        assertEquals(originalNote.title, parsedNote.title)
        assertEquals(originalNote.type, parsedNote.type)
        assertEquals(originalNote.tags, parsedNote.tags)
        assertEquals(originalNote.aliases, parsedNote.aliases)
        assertEquals(originalNote.eventStart, parsedNote.eventStart)
        assertEquals(originalNote.location, parsedNote.location)
        assertTrue(parsedNote.isPinned)
        assertEquals(originalNote.bodyContent, parsedNote.bodyContent)
    }

    @Test
    fun testSymlinkExtraction() {
        val text = """
            Here is a link to [[Project Roadmap]] and another with alias [[System Design|The Core Arch]].
            Also checking heading link [[Meeting Notes#Action Items|Actions]] and standard [[Daily Log]].
        """.trimIndent()

        val extracted = SymlinkExtractor.extractSymlinks(text)
        assertEquals(4, extracted.size)

        assertEquals("Project Roadmap", extracted[0].targetRef)
        assertEquals("Project Roadmap", extracted[0].displayText)

        assertEquals("System Design", extracted[1].targetRef)
        assertEquals("The Core Arch", extracted[1].displayText)

        assertEquals("Meeting Notes", extracted[2].targetRef)
        assertEquals("Action Items", extracted[2].heading)
        assertEquals("Actions", extracted[2].displayText)

        assertEquals("Daily Log", extracted[3].targetRef)
    }

    @Test
    fun testBidirectionalBacklinksMap() {
        val noteA = NoteEntity(
            id = "note-a",
            title = "Aether Design",
            bodyContent = "Referencing [[Quantum Computing]] and [[Local First]]."
        )

        val noteB = NoteEntity(
            id = "note-b",
            title = "Quantum Computing",
            bodyContent = "Quantum notes linked to [[Aether Design]]."
        )

        val noteC = NoteEntity(
            id = "note-c",
            title = "Local First",
            bodyContent = "Architectural principles for local storage."
        )

        val allNotes = listOf(noteA, noteB, noteC)
        val backlinksMap = SymlinkExtractor.buildBacklinksMap(allNotes)

        // Note B is referenced by Note A
        val backlinksB = backlinksMap["note-b"]
        assertNotNull(backlinksB)
        assertEquals(1, backlinksB?.size)
        assertEquals("Aether Design", backlinksB?.first()?.sourceTitle)

        // Note A is referenced by Note B
        val backlinksA = backlinksMap["note-a"]
        assertNotNull(backlinksA)
        assertEquals(1, backlinksA?.size)
        assertEquals("Quantum Computing", backlinksA?.first()?.sourceTitle)

        // Note C is referenced by Note A
        val backlinksC = backlinksMap["note-c"]
        assertNotNull(backlinksC)
        assertEquals(1, backlinksC?.size)
        assertEquals("Aether Design", backlinksC?.first()?.sourceTitle)
    }

    @Test
    fun testGraphDataGeneration() {
        val note1 = NoteEntity(id = "1", title = "Node 1", bodyContent = "Links to [[Node 2]]")
        val note2 = NoteEntity(id = "2", title = "Node 2", bodyContent = "Links to [[Node 3]]")
        val note3 = NoteEntity(id = "3", title = "Node 3", bodyContent = "Links to [[Node 1]]")

        val graph = SymlinkExtractor.buildGraphData(listOf(note1, note2, note3))
        assertEquals(3, graph.nodes.size)
        assertEquals(3, graph.links.size)
    }
}

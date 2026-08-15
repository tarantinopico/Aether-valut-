package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.storage.VaultStorageManager
import com.example.domain.model.Backlink
import com.example.domain.model.GraphData
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import com.example.domain.parser.SymlinkExtractor
import com.example.domain.parser.YamlFrontmatterParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class VaultRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val storageManager = VaultStorageManager(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("aether_prefs", Context.MODE_PRIVATE)

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _backlinksMap = MutableStateFlow<Map<String, List<Backlink>>>(emptyMap())
    val backlinksMap: StateFlow<Map<String, List<Backlink>>> = _backlinksMap.asStateFlow()

    private val _graphData = MutableStateFlow(GraphData(emptyList(), emptyList()))
    val graphData: StateFlow<GraphData> = _graphData.asStateFlow()

    private val _activeVaultUri = MutableStateFlow<String?>(prefs.getString("vault_tree_uri", null))
    val activeVaultUri: StateFlow<String?> = _activeVaultUri.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        scope.launch {
            loadVault()
        }
    }

    suspend fun setCustomVaultUri(uriString: String?) = withContext(Dispatchers.IO) {
        prefs.edit().putString("vault_tree_uri", uriString).apply()
        _activeVaultUri.value = uriString
        loadVault()
    }

    suspend fun loadVault() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        var loaded = storageManager.loadAllNotes(_activeVaultUri.value)
        if (loaded.isEmpty()) {
            // Populate starter sample vault
            val sampleNotes = createSampleNotes()
            for (sample in sampleNotes) {
                storageManager.saveNote(sample, _activeVaultUri.value)
            }
            loaded = storageManager.loadAllNotes(_activeVaultUri.value)
        }

        updateInternalState(loaded)
        _isLoading.value = false
    }

    private fun updateInternalState(allNotes: List<NoteEntity>) {
        _notes.value = allNotes
        _backlinksMap.value = SymlinkExtractor.buildBacklinksMap(allNotes)
        _graphData.value = SymlinkExtractor.buildGraphData(allNotes)
    }

    suspend fun getNoteById(id: String): NoteEntity? {
        return _notes.value.find { it.id == id }
    }

    suspend fun saveOrUpdateNote(note: NoteEntity): NoteEntity = withContext(Dispatchers.IO) {
        val updatedNote = note.copy(
            updatedAt = YamlFrontmatterParser.nowIsoString()
        )
        val savedFileName = storageManager.saveNote(updatedNote, _activeVaultUri.value)
        val finalNote = updatedNote.copy(fileName = savedFileName)

        val current = _notes.value.toMutableList()
        val index = current.indexOfFirst { it.id == finalNote.id }
        if (index != -1) {
            current[index] = finalNote
        } else {
            current.add(0, finalNote)
        }

        updateInternalState(current)
        return@withContext finalNote
    }

    suspend fun deleteNote(noteId: String): Boolean = withContext(Dispatchers.IO) {
        val note = _notes.value.find { it.id == noteId } ?: return@withContext false
        val success = storageManager.deleteNote(note, _activeVaultUri.value)
        if (success) {
            val current = _notes.value.filterNot { it.id == noteId }
            updateInternalState(current)
        }
        return@withContext success
    }

    suspend fun toggleTaskCheckbox(noteId: String, lineIndex: Int): NoteEntity? = withContext(Dispatchers.IO) {
        val note = _notes.value.find { it.id == noteId } ?: return@withContext null
        val lines = note.bodyContent.lines().toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            val updatedLine = when {
                line.contains("- [ ]") -> line.replace("- [ ]", "- [x]")
                line.contains("- [x]") -> line.replace("- [x]", "- [ ]")
                line.contains("* [ ]") -> line.replace("* [ ]", "* [x]")
                line.contains("* [x]") -> line.replace("* [x]", "* [ ]")
                else -> line
            }
            lines[lineIndex] = updatedLine
            val updatedContent = lines.joinToString("\n")
            val updated = note.copy(bodyContent = updatedContent)
            return@withContext saveOrUpdateNote(updated)
        }
        return@withContext null
    }

    suspend fun getOrCreateDailyNote(date: Date = Date()): NoteEntity = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        val title = "Daily Note - $dateStr"
        val existing = _notes.value.find { it.title.equals(title, ignoreCase = true) || it.aliases.contains(dateStr) }
        if (existing != null) {
            return@withContext existing
        }

        val cal = Calendar.getInstance().apply { time = date }
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startIso = isoFormat.format(cal.time)

        val newNote = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            type = NoteType.TEXT,
            createdAt = YamlFrontmatterParser.nowIsoString(),
            updatedAt = YamlFrontmatterParser.nowIsoString(),
            tags = listOf("daily", "journal"),
            aliases = listOf(dateStr),
            eventStart = startIso,
            isAllDay = true,
            colorHex = "#6366F1",
            bodyContent = """
                # ☀️ Daily Log: $dateStr
                
                > "Every day is a fresh canvas in your knowledge garden."
                
                ### 🎯 Top Priorities
                - [ ] Review sprint goals in [[Sprint Planning]]
                - [ ] Prepare updates for [[Product Strategy Sync]]
                - [ ] Capture new insights and links
                
                ### 📝 Notes & Reflections
                - Started the morning checking Aether bidirectional links.
                - Linked knowledge bases with [[Welcome to Aether]].
            """.trimIndent()
        )
        return@withContext saveOrUpdateNote(newNote)
    }

    suspend fun createEventNote(
        title: String,
        startIso: String,
        endIso: String?,
        isAllDay: Boolean,
        location: String?,
        colorHex: String?,
        tags: List<String>,
        initialContent: String = ""
    ): NoteEntity = withContext(Dispatchers.IO) {
        val body = if (initialContent.isNotBlank()) initialContent else """
            # $title
            
            **Event Details**
            - **Date/Time:** $startIso
            - **Location:** ${location ?: "Remote / Local"}
            
            ### 📌 Agenda & Action Items
            - [ ] Define objectives
            - [ ] Coordinate with team
            - [ ] Document outcomes and link to [[Welcome to Aether]]
        """.trimIndent()

        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            type = NoteType.EVENT,
            createdAt = YamlFrontmatterParser.nowIsoString(),
            updatedAt = YamlFrontmatterParser.nowIsoString(),
            tags = tags.ifEmpty { listOf("event") },
            eventStart = startIso,
            eventEnd = endIso,
            isAllDay = isAllDay,
            location = location,
            colorHex = colorHex ?: "#06B6D4",
            bodyContent = body
        )
        return@withContext saveOrUpdateNote(note)
    }

    suspend fun resetWithSampleVault() = withContext(Dispatchers.IO) {
        val sampleNotes = createSampleNotes()
        for (sample in sampleNotes) {
            storageManager.saveNote(sample, _activeVaultUri.value)
        }
        loadVault()
    }

    suspend fun getStorageStats(): Triple<Int, Long, Int> = withContext(Dispatchers.IO) {
        val (count, bytes) = storageManager.getVaultStats(_activeVaultUri.value)
        var totalBacklinks = 0
        _backlinksMap.value.values.forEach { totalBacklinks += it.size }
        return@withContext Triple(count, bytes, totalBacklinks)
    }

    private fun createSampleNotes(): List<NoteEntity> {
        val now = YamlFrontmatterParser.nowIsoString()
        val cal = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val eventStartIso = isoFormat.format(cal.time)

        cal.add(Calendar.HOUR_OF_DAY, 1)
        val eventEndIso = isoFormat.format(cal.time)

        val noteWelcome = NoteEntity(
            id = "aether-welcome-01",
            title = "Welcome to Aether",
            type = NoteType.TEXT,
            createdAt = now,
            updatedAt = now,
            tags = listOf("guide", "aether", "getting-started"),
            aliases = listOf("Aether Guide", "Introduction"),
            isPinned = true,
            colorHex = "#6366F1",
            bodyContent = """
                # 🌌 Welcome to Aether: Notes & Calendar Hybrid
                
                Aether connects your **structured thoughts**, **calendar timelines**, and **bidirectional graph network** directly using pure on-device Markdown files!
                
                ---
                
                ### ✨ Core Capabilities:
                1. **Bento Grid Navigation:** Fluid, organized card layout for notes and events.
                2. **Bidirectional Links (`[[...]]`):** Link to any note like [[Architecture Plan 2026]] or [[Product Strategy Sync]].
                3. **Interactive Checklists:** Tap directly in view mode to toggle tasks:
                   - [x] Download Aether
                   - [x] Explore starter vault notes
                   - [ ] Create your first custom note
                   - [ ] Add an event to the calendar
                4. **Calendar Integration:** Every event is backed by a full Markdown note with rich frontmatter!
                5. **Visual Graph:** Navigate to the **Graph** tab to inspect inter-note connections and clusters.
                
                ### 🔗 Related Links
                - Check the ongoing sprint in [[Sprint Planning]]
                - Review design guidelines in [[Design System V2]]
            """.trimIndent()
        )

        val noteArchitecture = NoteEntity(
            id = "aether-arch-02",
            title = "Architecture Plan 2026",
            type = NoteType.DATABASE,
            createdAt = now,
            updatedAt = now,
            tags = listOf("architecture", "database", "offline-first"),
            aliases = listOf("System Design"),
            isPinned = true,
            colorHex = "#10B981",
            bodyContent = """
                # 🏗️ Architecture Plan 2026
                
                > "Local-first software puts the user in control of their data."
                
                ### 📦 Data Layer Strategy
                - **Frontmatter Standard:** Pure YAML frontmatter parsed with round-trip safety.
                - **Storage Access Framework (SAF):** True folder ownership without cloud vendor lock-in.
                - **Graph Engine:** Live bidirectional link resolution parsed via regex.
                
                ### 📅 Key Milestones
                - Review specifications with the leadership team in [[Product Strategy Sync]].
                - Track weekly progress in [[Sprint Planning]].
                - Follow UI standards defined in [[Design System V2]].
            """.trimIndent()
        )

        val noteStrategy = NoteEntity(
            id = "aether-strategy-03",
            title = "Product Strategy Sync",
            type = NoteType.EVENT,
            createdAt = now,
            updatedAt = now,
            tags = listOf("meeting", "strategy", "q3"),
            eventStart = eventStartIso,
            eventEnd = eventEndIso,
            isAllDay = false,
            location = "Innovation Lab Room 402",
            isPinned = false,
            colorHex = "#06B6D4",
            bodyContent = """
                # 📅 Product Strategy Sync
                
                **Time:** Today 2:00 PM - 3:00 PM  
                **Location:** Innovation Lab Room 402
                
                ### 🎯 Agenda
                1. Review current roadmap and milestones in [[Architecture Plan 2026]].
                2. Design token review with [[Design System V2]].
                3. Update team sprint tasks in [[Sprint Planning]].
                
                ### 📝 Notes & Decisions
                - [x] Reaffirmed 100% offline-first architecture.
                - [ ] Finalize Markdown YAML schema v2.
                - [ ] Roll out beta to early adopters.
            """.trimIndent()
        )

        val noteDesign = NoteEntity(
            id = "aether-design-04",
            title = "Design System V2",
            type = NoteType.BOOKMARK,
            createdAt = now,
            updatedAt = now,
            tags = listOf("design", "tokens", "m3", "glassmorphism"),
            aliases = listOf("UI Tokens"),
            colorHex = "#EC4899",
            bodyContent = """
                # 🎨 Design System V2: Frosted Glass & Neon OLED
                
                Aether features a distinct visual aesthetic blending deep OLED contrast with soft frosted glass surfaces.
                
                ### 🌈 Color Palette Tokens
                - **Deep Void:** `#090D16` / `#000000`
                - **Cyber Indigo:** `#6366F1`
                - **Electric Cyan:** `#06B6D4`
                - **Neon Rose:** `#EC4899`
                - **Emerald Pulse:** `#10B981`
                - **Solar Amber:** `#F59E0B`
                
                ### 📐 Component Guidelines
                - Minimum touch target: 48dp
                - Subtle 1dp border gradients for cards
                - Connected seamlessly with [[Welcome to Aether]] and [[Architecture Plan 2026]].
            """.trimIndent()
        )

        val noteSprint = NoteEntity(
            id = "aether-sprint-05",
            title = "Sprint Planning",
            type = NoteType.TEXT,
            createdAt = now,
            updatedAt = now,
            tags = listOf("sprint", "tasks", "active"),
            aliases = listOf("Current Sprint"),
            colorHex = "#8B5CF6",
            bodyContent = """
                # ⚡ Sprint Planning
                
                Sprint objectives and delivery milestones:
                
                ### 📋 Task Backlog
                - [x] Implement YAML Frontmatter engine
                - [x] Build Bento Grid note visualizer
                - [x] Implement dynamic [[Note Link]] autocomplete
                - [x] Connect calendar events with linked notes
                - [ ] Prepare demo notes for [[Product Strategy Sync]]
                - [ ] Verify test suite with [[Architecture Plan 2026]]
                
                See all features documented in [[Welcome to Aether]].
            """.trimIndent()
        )

        return listOf(noteWelcome, noteArchitecture, noteStrategy, noteDesign, noteSprint)
    }
}

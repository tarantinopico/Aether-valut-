package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VaultRepository
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NoteFilterType {
    ALL,
    TEXT,
    EVENT,
    BOOKMARK,
    DATABASE,
    PINNED
}

enum class SortOrder {
    UPDATED_DESC,
    CREATED_DESC,
    TITLE_ASC
}

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val filteredNotes: List<NoteEntity> = emptyList(),
    val backlinksCounts: Map<String, Int> = emptyMap(),
    val selectedFilter: NoteFilterType = NoteFilterType.ALL,
    val selectedTag: String? = null,
    val allTags: List<String> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val isLoading: Boolean = false
)

private data class FilterParams(
    val filter: NoteFilterType = NoteFilterType.ALL,
    val tag: String? = null,
    val query: String = "",
    val sort: SortOrder = SortOrder.UPDATED_DESC
)

class NotesViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(NoteFilterType.ALL)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.UPDATED_DESC)

    private val _filterParams = combine(
        _selectedFilter,
        _selectedTag,
        _searchQuery,
        _sortOrder
    ) { filter, tag, query, sort ->
        FilterParams(filter, tag, query, sort)
    }

    val uiState: StateFlow<NotesUiState> = combine(
        repository.notes,
        repository.backlinksMap,
        _filterParams,
        repository.isLoading
    ) { notes, backlinksMap, filterParams, isLoading ->
        val counts = backlinksMap.mapValues { it.value.size }
        val allTags = notes.flatMap { it.tags }.distinct().sorted()

        var result = notes

        // Filter by type or pinned
        result = when (filterParams.filter) {
            NoteFilterType.ALL -> result
            NoteFilterType.TEXT -> result.filter { it.type == NoteType.TEXT }
            NoteFilterType.EVENT -> result.filter { it.type == NoteType.EVENT || it.isEvent }
            NoteFilterType.BOOKMARK -> result.filter { it.type == NoteType.BOOKMARK }
            NoteFilterType.DATABASE -> result.filter { it.type == NoteType.DATABASE }
            NoteFilterType.PINNED -> result.filter { it.isPinned }
        }

        // Filter by tag
        if (!filterParams.tag.isNullOrBlank()) {
            result = result.filter { it.tags.any { t -> t.equals(filterParams.tag, ignoreCase = true) } }
        }

        // Search query
        if (filterParams.query.isNotBlank()) {
            val q = filterParams.query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                        it.bodyContent.lowercase().contains(q) ||
                        it.tags.any { t -> t.lowercase().contains(q) } ||
                        it.aliases.any { a -> a.lowercase().contains(q) }
            }
        }

        // Sort order
        result = when (filterParams.sort) {
            SortOrder.UPDATED_DESC -> result.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenByDescending { it.updatedAt }
            )
            SortOrder.CREATED_DESC -> result.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenByDescending { it.createdAt }
            )
            SortOrder.TITLE_ASC -> result.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenBy { it.title.lowercase() }
            )
        }

        NotesUiState(
            notes = notes,
            filteredNotes = result,
            backlinksCounts = counts,
            selectedFilter = filterParams.filter,
            selectedTag = filterParams.tag,
            allTags = allTags,
            searchQuery = filterParams.query,
            sortOrder = filterParams.sort,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun setFilter(filter: NoteFilterType) {
        _selectedFilter.value = filter
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun createNewNote(type: NoteType = NoteType.TEXT, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val title = when (type) {
                NoteType.TEXT -> "Untitled Note"
                NoteType.EVENT -> "New Event"
                NoteType.BOOKMARK -> "New Bookmark"
                NoteType.DATABASE -> "New Database"
            }
            val newNote = NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                type = type,
                bodyContent = "# $title\n\nStart writing here or link other notes using `[[Note Title]]`.\n"
            )
            val saved = repository.saveOrUpdateNote(newNote)
            onCreated(saved.id)
        }
    }
}

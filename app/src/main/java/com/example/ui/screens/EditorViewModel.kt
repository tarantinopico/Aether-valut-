package com.example.ui.screens

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VaultRepository
import com.example.domain.model.Backlink
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EditorUiState(
    val note: NoteEntity? = null,
    val isEditMode: Boolean = true,
    val showMetadataSheet: Boolean = false,
    val showSymlinkModal: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val backlinks: List<Backlink> = emptyList(),
    val availableNotes: List<NoteEntity> = emptyList(),
    val isSaved: Boolean = true
)

private data class DialogStates(
    val isEditMode: Boolean = true,
    val showMetadataSheet: Boolean = false,
    val showSymlinkModal: Boolean = false,
    val showDeleteDialog: Boolean = false
)

class EditorViewModel(
    private val initialNoteId: String,
    private val repository: VaultRepository
) : ViewModel() {

    private val _currentNote = MutableStateFlow<NoteEntity?>(null)
    private val _isEditMode = MutableStateFlow(true)
    private val _showMetadataSheet = MutableStateFlow(false)
    private val _showSymlinkModal = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)

    // Body text field value for cursor management
    val bodyFieldValue = MutableStateFlow(TextFieldValue(""))
    val titleValue = MutableStateFlow("")

    private val _dialogStates = combine(
        _isEditMode,
        _showMetadataSheet,
        _showSymlinkModal,
        _showDeleteDialog
    ) { isEdit, showMeta, showSym, showDelete ->
        DialogStates(isEdit, showMeta, showSym, showDelete)
    }

    val uiState: StateFlow<EditorUiState> = combine(
        _currentNote,
        _dialogStates,
        repository.backlinksMap,
        repository.notes
    ) { note, dialogStates, backlinksMap, allNotes ->
        val backlinks = if (note != null) backlinksMap[note.id] ?: emptyList() else emptyList()
        EditorUiState(
            note = note,
            isEditMode = dialogStates.isEditMode,
            showMetadataSheet = dialogStates.showMetadataSheet,
            showSymlinkModal = dialogStates.showSymlinkModal,
            showDeleteDialog = dialogStates.showDeleteDialog,
            backlinks = backlinks,
            availableNotes = allNotes.filter { it.id != note?.id }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditorUiState()
    )

    init {
        viewModelScope.launch {
            val existing = repository.getNoteById(initialNoteId)
            if (existing != null) {
                _currentNote.value = existing
                bodyFieldValue.value = TextFieldValue(existing.bodyContent)
                titleValue.value = existing.title
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setShowMetadataSheet(show: Boolean) {
        _showMetadataSheet.value = show
    }

    fun setShowSymlinkModal(show: Boolean) {
        _showSymlinkModal.value = show
    }

    fun setShowDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun onTitleChange(newTitle: String) {
        titleValue.value = newTitle
        val note = _currentNote.value ?: return
        val updated = note.copy(title = newTitle)
        _currentNote.value = updated
        persistNote(updated)
    }

    fun onBodyChange(newValue: TextFieldValue) {
        bodyFieldValue.value = newValue
        val note = _currentNote.value ?: return
        val updated = note.copy(bodyContent = newValue.text)
        _currentNote.value = updated
        persistNote(updated)
    }

    fun togglePin() {
        val note = _currentNote.value ?: return
        val updated = note.copy(isPinned = !note.isPinned)
        _currentNote.value = updated
        persistNote(updated)
    }

    fun updateMetadata(
        type: NoteType,
        tags: List<String>,
        aliases: List<String>,
        eventStart: String?,
        eventEnd: String?,
        isAllDay: Boolean,
        location: String?,
        colorHex: String?
    ) {
        val note = _currentNote.value ?: return
        val updated = note.copy(
            type = type,
            tags = tags,
            aliases = aliases,
            eventStart = eventStart,
            eventEnd = eventEnd,
            isAllDay = isAllDay,
            location = location,
            colorHex = colorHex
        )
        _currentNote.value = updated
        persistNote(updated)
    }

    fun toggleTaskCheckbox(lineIndex: Int) {
        val noteId = _currentNote.value?.id ?: return
        viewModelScope.launch {
            val updated = repository.toggleTaskCheckbox(noteId, lineIndex)
            if (updated != null) {
                _currentNote.value = updated
                bodyFieldValue.value = TextFieldValue(updated.bodyContent)
            }
        }
    }

    fun insertSymlink(targetTitle: String) {
        val textToInsert = "[[$targetTitle]]"
        insertTextAtCursor(textToInsert)
        _showSymlinkModal.value = false
    }

    fun insertHeading(level: Int) {
        val prefix = "#".repeat(level) + " "
        insertLinePrefix(prefix)
    }

    fun insertBold() {
        wrapSelection("**", "**")
    }

    fun insertItalic() {
        wrapSelection("*", "*")
    }

    fun insertChecklist() {
        insertLinePrefix("- [ ] ")
    }

    fun insertBullet() {
        insertLinePrefix("- ")
    }

    fun insertQuote() {
        insertLinePrefix("> ")
    }

    fun insertCode() {
        val current = bodyFieldValue.value
        val selection = current.selection
        if (selection.collapsed) {
            insertTextAtCursor("\n```\n// Code block\n```\n")
        } else {
            wrapSelection("`", "`")
        }
    }

    fun insertDateStamp() {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        insertTextAtCursor(dateStr)
    }

    private fun insertTextAtCursor(textToInsert: String) {
        val current = bodyFieldValue.value
        val sel = current.selection
        val newText = current.text.replaceRange(sel.start, sel.end, textToInsert)
        val newCursor = sel.start + textToInsert.length
        val newValue = TextFieldValue(newText, selection = TextRange(newCursor))
        onBodyChange(newValue)
    }

    private fun wrapSelection(prefix: String, suffix: String) {
        val current = bodyFieldValue.value
        val sel = current.selection
        val selectedText = current.text.substring(sel.start, sel.end)
        val replacement = "$prefix$selectedText$suffix"
        val newText = current.text.replaceRange(sel.start, sel.end, replacement)
        val newCursor = sel.start + replacement.length
        val newValue = TextFieldValue(newText, selection = TextRange(newCursor))
        onBodyChange(newValue)
    }

    private fun insertLinePrefix(prefix: String) {
        val current = bodyFieldValue.value
        val sel = current.selection
        val text = current.text

        val lineStart = text.lastIndexOf('\n', (sel.start - 1).coerceAtLeast(0)).let {
            if (it == -1) 0 else it + 1
        }

        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val newCursor = sel.start + prefix.length
        val newValue = TextFieldValue(newText, selection = TextRange(newCursor))
        onBodyChange(newValue)
    }

    private fun persistNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.saveOrUpdateNote(note)
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        val noteId = _currentNote.value?.id ?: return
        viewModelScope.launch {
            repository.deleteNote(noteId)
            onDeleted()
        }
    }
}

package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VaultRepository
import com.example.domain.model.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CalendarViewMode {
    MONTH,
    WEEK,
    DAY
}

data class CalendarUiState(
    val selectedDate: Date = Date(),
    val currentMonth: Calendar = Calendar.getInstance(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val allEvents: List<NoteEntity> = emptyList(),
    val selectedDateEvents: List<NoteEntity> = emptyList(),
    val eventDaysInMonth: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Date())
    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.notes,
        _selectedDate,
        _currentMonth,
        _viewMode,
        repository.isLoading
    ) { notes, selectedDate, month, viewMode, isLoading ->
        val events = notes.filter { it.isEvent || it.eventStart != null }

        val selectedDayStr = dayFormat.format(selectedDate)
        val selectedDateEvents = events.filter { note ->
            val start = note.eventStart ?: note.createdAt
            start.startsWith(selectedDayStr) || note.aliases.contains(selectedDayStr)
        }.sortedBy { it.eventStart ?: it.createdAt }

        val eventDays = events.mapNotNull { note ->
            val start = note.eventStart ?: note.createdAt
            if (start.length >= 10) start.take(10) else null
        }.toSet()

        CalendarUiState(
            selectedDate = selectedDate,
            currentMonth = month,
            viewMode = viewMode,
            allEvents = events,
            selectedDateEvents = selectedDateEvents,
            eventDaysInMonth = eventDays,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        val cal = Calendar.getInstance().apply { time = date }
        _currentMonth.value = cal
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun prevMonth() {
        val next = (_currentMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = next
    }

    fun nextMonth() {
        val next = (_currentMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = next
    }

    fun openOrCreateDailyNote(onNoteReady: (String) -> Unit) {
        viewModelScope.launch {
            val dailyNote = repository.getOrCreateDailyNote(_selectedDate.value)
            onNoteReady(dailyNote.id)
        }
    }

    fun createEvent(
        title: String,
        startIso: String,
        endIso: String?,
        isAllDay: Boolean,
        location: String?,
        colorHex: String,
        tags: List<String>,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val note = repository.createEventNote(
                title = title,
                startIso = startIso,
                endIso = endIso,
                isAllDay = isAllDay,
                location = location,
                colorHex = colorHex,
                tags = tags
            )
            onCreated(note.id)
        }
    }
}

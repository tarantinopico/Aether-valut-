package com.example.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultStats(
    val totalNotes: Int = 0,
    val totalEvents: Int = 0,
    val totalBacklinks: Int = 0,
    val totalSizeBytes: Long = 0L,
    val vaultLocation: String = "App-Internal Storage"
)

data class VaultSettingsUiState(
    val stats: VaultStats = VaultStats(),
    val customVaultUri: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

class VaultSettingsViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VaultSettingsUiState> = combine(
        repository.notes,
        repository.backlinksMap,
        repository.activeVaultUri,
        _message,
        repository.isLoading
    ) { notes, backlinksMap, uri, message, isLoading ->
        val totalEvents = notes.count { it.isEvent }
        val totalBacklinks = backlinksMap.values.sumOf { it.size }
        val totalBytes = notes.sumOf { it.bodyContent.toByteArray().size.toLong() }
        val location = if (!uri.isNullOrBlank()) "Custom SAF Folder" else "App Internal Sandboxed Vault"

        VaultSettingsUiState(
            stats = VaultStats(
                totalNotes = notes.size,
                totalEvents = totalEvents,
                totalBacklinks = totalBacklinks,
                totalSizeBytes = totalBytes,
                vaultLocation = location
            ),
            customVaultUri = uri,
            isLoading = isLoading,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultSettingsUiState()
    )

    fun onFolderSelected(uri: Uri?) {
        viewModelScope.launch {
            if (uri != null) {
                repository.setCustomVaultUri(uri.toString())
                _message.value = "Vault connected to SAF folder!"
            }
        }
    }

    fun resetToInternalStorage() {
        viewModelScope.launch {
            repository.setCustomVaultUri(null)
            _message.value = "Reset to internal app storage."
        }
    }

    fun reloadVault() {
        viewModelScope.launch {
            repository.loadVault()
            _message.value = "Vault reloaded."
        }
    }

    fun resetWithSampleVault() {
        viewModelScope.launch {
            repository.resetWithSampleVault()
            _message.value = "Sample notes re-generated."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

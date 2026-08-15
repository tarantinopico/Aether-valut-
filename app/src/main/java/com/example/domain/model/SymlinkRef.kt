package com.example.domain.model

enum class SymlinkType {
    NOTE_TITLE,
    EVENT_ID,
    NOTE_ID
}

data class SymlinkRef(
    val rawMatch: String,
    val target: String,
    val type: SymlinkType,
    val displayText: String
)

package com.example.domain.model

enum class NoteType(val rawValue: String) {
    TEXT("text"),
    BOOKMARK("bookmark"),
    DATABASE("database"),
    EVENT("event");

    companion object {
        fun fromString(value: String?): NoteType {
            return when (value?.trim()?.lowercase()) {
                "bookmark" -> BOOKMARK
                "database" -> DATABASE
                "event" -> EVENT
                else -> TEXT
            }
        }
    }
}

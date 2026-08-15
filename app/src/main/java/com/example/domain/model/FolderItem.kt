package com.example.domain.model

data class FolderItem(
    val path: String,
    val name: String,
    val noteCount: Int = 0
)

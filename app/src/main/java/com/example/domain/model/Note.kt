package com.example.domain.model

import android.net.Uri

data class Note(
    val id: String,
    val frontmatter: Frontmatter,
    val body: String,
    val fileName: String,
    val relativePath: String = "",
    val uri: Uri? = null,
    val outboundLinks: List<SymlinkRef> = emptyList(),
    val backlinks: List<String> = emptyList() // List of Note IDs or Titles that link to this Note
) {
    val title: String
        get() = frontmatter.title.ifBlank { fileName.removeSuffix(".md") }

    val type: NoteType
        get() = frontmatter.type

    val wordCount: Int
        get() {
            val words = body.trim().split(Regex("\\s+"))
            return if (words.size == 1 && words.first().isEmpty()) 0 else words.size
        }

    val checkboxStats: Pair<Int, Int>
        get() {
            var total = 0
            var checked = 0
            val pattern = Regex("""^(\s*[-*+]\s+\[([ xX])\])""", RegexOption.MULTILINE)
            pattern.findAll(body).forEach { match ->
                total++
                if (match.groupValues[2].equals("x", ignoreCase = true)) {
                    checked++
                }
            }
            return Pair(checked, total)
        }
}

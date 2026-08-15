package com.example.data.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.domain.model.NoteEntity
import com.example.domain.parser.YamlFrontmatterParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VaultStorageManager(private val context: Context) {

    private val defaultVaultDir: File
        get() = File(context.filesDir, "AetherVault").apply {
            if (!exists()) mkdirs()
        }

    suspend fun getStorageType(customUriString: String?): String {
        return if (!customUriString.isNullOrBlank()) "Storage Access Framework (SAF)" else "Local Secure Vault"
    }

    /**
     * Reads all markdown notes from current active vault directory (internal or SAF).
     */
    suspend fun loadAllNotes(customUriString: String?): List<NoteEntity> = withContext(Dispatchers.IO) {
        val notes = mutableListOf<NoteEntity>()

        if (!customUriString.isNullOrBlank()) {
            // Read from SAF DocumentFile tree
            try {
                val treeUri = Uri.parse(customUriString)
                val dir = DocumentFile.fromTreeUri(context, treeUri)
                if (dir != null && dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles()
                    for (docFile in files) {
                        val name = docFile.name ?: ""
                        if (name.endsWith(".md", ignoreCase = true) && docFile.isFile) {
                            context.contentResolver.openInputStream(docFile.uri)?.use { stream ->
                                val content = stream.bufferedReader().readText()
                                val note = YamlFrontmatterParser.parse(content, name)
                                notes.add(note)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Read from internal app directory
            val dir = defaultVaultDir
            val files = dir.listFiles { _, name -> name.endsWith(".md", ignoreCase = true) }
            if (files != null) {
                for (file in files) {
                    try {
                        val content = file.readText()
                        val note = YamlFrontmatterParser.parse(content, file.name)
                        notes.add(note)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        return@withContext notes
    }

    /**
     * Saves or updates a note file on disk.
     */
    suspend fun saveNote(note: NoteEntity, customUriString: String?): String = withContext(Dispatchers.IO) {
        val rawContent = YamlFrontmatterParser.serialize(note)
        val safeTitle = sanitizeFilename(note.title.ifBlank { "Untitled" })
        val targetFileName = "${safeTitle}.md"

        if (!customUriString.isNullOrBlank()) {
            val treeUri = Uri.parse(customUriString)
            val dir = DocumentFile.fromTreeUri(context, treeUri)
            if (dir != null && dir.exists()) {
                // Find existing file if note has existing fileName or matching targetFileName
                var docFile: DocumentFile? = null
                if (note.fileName.isNotBlank()) {
                    docFile = dir.findFile(note.fileName)
                }
                if (docFile == null) {
                    docFile = dir.findFile(targetFileName)
                }
                if (docFile == null) {
                    docFile = dir.createFile("text/markdown", targetFileName)
                }

                if (docFile != null) {
                    context.contentResolver.openOutputStream(docFile.uri, "wt")?.use { stream ->
                        stream.bufferedWriter().use { it.write(rawContent) }
                    }
                    return@withContext docFile.name ?: targetFileName
                }
            }
        }

        // Fallback to internal storage
        val dir = defaultVaultDir
        val targetFile = if (note.fileName.isNotBlank()) {
            val oldFile = File(dir, note.fileName)
            if (oldFile.exists() && note.fileName != targetFileName) {
                oldFile.delete()
            }
            File(dir, targetFileName)
        } else {
            File(dir, targetFileName)
        }

        targetFile.writeText(rawContent)
        return@withContext targetFile.name
    }

    /**
     * Deletes a note file from disk.
     */
    suspend fun deleteNote(note: NoteEntity, customUriString: String?): Boolean = withContext(Dispatchers.IO) {
        val fileName = note.fileName.ifBlank { "${sanitizeFilename(note.title)}.md" }

        if (!customUriString.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(customUriString)
                val dir = DocumentFile.fromTreeUri(context, treeUri)
                val docFile = dir?.findFile(fileName)
                if (docFile != null && docFile.exists()) {
                    return@withContext docFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val file = File(defaultVaultDir, fileName)
        if (file.exists()) {
            return@withContext file.delete()
        }
        return@withContext false
    }

    /**
     * Computes vault statistics.
     */
    suspend fun getVaultStats(customUriString: String?): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var fileCount = 0
        var totalBytes = 0L

        if (!customUriString.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(customUriString)
                val dir = DocumentFile.fromTreeUri(context, treeUri)
                dir?.listFiles()?.forEach { file ->
                    if (file.name?.endsWith(".md", ignoreCase = true) == true) {
                        fileCount++
                        totalBytes += file.length()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            defaultVaultDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".md", ignoreCase = true)) {
                    fileCount++
                    totalBytes += file.length()
                }
            }
        }

        return@withContext Pair(fileCount, totalBytes)
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "Untitled" }
    }
}

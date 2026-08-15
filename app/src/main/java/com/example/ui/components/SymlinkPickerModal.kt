package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.NoteEntity
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceContainerHigh
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SymlinkPickerModal(
    availableNotes: List<NoteEntity>,
    onSelectNote: (title: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = remember(searchQuery, availableNotes) {
        if (searchQuery.isBlank()) {
            availableNotes
        } else {
            availableNotes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.aliases.any { alias -> alias.contains(searchQuery, ignoreCase = true) } ||
                        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("symlink_picker_modal"),
            color = AetherSurfaceContainer,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Symlink",
                            tint = NeonIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Link to Note",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_symlink_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search note title or create new...", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonIndigo
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = AetherBorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("symlink_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // If user typed a new name not in existing notes, provide Create action
                if (searchQuery.isNotBlank() && availableNotes.none { it.title.equals(searchQuery.trim(), ignoreCase = true) }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectNote(searchQuery.trim()) }
                            .padding(bottom = 8.dp)
                            .testTag("create_new_linked_note_button"),
                        color = NeonIndigo.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = NeonIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create [[${searchQuery.trim()}]]",
                                color = NeonIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // List of existing notes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectNote(note.title) }
                                .testTag("symlink_item_${note.id}"),
                            color = AetherSurfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    if (note.tags.isNotEmpty()) {
                                        Text(
                                            text = note.tags.joinToString(" ") { "#$it" },
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = getNoteTypeIcon(note.type),
                                    contentDescription = note.type.displayName,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

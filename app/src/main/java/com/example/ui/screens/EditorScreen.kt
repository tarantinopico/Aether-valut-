package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Backlink
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.MarkdownRenderer
import com.example.ui.components.RichEditorToolbar
import com.example.ui.components.SymlinkPickerModal
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceContainerHigh
import com.example.ui.theme.AetherSurfaceDeep
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseColorHex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onNavigateToNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val bodyFieldValue by viewModel.bodyFieldValue.collectAsState()
    val titleValue by viewModel.titleValue.collectAsState()

    val note = uiState.note

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = AetherVoid,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AetherSurfaceDeep,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    // Mode switch & Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit / Preview Toggle
                        IconButton(
                            onClick = { viewModel.toggleEditMode() },
                            modifier = Modifier.testTag("toggle_preview_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = if (uiState.isEditMode) "Preview" else "Edit",
                                tint = if (uiState.isEditMode) TextSecondary else ElectricCyan
                            )
                        }

                        // Pin toggle
                        IconButton(
                            onClick = { viewModel.togglePin() },
                            modifier = Modifier.testTag("toggle_pin_button")
                        ) {
                            Icon(
                                imageVector = if (note?.isPinned == true) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin",
                                tint = if (note?.isPinned == true) NeonAmber else TextSecondary
                            )
                        }

                        // Metadata Sheet toggle
                        IconButton(
                            onClick = { viewModel.setShowMetadataSheet(true) },
                            modifier = Modifier.testTag("metadata_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Frontmatter Metadata",
                                tint = NeonIndigo
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = { viewModel.setShowDeleteDialog(true) },
                            modifier = Modifier.testTag("delete_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = NeonRose
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.isEditMode) {
                RichEditorToolbar(
                    onInsertHeading = { viewModel.insertHeading(it) },
                    onInsertBold = { viewModel.insertBold() },
                    onInsertItalic = { viewModel.insertItalic() },
                    onInsertChecklist = { viewModel.insertChecklist() },
                    onInsertBullet = { viewModel.insertBullet() },
                    onInsertQuote = { viewModel.insertQuote() },
                    onInsertCode = { viewModel.insertCode() },
                    onOpenSymlinkPicker = { viewModel.setShowSymlinkModal(true) },
                    onInsertDateStamp = { viewModel.insertDateStamp() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Note Title Input
            TextField(
                value = titleValue,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = { Text("Note Title...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMuted) },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("note_title_input")
            )

            // Tags & Meta Bar
            if (note != null && (note.tags.isNotEmpty() || !note.eventStart.isNullOrBlank())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!note.eventStart.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "📅 ${note.eventStart.take(10)}",
                                fontSize = 11.sp,
                                color = ElectricCyan,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    note.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AetherSurfaceContainerHigh
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(color = AetherBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Body Area (Edit Mode vs Preview Mode)
            if (uiState.isEditMode) {
                TextField(
                    value = bodyFieldValue,
                    onValueChange = { viewModel.onBodyChange(it) },
                    placeholder = {
                        Text(
                            text = "Write in Markdown...\n\nUse [[Note Title]] to link notes, - [ ] for tasks, or toolbar below.",
                            color = TextMuted,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("note_body_editor")
                )
            } else {
                // Interactive Markdown Renderer
                MarkdownRenderer(
                    markdown = bodyFieldValue.text,
                    modifier = Modifier.padding(16.dp),
                    onSymlinkClick = { targetRef ->
                        // Resolve target or create note
                        val target = uiState.availableNotes.find {
                            it.title.equals(targetRef, ignoreCase = true) || it.id == targetRef
                        }
                        if (target != null) {
                            onNavigateToNote(target.id)
                        } else {
                            viewModel.insertSymlink(targetRef)
                        }
                    },
                    onCheckboxClick = { lineIndex ->
                        viewModel.toggleTaskCheckbox(lineIndex)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Backlinks Section (Incoming Bidirectional Links)
            BacklinksSection(
                backlinks = uiState.backlinks,
                onBacklinkClick = { backlink -> onNavigateToNote(backlink.sourceNoteId) },
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Frontmatter Metadata Sheet
    if (uiState.showMetadataSheet && note != null) {
        MetadataBottomSheet(
            note = note,
            onDismiss = { viewModel.setShowMetadataSheet(false) },
            onSave = { type, tags, aliases, start, end, isAllDay, loc, color ->
                viewModel.updateMetadata(type, tags, aliases, start, end, isAllDay, loc, color)
                viewModel.setShowMetadataSheet(false)
            }
        )
    }

    // Symlink Picker Modal
    if (uiState.showSymlinkModal) {
        SymlinkPickerModal(
            availableNotes = uiState.availableNotes,
            onSelectNote = { title ->
                viewModel.insertSymlink(title)
            },
            onDismiss = { viewModel.setShowSymlinkModal(false) }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowDeleteDialog(false) },
            title = { Text("Delete Note?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${note?.title}'? This will permanently remove the .md file.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setShowDeleteDialog(false)
                        viewModel.deleteNote(onDeleted = onBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRose, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AetherSurfaceContainer
        )
    }
}

@Composable
private fun BacklinksSection(
    backlinks: List<Backlink>,
    onBacklinkClick: (Backlink) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Backlinks",
                tint = NeonViolet,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Backlinks (${backlinks.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (backlinks.isEmpty()) {
            Text(
                text = "No notes reference this note yet. Use [[${"Title"}]] in other notes to connect them.",
                fontSize = 12.sp,
                color = TextMuted
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                backlinks.forEach { backlink ->
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth().testTag("backlink_item_${backlink.sourceNoteId}"),
                        backgroundColor = AetherSurfaceContainerHigh,
                        borderColor = NeonViolet.copy(alpha = 0.3f),
                        onClick = { onBacklinkClick(backlink) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = backlink.sourceTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                            if (backlink.excerpt.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = backlink.excerpt,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetadataBottomSheet(
    note: NoteEntity,
    onDismiss: () -> Unit,
    onSave: (type: NoteType, tags: List<String>, aliases: List<String>, start: String?, end: String?, isAllDay: Boolean, loc: String?, color: String?) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf(note.type) }
    var tagsInput by remember { mutableStateOf(note.tags.joinToString(", ")) }
    var aliasesInput by remember { mutableStateOf(note.aliases.joinToString(", ")) }
    var eventStartInput by remember { mutableStateOf(note.eventStart ?: "") }
    var eventEndInput by remember { mutableStateOf(note.eventEnd ?: "") }
    var isAllDay by remember { mutableStateOf(note.isAllDay) }
    var locationInput by remember { mutableStateOf(note.location ?: "") }
    var selectedColorHex by remember { mutableStateOf(note.colorHex ?: "#6366F1") }

    val colorOptions = listOf("#6366F1", "#06B6D4", "#EC4899", "#10B981", "#F59E0B", "#8B5CF6")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AetherSurfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Note Frontmatter Metadata",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Strict YAML frontmatter properties saved in .md file",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note Type Selector
            Text(text = "Note Type", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteType.entries.forEach { type ->
                    val isSel = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) NeonIndigo.copy(alpha = 0.25f) else AetherSurfaceContainerHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonIndigo else AetherBorderSubtle),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedType = type }
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) NeonIndigo else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Aliases
            OutlinedTextField(
                value = aliasesInput,
                onValueChange = { aliasesInput = it },
                label = { Text("Aliases (alternative names for [[links]])") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Event Date
            OutlinedTextField(
                value = eventStartInput,
                onValueChange = { eventStartInput = it },
                label = { Text("Event Start ISO (e.g. 2026-08-15T14:00:00Z)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                label = { Text("Location") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonRose,
                    unfocusedBorderColor = AetherBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color
            Text(text = "Card Accent Color", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorOptions.forEach { hex ->
                    val color = parseColorHex(hex)
                    val isSel = selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val tags = tagsInput.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }
                    val aliases = aliasesInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(
                        selectedType,
                        tags,
                        aliases,
                        eventStartInput.trim().ifBlank { null },
                        eventEndInput.trim().ifBlank { null },
                        isAllDay,
                        locationInput.trim().ifBlank { null },
                        selectedColorHex
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Metadata", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import com.example.ui.components.BentoGrid
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceDeep
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteSelected: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AetherVoid,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(visible = showFabMenu, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FabMenuItem(
                            label = "Database Note",
                            color = NeonEmerald,
                            icon = Icons.Default.Storage,
                            tag = "fab_create_database",
                            onClick = {
                                showFabMenu = false
                                viewModel.createNewNote(NoteType.DATABASE) { onNoteSelected(NoteEntity(id = it)) }
                            }
                        )
                        FabMenuItem(
                            label = "Bookmark Note",
                            color = NeonRose,
                            icon = Icons.Default.Bookmark,
                            tag = "fab_create_bookmark",
                            onClick = {
                                showFabMenu = false
                                viewModel.createNewNote(NoteType.BOOKMARK) { onNoteSelected(NoteEntity(id = it)) }
                            }
                        )
                        FabMenuItem(
                            label = "Event Note",
                            color = ElectricCyan,
                            icon = Icons.Default.CalendarToday,
                            tag = "fab_create_event",
                            onClick = {
                                showFabMenu = false
                                viewModel.createNewNote(NoteType.EVENT) { onNoteSelected(NoteEntity(id = it)) }
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (showFabMenu) {
                            showFabMenu = false
                        } else {
                            viewModel.createNewNote(NoteType.TEXT) { onNoteSelected(NoteEntity(id = it)) }
                        }
                    },
                    containerColor = NeonIndigo,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Note",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Aether Vault",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${uiState.filteredNotes.size} notes & events",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_notes_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Notes",
                                tint = NeonIndigo
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(AetherSurfaceContainer)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Last Updated", color = TextPrimary) },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.UPDATED_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date Created", color = TextPrimary) },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.CREATED_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (A-Z)", color = TextPrimary) },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.TITLE_ASC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, content, [[links]], #tags...", color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonIndigo
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
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
                        .testTag("notes_search_bar")
                )
            }

            // Filter Tabs (All, Notes, Events, Bookmarks, Databases, Pinned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabItem(
                    label = "All",
                    isSelected = uiState.selectedFilter == NoteFilterType.ALL,
                    tag = "filter_all",
                    onClick = { viewModel.setFilter(NoteFilterType.ALL) }
                )
                FilterTabItem(
                    label = "Notes",
                    isSelected = uiState.selectedFilter == NoteFilterType.TEXT,
                    tag = "filter_text",
                    accentColor = NeonIndigo,
                    onClick = { viewModel.setFilter(NoteFilterType.TEXT) }
                )
                FilterTabItem(
                    label = "Events",
                    isSelected = uiState.selectedFilter == NoteFilterType.EVENT,
                    tag = "filter_event",
                    accentColor = ElectricCyan,
                    onClick = { viewModel.setFilter(NoteFilterType.EVENT) }
                )
                FilterTabItem(
                    label = "Bookmarks",
                    isSelected = uiState.selectedFilter == NoteFilterType.BOOKMARK,
                    tag = "filter_bookmark",
                    accentColor = NeonRose,
                    onClick = { viewModel.setFilter(NoteFilterType.BOOKMARK) }
                )
                FilterTabItem(
                    label = "Databases",
                    isSelected = uiState.selectedFilter == NoteFilterType.DATABASE,
                    tag = "filter_database",
                    accentColor = NeonEmerald,
                    onClick = { viewModel.setFilter(NoteFilterType.DATABASE) }
                )
                FilterTabItem(
                    label = "Pinned",
                    isSelected = uiState.selectedFilter == NoteFilterType.PINNED,
                    tag = "filter_pinned",
                    accentColor = NeonAmber,
                    onClick = { viewModel.setFilter(NoteFilterType.PINNED) }
                )
            }

            // Tag chips row
            if (uiState.allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.allTags.forEach { tag ->
                        val isSelected = uiState.selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonIndigo.copy(alpha = 0.25f) else AetherSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) NeonIndigo else AetherBorderSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectTag(tag) }
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonIndigo else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Bento Grid
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonIndigo)
                }
            } else if (uiState.filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notes found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create a note or adjust filters to explore your vault.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                BentoGrid(
                    notes = uiState.filteredNotes,
                    backlinksCounts = uiState.backlinksCounts,
                    onNoteClick = onNoteSelected,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    tag: String,
    accentColor: Color = NeonIndigo,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else AetherSurfaceDeep,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) accentColor else AetherBorderSubtle
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AetherSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NoteEntity
import com.example.domain.model.NoteType
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceGlass
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

@Composable
fun BentoGrid(
    notes: List<NoteEntity>,
    backlinksCounts: Map<String, Int> = emptyMap(),
    onNoteClick: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize().testTag("bento_grid"),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        items(items = notes, key = { it.id }) { note ->
            BentoNoteCard(
                note = note,
                backlinkCount = backlinksCounts[note.id] ?: 0,
                onClick = { onNoteClick(note) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BentoNoteCard(
    note: NoteEntity,
    backlinkCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHex(note.colorHex, fallback = when (note.type) {
        NoteType.TEXT -> NeonIndigo
        NoteType.EVENT -> ElectricCyan
        NoteType.BOOKMARK -> NeonRose
        NoteType.DATABASE -> NeonEmerald
    })

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
        borderColor = if (note.isPinned) NeonAmber.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.3f),
        backgroundColor = AetherSurfaceGlass,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Type Icon & Pin & Backlink badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = getNoteTypeIcon(note.type),
                        contentDescription = note.type.displayName,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (backlinkCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Backlinks",
                                tint = NeonViolet,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$backlinkCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                        }
                    }

                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = NeonAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Event info if applicable
            if (!note.eventStart.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = ElectricCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.eventStart.take(10),
                        fontSize = 11.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Excerpt
            val excerpt = note.getCleanExcerpt(80)
            if (excerpt.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = excerpt,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AetherBorderSubtle,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (note.tags.size > 3) {
                        Text(
                            text = "+${note.tags.size - 3}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getNoteTypeIcon(type: NoteType): ImageVector {
    return when (type) {
        NoteType.TEXT -> Icons.Default.Description
        NoteType.EVENT -> Icons.Default.CalendarToday
        NoteType.BOOKMARK -> Icons.Default.Bookmark
        NoteType.DATABASE -> Icons.Default.Storage
    }
}

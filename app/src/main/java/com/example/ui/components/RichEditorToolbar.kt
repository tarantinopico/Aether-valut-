package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherSurfaceContainerHigh
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RichEditorToolbar(
    onInsertHeading: (Int) -> Unit,
    onInsertBold: () -> Unit,
    onInsertItalic: () -> Unit,
    onInsertChecklist: () -> Unit,
    onInsertBullet: () -> Unit,
    onInsertQuote: () -> Unit,
    onInsertCode: () -> Unit,
    onOpenSymlinkPicker: () -> Unit,
    onInsertDateStamp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AetherSurfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symlink Insert button (Highlighted)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("toolbar_symlink_button"),
                color = NeonIndigo.copy(alpha = 0.2f),
                onClick = onOpenSymlinkPicker
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Insert Symlink",
                        tint = NeonIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " [[link]]",
                        color = NeonIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            ToolbarIconButton(
                icon = Icons.Default.Title,
                contentDescription = "Heading 1",
                tag = "toolbar_h1",
                onClick = { onInsertHeading(1) }
            )

            ToolbarIconButton(
                icon = Icons.Default.Title,
                contentDescription = "Heading 2",
                tag = "toolbar_h2",
                onClick = { onInsertHeading(2) },
                iconModifier = Modifier.size(16.dp)
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                tag = "toolbar_bold",
                onClick = onInsertBold
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "Italic",
                tag = "toolbar_italic",
                onClick = onInsertItalic
            )

            ToolbarIconButton(
                icon = Icons.Default.CheckBox,
                contentDescription = "Checklist",
                tag = "toolbar_checklist",
                onClick = onInsertChecklist,
                tint = ElectricCyan
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatListBulleted,
                contentDescription = "Bullet List",
                tag = "toolbar_bullet",
                onClick = onInsertBullet
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatQuote,
                contentDescription = "Quote",
                tag = "toolbar_quote",
                onClick = onInsertQuote
            )

            ToolbarIconButton(
                icon = Icons.Default.Code,
                contentDescription = "Code Block",
                tag = "toolbar_code",
                onClick = onInsertCode
            )

            ToolbarIconButton(
                icon = Icons.Default.CalendarToday,
                contentDescription = "Insert Date",
                tag = "toolbar_date",
                onClick = onInsertDateStamp
            )
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = TextSecondary,
    iconModifier: Modifier = Modifier.size(20.dp)
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = iconModifier
        )
    }
}

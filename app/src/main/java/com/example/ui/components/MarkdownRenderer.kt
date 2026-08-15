package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainerHigh
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier,
    onSymlinkClick: ((String) -> Unit)? = null,
    onCheckboxClick: ((Int) -> Unit)? = null
) {
    val lines = markdown.lines()
    var inCodeBlock = false
    val codeBlockBuilder = StringBuilder()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()

            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    CodeBlockItem(code = codeBlockBuilder.toString().trimEnd())
                    codeBlockBuilder.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockBuilder.append(rawLine).append("\n")
                return@forEachIndexed
            }

            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(
                        color = AetherBorderSubtle,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonIndigo,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    BlockquoteItem(quote = trimmed.removePrefix("> ").trim(), onSymlinkClick = onSymlinkClick)
                }
                trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]") -> {
                    val taskText = trimmed.drop(5).trim()
                    TaskCheckboxItem(
                        isChecked = false,
                        text = taskText,
                        onToggle = { onCheckboxClick?.invoke(index) },
                        onSymlinkClick = onSymlinkClick
                    )
                }
                trimmed.startsWith("- [x]") || trimmed.startsWith("* [x]") ||
                trimmed.startsWith("- [X]") || trimmed.startsWith("* [X]") -> {
                    val taskText = trimmed.drop(5).trim()
                    TaskCheckboxItem(
                        isChecked = true,
                        text = taskText,
                        onToggle = { onCheckboxClick?.invoke(index) },
                        onSymlinkClick = onSymlinkClick
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val bulletText = trimmed.drop(2).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        FormattedInlineText(
                            text = bulletText,
                            onSymlinkClick = onSymlinkClick
                        )
                    }
                }
                else -> {
                    FormattedInlineText(
                        text = line,
                        onSymlinkClick = onSymlinkClick
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockBuilder.isNotEmpty()) {
            CodeBlockItem(code = codeBlockBuilder.toString().trimEnd())
        }
    }
}

@Composable
private fun CodeBlockItem(code: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = AetherSurfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = ElectricCyan,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun BlockquoteItem(
    quote: String,
    onSymlinkClick: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NeonIndigo.copy(alpha = 0.08f))
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .background(NeonIndigo, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        FormattedInlineText(
            text = quote,
            isItalic = true,
            onSymlinkClick = onSymlinkClick
        )
    }
}

@Composable
private fun TaskCheckboxItem(
    isChecked: Boolean,
    text: String,
    onToggle: () -> Unit,
    onSymlinkClick: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(vertical = 2.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (isChecked) "Completed" else "Uncompleted",
            tint = if (isChecked) NeonEmerald else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FormattedInlineText(
            text = text,
            isStrikethrough = isChecked,
            onSymlinkClick = onSymlinkClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormattedInlineText(
    text: String,
    modifier: Modifier = Modifier,
    isItalic: Boolean = false,
    isStrikethrough: Boolean = false,
    onSymlinkClick: ((String) -> Unit)? = null
) {
    // Check if contains symlinks [[...]]
    val symlinkRegex = Regex("\\[\\[([^\\]|#]+)(?:#[^\\]|]+)?(?:\\|([^\\]]+))?\\]\\]")
    val hasSymlinks = symlinkRegex.containsMatchIn(text)

    if (!hasSymlinks) {
        val annotated = parseFormatting(text, isItalic, isStrikethrough)
        Text(
            text = annotated,
            color = if (isStrikethrough) TextMuted else TextPrimary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = modifier
        )
        return
    }

    // Split text by symlinks and render interactive FlowRow
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        var lastIdx = 0
        symlinkRegex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIdx) {
                val plainPart = text.substring(lastIdx, start)
                val annotated = parseFormatting(plainPart, isItalic, isStrikethrough)
                Text(
                    text = annotated,
                    color = if (isStrikethrough) TextMuted else TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            val targetRef = match.groupValues[1].trim()
            val displayText = match.groupValues[2].ifBlank { null }?.trim() ?: targetRef

            // Symlink pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSymlinkClick?.invoke(targetRef) },
                color = NeonIndigo.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Link",
                        tint = NeonIndigo,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = displayText,
                        color = NeonIndigo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            lastIdx = end
        }

        if (lastIdx < text.length) {
            val trailingPart = text.substring(lastIdx)
            val annotated = parseFormatting(trailingPart, isItalic, isStrikethrough)
            Text(
                text = annotated,
                color = if (isStrikethrough) TextMuted else TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

private fun parseFormatting(text: String, isItalicBase: Boolean, isStrikethroughBase: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var current = text

        // Process bold **text** and italic *text* and code `text`
        // Simple sequential inline parser
        var i = 0
        while (i < current.length) {
            if (current.startsWith("**", i)) {
                val end = current.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(current.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            } else if (current.startsWith("`", i)) {
                val end = current.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = ElectricCyan, background = AetherSurfaceContainerHigh)) {
                        append(current.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            } else if (current.startsWith("*", i)) {
                val end = current.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(current.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }

            val char = current[i]
            withStyle(
                SpanStyle(
                    fontStyle = if (isItalicBase) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isStrikethroughBase) TextDecoration.LineThrough else TextDecoration.None
                )
            ) {
                append(char)
            }
            i++
        }
    }
}

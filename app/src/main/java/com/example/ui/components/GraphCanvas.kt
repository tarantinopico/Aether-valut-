package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.domain.model.GraphData
import com.example.domain.model.GraphNode
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.parseColorHex

@Composable
fun GraphCanvas(
    graphData: GraphData,
    onNodeSelected: (GraphNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AetherVoid)
            .testTag("graph_canvas")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 3.5f)
                    offset += pan
                }
            }
            .pointerInput(graphData, scale, offset) {
                detectTapGestures { tapOffset ->
                    val centerX = size.width / 2f + offset.x
                    val centerY = size.height / 2f + offset.y

                    val tappedNode = graphData.nodes.find { node ->
                        val nodeScreenX = centerX + node.x * scale
                        val nodeScreenY = centerY + node.y * scale
                        val radius = (18f + node.connectionCount * 3f) * scale
                        val distance = kotlin.math.sqrt(
                            (tapOffset.x - nodeScreenX) * (tapOffset.x - nodeScreenX) +
                                    (tapOffset.y - nodeScreenY) * (tapOffset.y - nodeScreenY)
                        )
                        distance <= radius * 1.5f
                    }

                    if (tappedNode != null) {
                        onNodeSelected(tappedNode)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f + offset.x
            val centerY = size.height / 2f + offset.y

            val nodeMap = graphData.nodes.associateBy { it.id }

            // Draw Links
            for (link in graphData.links) {
                val source = nodeMap[link.sourceId]
                val target = nodeMap[link.targetId]
                if (source != null && target != null) {
                    val startX = centerX + source.x * scale
                    val startY = centerY + source.y * scale
                    val endX = centerX + target.x * scale
                    val endY = centerY + target.y * scale

                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                parseColorHex(source.colorHex).copy(alpha = 0.5f),
                                parseColorHex(target.colorHex).copy(alpha = 0.5f)
                            )
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f * scale
                    )
                }
            }

            // Draw Nodes
            for (node in graphData.nodes) {
                val nodeX = centerX + node.x * scale
                val nodeY = centerY + node.y * scale
                val baseRadius = (16f + (node.connectionCount * 3f).coerceAtMost(24f)) * scale
                val nodeColor = parseColorHex(node.colorHex)

                // Glow ring
                drawCircle(
                    color = nodeColor.copy(alpha = 0.2f),
                    radius = baseRadius * 1.6f,
                    center = Offset(nodeX, nodeY)
                )

                // Core circle
                drawCircle(
                    color = nodeColor,
                    radius = baseRadius,
                    center = Offset(nodeX, nodeY)
                )

                // Border
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = baseRadius,
                    center = Offset(nodeX, nodeY),
                    style = Stroke(width = 1.5f * scale)
                )

                // Text label
                if (scale > 0.6f) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = (12f * scale).coerceAtLeast(10f)
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val text = if (node.label.length > 15) node.label.take(13) + ".." else node.label
                        drawText(text, nodeX, nodeY + baseRadius + (16f * scale), paint)
                    }
                }
            }
        }
    }
}

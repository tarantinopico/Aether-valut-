package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GraphNode
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GraphCanvas
import com.example.ui.components.getNoteTypeIcon
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceDeep
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.parseColorHex

@Composable
fun GraphScreen(
    viewModel: GraphViewModel,
    onNavigateToNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AetherVoid
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive 2D Graph Canvas
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonIndigo)
                }
            } else {
                GraphCanvas(
                    graphData = uiState.graphData,
                    onNodeSelected = { node -> viewModel.selectNode(node) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Floating Stat Bar
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                backgroundColor = AetherSurfaceDeep.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Graph Network",
                            tint = NeonViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aether Connections",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatPill(count = uiState.graphData.nodes.size, label = "Nodes", color = ElectricCyan)
                        StatPill(count = uiState.graphData.links.size, label = "Links", color = NeonIndigo)
                    }
                }
            }

            // Bottom Selected Node Inspector Card
            AnimatedVisibility(
                visible = uiState.selectedNode != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                val selected = uiState.selectedNode
                if (selected != null) {
                    NodeInspectorCard(
                        node = selected,
                        onOpenNote = { onNavigateToNote(selected.id) },
                        onDismiss = { viewModel.selectNode(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(count: Int, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun NodeInspectorCard(
    node: GraphNode,
    onOpenNote: () -> Unit,
    onDismiss: () -> Unit
) {
    val nodeColor = parseColorHex(node.colorHex)

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_inspector_card"),
        backgroundColor = AetherSurfaceContainer,
        borderColor = nodeColor.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getNoteTypeIcon(node.type),
                        contentDescription = node.type.displayName,
                        tint = nodeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = node.label,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${node.connectionCount} active bidirectional connections",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenNote,
                colors = ButtonDefaults.buttonColors(containerColor = nodeColor, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_node_button")
            ) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Note in Editor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

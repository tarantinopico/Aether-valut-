package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.AetherBorderSubtle
import com.example.ui.theme.AetherSurfaceContainer
import com.example.ui.theme.AetherSurfaceContainerHigh
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

@Composable
fun VaultSettingsScreen(
    viewModel: VaultSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.onFolderSelected(uri)
        }
    }

    LaunchedEffect(uiState.message) {
        val msg = uiState.message
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AetherVoid,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NeonIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vault & Storage",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text(
                text = "Pure local Markdown files with YAML frontmatter",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Access Framework Card
            FrostedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AetherSurfaceContainer,
                borderColor = NeonIndigo.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "SAF Folder",
                                tint = NeonIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Storage Location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.customVaultUri != null) NeonEmerald.copy(alpha = 0.2f) else NeonIndigo.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (uiState.customVaultUri != null) "SAF Connected" else "Internal Vault",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.customVaultUri != null) NeonEmerald else NeonIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (uiState.customVaultUri != null) {
                            "Syncing with user-selected directory on device."
                        } else {
                            "Using app sandboxed storage. Connect any device folder to edit files in Obsidian/Logseq or other Markdown editors."
                        },
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openFolderLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonIndigo,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("select_saf_folder_button")
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = "Folder", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Folder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (uiState.customVaultUri != null) {
                            OutlinedButton(
                                onClick = { viewModel.resetToInternalStorage() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("reset_storage_button")
                            ) {
                                Text("Reset", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vault Statistics Grid
            Text(
                text = "Vault Metrics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Total Notes",
                    value = "${uiState.stats.totalNotes}",
                    icon = Icons.Default.Description,
                    color = NeonIndigo,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Calendar Events",
                    value = "${uiState.stats.totalEvents}",
                    icon = Icons.Default.Storage,
                    color = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Backlinks",
                    value = "${uiState.stats.totalBacklinks}",
                    icon = Icons.Default.Link,
                    color = NeonViolet,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Text Size",
                    value = "${uiState.stats.totalSizeBytes / 1024} KB",
                    icon = Icons.Default.CheckCircle,
                    color = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vault Actions Card
            FrostedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AetherSurfaceContainerHigh
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vault Maintenance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.reloadVault() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan.copy(alpha = 0.2f),
                            contentColor = ElectricCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reload_vault_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-index Markdown Vault", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.resetWithSampleVault() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRose.copy(alpha = 0.2f),
                            contentColor = NeonRose
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("regenerate_samples_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Sample", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-generate Starter Sample Notes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Markdown Frontmatter Architecture Reference
            FrostedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AetherSurfaceContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Architecture",
                            tint = NeonAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aether YAML Schema",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "--- \nid: \"<uuid>\"\ntitle: \"Sprint Note\"\ntype: text | event | bookmark | database\nevent_start: \"2026-08-15T14:00:00Z\"\ntags: [\"sprint\", \"design\"]\n---\n\n# Body content with [[links]]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier,
        backgroundColor = AetherSurfaceContainer,
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = TextSecondary)
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

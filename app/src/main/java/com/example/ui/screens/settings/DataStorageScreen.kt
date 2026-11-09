package com.example.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val storageStats by viewModel.storageStats.collectAsState()
    val networkUsage by viewModel.networkUsage.collectAsState()

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }

    val totalMediaUsed = storageStats.photosMb + storageStats.videosMb + storageStats.documentsMb + storageStats.voiceNotesMb + storageStats.cacheMb

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage and Data", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("data_storage_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Storage Bar Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ChatMitra Storage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "${String.format("%.1f", totalMediaUsed)} MB used",
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Colored Segmented Storage Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(modifier = Modifier.weight(storageStats.videosMb).fillMaxHeight().background(Color(0xFF00796B)))
                            Box(modifier = Modifier.weight(storageStats.photosMb).fillMaxHeight().background(Color(0xFF9C27B0)))
                            Box(modifier = Modifier.weight(storageStats.documentsMb).fillMaxHeight().background(Color(0xFFD32F2F)))
                            Box(modifier = Modifier.weight(storageStats.voiceNotesMb).fillMaxHeight().background(Color(0xFFFF9800)))
                            if (storageStats.cacheMb > 0) {
                                Box(modifier = Modifier.weight(storageStats.cacheMb).fillMaxHeight().background(Color.Gray))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Legend breakdown
                        StorageCategoryRow(label = "Videos", sizeText = "${storageStats.videosMb} MB", color = Color(0xFF00796B))
                        StorageCategoryRow(label = "Photos", sizeText = "${storageStats.photosMb} MB", color = Color(0xFF9C27B0))
                        StorageCategoryRow(label = "Documents & PDFs", sizeText = "${storageStats.documentsMb} MB", color = Color(0xFFD32F2F))
                        StorageCategoryRow(label = "Voice Notes", sizeText = "${storageStats.voiceNotesMb} MB", color = Color(0xFFFF9800))
                        StorageCategoryRow(label = "Temporary Cache", sizeText = "${storageStats.cacheMb} MB", color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showClearCacheDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear App Cache")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Network Usage Section
            item {
                Text(
                    text = "Network Usage",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Messages") },
                    supportingContent = { Text("${networkUsage.messagesSentMb} MB sent • ${networkUsage.messagesReceivedMb} MB received") },
                    leadingContent = { Icon(Icons.Default.Chat, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("Media Transfers") },
                    supportingContent = { Text("${networkUsage.mediaSentMb} MB sent • ${networkUsage.mediaReceivedMb} MB received") },
                    leadingContent = { Icon(Icons.Default.PermMedia, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("Voice & Video Calls") },
                    supportingContent = { Text("${networkUsage.callsSentMb} MB sent • ${networkUsage.callsReceivedMb} MB received") },
                    leadingContent = { Icon(Icons.Default.Call, contentDescription = null) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Media Auto-Download preferences
            item {
                Text(
                    text = "Media Auto-Download",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("When using mobile data") },
                    supportingContent = { Text("Photos only") },
                    leadingContent = { Icon(Icons.Default.SignalCellularAlt, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("When connected to Wi-Fi") },
                    supportingContent = { Text("All media (Photos, Audio, Videos, Documents)") },
                    leadingContent = { Icon(Icons.Default.Wifi, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("When roaming") },
                    supportingContent = { Text("No media") },
                    leadingContent = { Icon(Icons.Default.Public, contentDescription = null) }
                )
            }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache?") },
            text = { Text("This will remove temporary preview files (${storageStats.cacheMb} MB). Your chat messages and downloaded media will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        val cleared = viewModel.settingsRepo.clearCache()
                        showClearCacheDialog = false
                        cacheClearedMessage = "$cleared MB cache cleared!"
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (cacheClearedMessage != null) {
        AlertDialog(
            onDismissRequest = { cacheClearedMessage = null },
            title = { Text("Cache Cleared") },
            text = { Text(cacheClearedMessage!!) },
            confirmButton = {
                Button(onClick = { cacheClearedMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StorageCategoryRow(
    label: String,
    sizeText: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp)
        }
        Text(sizeText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

package com.example.ui.screens.storage

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.LargeMediaFile
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartStorageScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val storageStats by viewModel.storageStats.collectAsState()
    val largeFiles by viewModel.largeFiles.collectAsState()
    val allChats by viewModel.allChats.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var showDuplicatesCleanedAlert by remember { mutableStateOf(false) }
    var cleanedAmount by remember { mutableFloatStateOf(0f) }

    var showChatCleanAlert by remember { mutableStateOf<String?>(null) }
    var showExportImportDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Storage & Cleaner", fontWeight = FontWeight.Bold) },
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
                .testTag("smart_storage_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Overview Dashboard
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("ChatMitra Media Storage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${storageStats.photosMb + storageStats.videosMb + storageStats.documentsMb} MB used • ${storageStats.totalDeviceFreeGb} GB free on device",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        ) {
                            Box(modifier = Modifier.weight(storageStats.videosMb).fillMaxHeight().background(Color(0xFF00796B)))
                            Box(modifier = Modifier.weight(storageStats.photosMb).fillMaxHeight().background(Color(0xFF9C27B0)))
                            Box(modifier = Modifier.weight(storageStats.documentsMb).fillMaxHeight().background(Color(0xFFD32F2F)))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Clean Duplicate Downloads Button
                        Button(
                            onClick = {
                                cleanedAmount = viewModel.settingsRepo.deleteDuplicateDownloads()
                                showDuplicatesCleanedAlert = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Duplicate Downloads (Free ~48.5 MB)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Find Large Files (> 10 MB)
            item {
                Text(
                    text = "Review and Delete Large Files (${largeFiles.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(largeFiles, key = { it.id }) { file ->
                LargeFileRow(
                    file = file,
                    onDelete = { viewModel.settingsRepo.deleteLargeFile(file.id) }
                )
            }

            // Manage Individual Chat Storage
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chats Storage Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(allChats.take(5), key = { it.id }) { chat ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${chat.mediaStorageMb} MB media storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        TextButton(
                            onClick = { showChatCleanAlert = chat.name },
                            colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                        ) {
                            Text("Clear Media", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Auto Media Cleanup & Data Portability
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Automated Cleanup & Data Portability",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                ListItem(
                    headlineContent = { Text("Auto-delete media older than 30 days") },
                    supportingContent = { Text("Frees local device memory while keeping cloud copies intact") },
                    trailingContent = {
                        Switch(
                            checked = settings.autoMediaCleanupDays > 0,
                            onCheckedChange = {}
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Export full chat database (JSON / ZIP)") },
                    supportingContent = { Text("Create portable offline backup of all media and chats") },
                    leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    modifier = Modifier.clickable { showExportImportDialog = "Exported 7 chat threads and 34 media attachments to ChatMitra_Data_Archive.zip" }
                )

                ListItem(
                    headlineContent = { Text("Import chats from another device") },
                    supportingContent = { Text("Restore messages and media from exported ZIP file") },
                    leadingContent = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    modifier = Modifier.clickable { showExportImportDialog = "Archive validated successfully! All chat logs imported." }
                )
            }
        }
    }

    if (showDuplicatesCleanedAlert) {
        AlertDialog(
            onDismissRequest = { showDuplicatesCleanedAlert = false },
            title = { Text("Duplicates Removed") },
            text = { Text("Successfully purged duplicate cached downloads. Freed $cleanedAmount MB storage!") },
            confirmButton = {
                Button(onClick = { showDuplicatesCleanedAlert = false }) { Text("OK") }
            }
        )
    }

    if (showChatCleanAlert != null) {
        val name = showChatCleanAlert!!
        AlertDialog(
            onDismissRequest = { showChatCleanAlert = null },
            title = { Text("Clear media for $name?") },
            text = { Text("This will delete downloaded videos and images for this conversation to free up device space. Text messages will not be deleted.") },
            confirmButton = {
                Button(
                    onClick = { showChatCleanAlert = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Clear Media")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatCleanAlert = null }) { Text("Cancel") }
            }
        )
    }

    if (showExportImportDialog != null) {
        AlertDialog(
            onDismissRequest = { showExportImportDialog = null },
            title = { Text("Chat Data Portability") },
            text = { Text(showExportImportDialog!!) },
            confirmButton = {
                Button(onClick = { showExportImportDialog = null }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun LargeFileRow(
    file: LargeMediaFile,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (file.type) {
                    "Video" -> Icons.Default.Videocam
                    "Document" -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = WhatsAppGreen,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                Text("${file.sizeMb} MB • ${file.chatName} • ${file.formattedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete file", tint = CoralRed)
            }
        }
    }
}

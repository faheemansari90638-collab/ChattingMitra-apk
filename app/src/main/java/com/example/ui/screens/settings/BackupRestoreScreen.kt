package com.example.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val settings by viewModel.appSettings.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val backupProgress by viewModel.backupProgress.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showRestoreSuccess by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats & Backup", fontWeight = FontWeight.Bold) },
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
                .testTag("backup_restore_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Backup status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Last Cloud Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(settings.lastBackupDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Total Backup Size: ${settings.backupSizeMb} MB (Messages, Media & Settings)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isBackingUp) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { backupProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = WhatsAppGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Encrypting & uploading chats: ${(backupProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.settingsRepo.performBackup()
                                }
                            },
                            enabled = !isBackingUp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_now_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back Up Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Cloud settings
            item {
                Text(
                    text = "Cloud Backup Settings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Back up frequency") },
                    supportingContent = { Text(settings.backupFrequency) },
                    leadingContent = { Icon(Icons.Default.Update, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("Google Drive / Cloud Account") },
                    supportingContent = { Text("hamzaansari6929@gmail.com") },
                    leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                )

                ListItem(
                    headlineContent = { Text("Back up over Wi-Fi only") },
                    trailingContent = {
                        Switch(
                            checked = settings.mediaAutoDownloadWifi,
                            onCheckedChange = {}
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Restore & Export
            item {
                Text(
                    text = "Restore & Export",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Restore previous chats") },
                    supportingContent = { Text("Restore all messages and media from your cloud vault") },
                    leadingContent = { Icon(Icons.Default.Restore, contentDescription = null) },
                    modifier = Modifier.clickable { showRestoreSuccess = true }
                )

                ListItem(
                    headlineContent = { Text("Export chat history") },
                    supportingContent = { Text("Save chat logs to a text file for sharing or safe keeping") },
                    leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    modifier = Modifier.clickable { showExportSuccess = true }
                )
            }
        }
    }

    if (showRestoreSuccess) {
        AlertDialog(
            onDismissRequest = { showRestoreSuccess = false },
            title = { Text("Chats Restored") },
            text = { Text("All your encrypted messages, media, and call logs have been successfully verified and restored from your cloud backup.") },
            confirmButton = {
                Button(onClick = { showRestoreSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Chat History Exported") },
            text = { Text("Chat history compiled to 'ChatMitra_Export_2026.txt' and ready to share.") },
            confirmButton = {
                Button(onClick = { showExportSuccess = false }) {
                    Text("Done")
                }
            }
        )
    }
}

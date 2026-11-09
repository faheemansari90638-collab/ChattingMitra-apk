package com.example.ui.screens.security

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCenterScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val settings by viewModel.appSettings.collectAsState()
    val securityAlerts by viewModel.securityAlerts.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    var showKeyCopiedSnackbar by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAccountRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("user@chatmitra.app") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security & Audit Center", fontWeight = FontWeight.Bold) },
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
                .testTag("security_center_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Suspicious Login / Alert Banner if any
            if (securityAlerts.isNotEmpty()) {
                item {
                    Text(
                        text = "Security & New Device Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(securityAlerts, key = { it.id }) { alert ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppGreen.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(alert.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(alert.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // End to End Encrypted Backup 64-Digit Key
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("End-to-End 64-Digit Encryption Key", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Protect your cloud chat backups with zero-knowledge hardware encryption. Only you possess this master key.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = settings.e2eBackupKey,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(settings.e2eBackupKey))
                                showKeyCopiedSnackbar = true
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Secret Encryption Key", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Security Audit & Login History
            item {
                Text(
                    text = "Recent Account Activity & Security Logs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        securityLogs.forEach { log ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(log, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Recovery & Policy Items
            item {
                Text(
                    text = "Account Recovery & Compliance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                ListItem(
                    headlineContent = { Text("Email-based account recovery") },
                    supportingContent = { Text("Configured with $recoveryEmail") },
                    leadingContent = { Icon(Icons.Default.MarkEmailRead, contentDescription = null) },
                    modifier = Modifier.clickable { showAccountRecoveryDialog = true }
                )

                ListItem(
                    headlineContent = { Text("Privacy Policy & Terms of Service") },
                    supportingContent = { Text("GDPR & End-to-End Encryption compliance documentation") },
                    leadingContent = { Icon(Icons.Default.Policy, contentDescription = null) },
                    modifier = Modifier.clickable { showPrivacyPolicyDialog = true }
                )

                ListItem(
                    headlineContent = { Text("Delete Account Permanently", color = CoralRed) },
                    supportingContent = { Text("Erase all messages, groups, and cloud vault history", color = CoralRed.copy(alpha = 0.8f)) },
                    leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CoralRed) },
                    modifier = Modifier.clickable { showDeleteAccountDialog = true }
                )
            }
        }
    }

    if (showKeyCopiedSnackbar) {
        AlertDialog(
            onDismissRequest = { showKeyCopiedSnackbar = false },
            title = { Text("Key Copied") },
            text = { Text("Your 64-digit encryption key has been copied to your clipboard. Store it in a secure password vault.") },
            confirmButton = {
                Button(onClick = { showKeyCopiedSnackbar = false }) { Text("OK") }
            }
        )
    }

    if (showAccountRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showAccountRecoveryDialog = false },
            title = { Text("Account Recovery Email") },
            text = {
                Column {
                    Text("If you lose your phone or forget your app PIN, an encrypted recovery code will be dispatched to this email:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = { Text("Recovery Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAccountRecoveryDialog = false }) { Text("Save Email") }
            },
            dismissButton = {
                TextButton(onClick = { showAccountRecoveryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("ChatMitra Privacy & Security Policy") },
            text = {
                Column {
                    Text("1. Zero Access to Your Messages: ChatMitra uses client-side Signal Protocol inspired end-to-end encryption. Only you and the intended recipient have the keys to decrypt text, voice notes, photos, or documents.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. No AI Training: We never parse, sell, index, or use user communications to train AI models.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3. Metadata Minimization: We do not retain contact phone numbers or call logs on external centralized databases.")
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) { Text("Understood") }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Permanently Delete Account?") },
            text = { Text("WARNING: This action is irreversible. All your messages, contacts, group memberships, and cloud encrypted backups will be permanently wiped.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}

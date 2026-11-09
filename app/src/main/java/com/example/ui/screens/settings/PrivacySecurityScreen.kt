package com.example.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val settings by viewModel.appSettings.collectAsState()

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    var showVisibilityDialog by remember { mutableStateOf<String?>(null) } // "last_seen", "profile_photo", "status"
    var selectedOption by remember { mutableStateOf("Everyone") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
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
                .testTag("privacy_settings_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // App Lock Section
            item {
                Text(
                    text = "App Lock & Security",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unlock with Fingerprint or PIN", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "When enabled, you'll need to enter your PIN to open ChatMitra",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isAppLockEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.settingsRepo.setAppLock(isChecked, settings.appLockPin)
                        },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }

                if (settings.isAppLockEnabled) {
                    ListItem(
                        headlineContent = { Text("Change App Lock PIN") },
                        supportingContent = { Text("Current PIN: ${settings.appLockPin}") },
                        leadingContent = { Icon(Icons.Default.Password, contentDescription = null) },
                        modifier = Modifier.clickable { showPinChangeDialog = true }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            // Who can see my personal info
            item {
                Text(
                    text = "Who can see my personal info",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Last Seen & Online") },
                    supportingContent = { Text(settings.lastSeenPrivacy) },
                    modifier = Modifier.clickable {
                        selectedOption = settings.lastSeenPrivacy
                        showVisibilityDialog = "last_seen"
                    }
                )

                ListItem(
                    headlineContent = { Text("Profile Photo") },
                    supportingContent = { Text(settings.profilePhotoPrivacy) },
                    modifier = Modifier.clickable {
                        selectedOption = settings.profilePhotoPrivacy
                        showVisibilityDialog = "profile_photo"
                    }
                )

                ListItem(
                    headlineContent = { Text("Status") },
                    supportingContent = { Text(settings.statusPrivacy) },
                    modifier = Modifier.clickable {
                        selectedOption = settings.statusPrivacy
                        showVisibilityDialog = "status"
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            // Disappearing Messages & Two-Step Verification
            item {
                Text(
                    text = "Disappearing Messages",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Default message timer") },
                    supportingContent = { Text("Start new chats with disappearing messages set to off") },
                    leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Two-Step Verification",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Two-step verification PIN") },
                    supportingContent = { Text("Enabled • Additional protection when registering your number") },
                    leadingContent = { Icon(Icons.Default.Security, contentDescription = null, tint = WhatsAppGreen) }
                )

                ListItem(
                    headlineContent = { Text("Blocked Contacts") },
                    supportingContent = { Text("0 contacts blocked") },
                    leadingContent = { Icon(Icons.Default.Block, contentDescription = null) }
                )
            }
        }
    }

    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Set New 4-Digit PIN") },
            text = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.length <= 4) newPinInput = it },
                    label = { Text("Enter 4 digits") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            viewModel.settingsRepo.setAppLock(true, newPinInput)
                            showPinChangeDialog = false
                            newPinInput = ""
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showVisibilityDialog != null) {
        val title = when (showVisibilityDialog) {
            "last_seen" -> "Last Seen & Online Visibility"
            "profile_photo" -> "Profile Photo Visibility"
            else -> "Status Visibility"
        }
        val options = listOf("Everyone", "My Contacts", "Nobody")

        AlertDialog(
            onDismissRequest = { showVisibilityDialog = null },
            title = { Text(title) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (showVisibilityDialog) {
                                        "last_seen" -> viewModel.settingsRepo.setPrivacy(option, settings.profilePhotoPrivacy, settings.statusPrivacy)
                                        "profile_photo" -> viewModel.settingsRepo.setPrivacy(settings.lastSeenPrivacy, option, settings.statusPrivacy)
                                        "status" -> viewModel.settingsRepo.setPrivacy(settings.lastSeenPrivacy, settings.profilePhotoPrivacy, option)
                                    }
                                    showVisibilityDialog = null
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVisibilityDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

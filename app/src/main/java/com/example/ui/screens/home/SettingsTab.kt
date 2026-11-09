package com.example.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var showQrDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.name) }
    var editStatus by remember { mutableStateOf(currentUser.statusText) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(AppScreen.AdvancedProfile)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        name = currentUser.name,
                        avatarColor = currentUser.avatarColor,
                        avatarUrl = currentUser.avatarUrl,
                        size = 64.dp,
                        isOnline = true
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser.statusText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser.phoneNumber,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier.testTag("qr_code_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "My QR Code",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Premium Promo Card if not premium
        if (!settings.isPremium) {
            item {
                Card(
                    onClick = { viewModel.navigateTo(AppScreen.Premium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("upgrade_premium_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WhatsAppGreen.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ChatMitra Premium",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ad-free experience, 4GB files & 3D stickers",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Settings items
        item {
            SettingsNavRow(
                icon = Icons.Outlined.AccountCircle,
                title = "Account & Profile",
                subtitle = "Avatars, custom status emoji, bio, verification badge",
                onClick = { viewModel.navigateTo(AppScreen.AdvancedProfile) },
                testTag = "settings_account_profile_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Devices,
                title = "Linked Devices",
                subtitle = "Active sessions & ChatMitra Web QR sync",
                onClick = { viewModel.navigateTo(AppScreen.LinkedDevices) },
                testTag = "settings_linked_devices_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Campaign,
                title = "Channels & Communities",
                subtitle = "Follow broadcasts, explore public channels & communities",
                onClick = { viewModel.navigateTo(AppScreen.ChannelsAndCommunities) },
                testTag = "settings_channels_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.CleaningServices,
                title = "Smart Storage & Cleanup",
                subtitle = "Find large files >10MB, duplicate media cleaner",
                onClick = { viewModel.navigateTo(AppScreen.SmartStorage) },
                testTag = "settings_smart_storage_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Security,
                title = "Security Center & Alerts",
                subtitle = "64-digit key, login history, audit logs, recovery",
                onClick = { viewModel.navigateTo(AppScreen.SecurityCenter) },
                testTag = "settings_security_center_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Lock,
                title = "Privacy & App Lock",
                subtitle = "Fingerprint/PIN lock, disappearing messages, blocked",
                onClick = { viewModel.navigateTo(AppScreen.PrivacySecurity) },
                testTag = "settings_privacy_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Palette,
                title = "Themes & Display",
                subtitle = "Dark mode, wallpapers, font size, bubble styles",
                onClick = { viewModel.navigateTo(AppScreen.ThemeCustomization) },
                testTag = "settings_theme_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Backup,
                title = "Chats & Backup",
                subtitle = "Cloud chat backup, export chat, auto-download",
                onClick = { viewModel.navigateTo(AppScreen.BackupRestore) },
                testTag = "settings_backup_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Storage,
                title = "Data & Storage Usage",
                subtitle = "Network meters, storage breakdown, clear cache",
                onClick = { viewModel.navigateTo(AppScreen.DataStorage) },
                testTag = "settings_storage_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Translate,
                title = "Language & Translation",
                subtitle = "${settings.selectedLanguage} • In-chat live translation",
                onClick = { viewModel.navigateTo(AppScreen.ThemeCustomization) },
                testTag = "settings_language_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications & Sounds",
                subtitle = "${settings.notificationSound} • Vibrate: ${if (settings.vibrateEnabled) "On" else "Off"}",
                onClick = { viewModel.navigateTo(AppScreen.ThemeCustomization) },
                testTag = "settings_notifications_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.MonetizationOn,
                title = "Premium & Subscription",
                subtitle = if (settings.isPremium) "Active Plan • Ad-Free" else "Unlock premium features",
                onClick = { viewModel.navigateTo(AppScreen.Premium) },
                testTag = "settings_premium_item"
            )

            SettingsNavRow(
                icon = Icons.Outlined.AdminPanelSettings,
                title = "Admin Dashboard & Business",
                subtitle = "Manage verification, channels, tickets & analytics",
                onClick = { viewModel.navigateTo(AppScreen.AdminDashboard) },
                testTag = "settings_admin_item"
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            // Logout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("settings_logout_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Log out",
                    tint = CoralRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Log Out of ChatMitra",
                    color = CoralRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it },
                        label = { Text("About / Status") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.chatRepo.updateCurrentUser(
                            name = editName.ifBlank { currentUser.name },
                            statusText = editStatus.ifBlank { currentUser.statusText },
                            phone = currentUser.phoneNumber
                        )
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("My ChatMitra QR Code") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentUser.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Scan to add me on ChatMitra",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?") },
            text = { Text("Are you sure you want to log out of ChatMitra on this device? Your local backups will remain safe.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

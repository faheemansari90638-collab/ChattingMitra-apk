package com.example.ui.screens.home

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.contacts.PhoneContact
import com.example.data.contacts.PhoneContactsManager
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTab(
    viewModel: MainViewModel,
    onContactClick: (name: String, phone: String) -> Unit,
    onStartCall: (name: String, phone: String, avatarColor: Long, isVideo: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var alsoSaveToPhoneAddressBook by remember { mutableStateOf(true) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    var contactToDelete by remember { mutableStateOf<PhoneContact?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var hasContactsPermission by remember {
        mutableStateOf(PhoneContactsManager.hasContactsPermission(context))
    }

    val contactsList = remember {
        mutableStateListOf<PhoneContact>().apply {
            addAll(PhoneContactsManager.loadDeviceContacts(context))
        }
    }

    // Permission Launcher for READ_CONTACTS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
        if (isGranted) {
            contactsList.clear()
            contactsList.addAll(PhoneContactsManager.loadDeviceContacts(context))
        }
    }

    fun syncContacts() {
        isSyncing = true
        contactsList.clear()
        contactsList.addAll(PhoneContactsManager.loadDeviceContacts(context))
        isSyncing = false
    }

    val filtered = contactsList.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phoneNumber.contains(searchQuery)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search phone contacts or numbers...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("contacts_search_field")
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Permission Alert Banner if not granted
            if (!hasContactsPermission) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Connect Phone Address Book",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Grant contacts permission to automatically sync and chat with friends on your phone.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                                ) {
                                    Text("Allow Contacts Access", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Action Buttons
            item {
                ListItem(
                    headlineContent = { Text("New contact", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Add to ChatMitra & Phone address book") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = Color.White)
                        }
                    },
                    modifier = Modifier
                        .clickable { showAddContactDialog = true }
                        .testTag("add_contact_button")
                )

                ListItem(
                    headlineContent = { Text("Sync phone contacts", fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text(if (isSyncing) "Syncing with Android Contacts Provider..." else "${contactsList.size} contacts synchronized")
                    },
                    trailingContent = {
                        IconButton(onClick = { syncContacts() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WhatsAppGreen)
                        }
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    },
                    modifier = Modifier.clickable { syncContacts() }
                )

                ListItem(
                    headlineContent = { Text("Invite friends", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Share download link via SMS or social apps") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    },
                    modifier = Modifier.clickable { showInviteDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phone Contacts (${filtered.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${filtered.count { it.isOnChatMitra }} on ChatMitra",
                        fontSize = 11.sp,
                        color = WhatsAppGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(filtered, key = { it.id }) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactClick(contact.name, contact.phoneNumber) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("contact_item_${contact.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        name = contact.name,
                        avatarColor = contact.avatarColor,
                        avatarUrl = contact.photoUri ?: "",
                        size = 48.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (contact.isOnChatMitra) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = WhatsAppGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ChatMitra",
                                        color = WhatsAppGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = contact.phoneNumber,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contact.status,
                            fontSize = 12.sp,
                            color = if (contact.isOnChatMitra) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF00796B),
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Phone Dialer Button (connects directly to phone dialer)
                        IconButton(
                            onClick = { PhoneContactsManager.openPhoneDialer(context, contact.phoneNumber) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneForwarded,
                                contentDescription = "Open Phone Dialer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // ChatMitra Voice Call
                        IconButton(
                            onClick = { onStartCall(contact.name, contact.phoneNumber, contact.avatarColor, false) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // ChatMitra Video Call
                        IconButton(
                            onClick = { onStartCall(contact.name, contact.phoneNumber, contact.avatarColor, true) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // More options: view details or delete from ChatMitra
                        Box {
                            var showContactMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showContactMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = showContactMenu,
                                onDismissRequest = { showContactMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open in Phone Contacts") },
                                    leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                                    onClick = {
                                        showContactMenu = false
                                        PhoneContactsManager.openPhoneContactDetails(context, contact.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete from ChatMitra", color = CoralRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed) },
                                    onClick = {
                                        showContactMenu = false
                                        contactToDelete = contact
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add New Contact Dialog
    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add New Contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 98765 00000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = alsoSaveToPhoneAddressBook,
                            onCheckedChange = { alsoSaveToPhoneAddressBook = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Also add to phone's Android address book", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val newContact = PhoneContact(
                                id = "c_${System.currentTimeMillis()}",
                                name = newName.trim(),
                                phoneNumber = newPhone.trim(),
                                photoUri = null,
                                isOnChatMitra = true,
                                status = "Hey there! I am using ChatMitra",
                                avatarColor = 0xFF00796B
                            )
                            contactsList.add(0, newContact)
                            viewModel.chatRepo.addContactChat(newName.trim(), newPhone.trim())

                            if (alsoSaveToPhoneAddressBook) {
                                PhoneContactsManager.launchAddNewContact(context, newName.trim(), newPhone.trim())
                            }

                            newName = ""
                            newPhone = ""
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Contact Confirmation Dialog (explicitly guards phone contact vs app contact)
    if (showDeleteConfirmDialog && contactToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                contactToDelete = null
            },
            title = { Text("Delete Contact from ChatMitra?") },
            text = {
                Text(
                    "Are you sure you want to remove \"${contactToDelete?.name}\" from ChatMitra?\n\nNote: This will remove the conversation and contact entry from ChatMitra only. Your device's native phone contacts will NOT be altered."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        contactsList.removeAll { it.id == contactToDelete?.id }
                        showDeleteConfirmDialog = false
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Delete from ChatMitra")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        contactToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Friends to ChatMitra") },
            text = {
                Text(
                    "Share this invitation link with your friends to join ChatMitra:\n\n\"Hey! Chat with me on ChatMitra – the private, secure messaging app with HD audio & video calls: https://chatmitra.app/join\""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInviteDialog = false
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Hey! Chat with me on ChatMitra – the private, secure messaging app with HD audio & video calls: https://chatmitra.app/join"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Invite Friends"))
                        } catch (_: Exception) {}
                    }
                ) {
                    Text("Share Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

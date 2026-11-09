package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.ui.components.MessageStatusIcon
import com.example.ui.components.UnreadBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    viewModel: MainViewModel,
    onChatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.filteredChats.collectAsState()
    val chatFilter by viewModel.chatFilter.collectAsState()
    val lockedUnlocked by viewModel.lockedChatsUnlocked.collectAsState()
    val allChats by viewModel.allChats.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var selectedChatForAction by remember { mutableStateOf<Chat?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    // Multi-select mode
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedChatIds = remember { mutableStateListOf<String>() }

    val hasLockedChats = allChats.any { it.isLocked }

    Column(modifier = modifier.fillMaxSize()) {
        // Multi-select Action Bar
        if (isSelectionMode) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedChatIds.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                        Text(
                            text = "${selectedChatIds.size} selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                selectedChatIds.forEach { viewModel.chatRepo.togglePinChat(it) }
                                isSelectionMode = false
                                selectedChatIds.clear()
                            }
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin")
                        }
                        IconButton(
                            onClick = {
                                selectedChatIds.forEach { viewModel.chatRepo.toggleArchiveChat(it) }
                                isSelectionMode = false
                                selectedChatIds.clear()
                            }
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                        }
                        IconButton(
                            onClick = {
                                selectedChatIds.forEach { viewModel.chatRepo.blockContact(it) }
                                isSelectionMode = false
                                selectedChatIds.clear()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed)
                        }
                    }
                }
            }
        }

        // Folder Chips Row (Horizontally Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Unread", "Personal", "Work", "Groups", "Favorites")
            filters.forEach { filter ->
                val isSelected = chatFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setChatFilter(filter) },
                    label = {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_$filter")
                )
            }
        }

        // Locked Chats Folder if locked chats exist
        if (hasLockedChats) {
            Card(
                onClick = {
                    if (lockedUnlocked) {
                        viewModel.chatRepo.lockLockedChats()
                    } else {
                        showPinDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("locked_chats_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (lockedUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Locked Chats",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Locked Chats",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lockedUnlocked) "Unlocked • Tap to lock" else "Hidden & protected by secret PIN",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (lockedUnlocked) Icons.Default.VisibilityOff else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Chat List
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No conversations in this folder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start a new chat using the button below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(chats, key = { it.id }) { chat ->
                    val isSelected = selectedChatIds.contains(chat.id)
                    ChatItemRow(
                        chat = chat,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                if (isSelected) selectedChatIds.remove(chat.id)
                                else selectedChatIds.add(chat.id)
                                if (selectedChatIds.isEmpty()) isSelectionMode = false
                            } else {
                                onChatClick(chat.id)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedChatIds.add(chat.id)
                            } else {
                                selectedChatForAction = chat
                                showActionSheet = true
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }

    // PIN Dialog for locked chats
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                enteredPin = ""
                pinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Locked Chats")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter secret PIN to access protected chats (Default: 1234)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                enteredPin = it
                                pinError = false
                            }
                        },
                        label = { Text("4-Digit PIN") },
                        isError = pinError,
                        supportingText = if (pinError) {
                            { Text("Incorrect PIN. Please try again.") }
                        } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valid = viewModel.unlockApp(enteredPin)
                        if (valid) {
                            viewModel.chatRepo.unlockLockedChats(enteredPin)
                            showPinDialog = false
                            enteredPin = ""
                            pinError = false
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.testTag("pin_confirm_button")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Action Bottom Sheet for selected chat
    if (showActionSheet && selectedChatForAction != null) {
        val chat = selectedChatForAction!!
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text(if (chat.isPinned) "Unpin chat" else "Pin chat") },
                    leadingContent = {
                        Icon(
                            imageVector = if (chat.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.togglePinChat(chat.id)
                        showActionSheet = false
                    }
                )

                ListItem(
                    headlineContent = { Text("Move to folder (Current: ${chat.folderCategory})") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        showActionSheet = false
                        showFolderDialog = true
                    }
                )

                ListItem(
                    headlineContent = { Text(if (chat.isArchived) "Unarchive chat" else "Archive chat") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.toggleArchiveChat(chat.id)
                        showActionSheet = false
                    }
                )

                ListItem(
                    headlineContent = { Text(if (chat.isLocked) "Unlock chat" else "Lock chat with PIN") },
                    leadingContent = {
                        Icon(
                            imageVector = if (chat.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.toggleLockChat(chat.id)
                        showActionSheet = false
                    }
                )

                ListItem(
                    headlineContent = { Text("Delete conversation", color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.blockContact(chat.id)
                        showActionSheet = false
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showFolderDialog && selectedChatForAction != null) {
        val chat = selectedChatForAction!!
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Move to Folder") },
            text = {
                Column {
                    listOf("Personal", "Work", "Groups", "All").forEach { folder ->
                        ListItem(
                            headlineContent = { Text(folder) },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = WhatsAppGreen) },
                            modifier = Modifier.clickable {
                                viewModel.chatRepo.setChatFolder(chat.id, folder)
                                showFolderDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChatItemRow(
    chat: Chat,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(if (isSelected) WhatsAppGreen.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        UserAvatar(
            name = chat.name,
            avatarColor = chat.avatarColor,
            avatarUrl = chat.avatarUrl,
            size = 52.dp,
            isOnline = chat.isOnline
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (chat.isGroup) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = chat.lastMessageTime,
                    fontSize = 12.sp,
                    color = if (chat.unreadCount > 0) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.draftText.isNotBlank()) {
                        Text(
                            text = "Draft: ",
                            color = WhatsAppGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = chat.draftText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (chat.isTyping) {
                        Text(
                            text = chat.typingText,
                            color = WhatsAppGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        if (!chat.isGroup) {
                            MessageStatusIcon(
                                status = chat.lastMessageStatus,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = chat.lastMessage,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    if (chat.unreadCount > 0) {
                        UnreadBadge(count = chat.unreadCount)
                    }
                }
            }
        }
    }
}

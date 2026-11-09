package com.example.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.MediaSendPreviewDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.MediaHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allChats by viewModel.allChats.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val chat = allChats.find { it.id == chatId } ?: Chat(
        id = chatId,
        name = "Conversation",
        isGroup = false
    )
    val messageList = allMessages[chatId].orEmpty()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf(chat.draftText) }
    var replyingTo by remember { mutableStateOf<ReplySnapshot?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showTranslateDialog by remember { mutableStateOf<Message?>(null) }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showGroupInfoSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var sendSilently by remember { mutableStateOf(false) }

    // Date search jump
    var showDateSearchDialog by remember { mutableStateOf(false) }
    var searchDateText by remember { mutableStateOf("") }

    // Edit message dialog
    var showEditDialog by remember { mutableStateOf<Message?>(null) }
    var editMessageText by remember { mutableStateOf("") }

    val context = LocalContext.current
    var pendingMediaUri by remember { mutableStateOf<Uri?>(null) }
    var pendingIsVideo by remember { mutableStateOf(false) }
    var showMediaPreviewDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val copied = MediaHelper.copyUriToAppStorage(context, it, "chat_send")
            pendingMediaUri = copied ?: it
            val isVideo = context.contentResolver.getType(it)?.startsWith("video") == true
            pendingIsVideo = isVideo
            showMediaPreviewDialog = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = MediaHelper.saveBitmapToAppStorage(context, it, "camera_capture")
            pendingMediaUri = uri
            pendingIsVideo = false
            showMediaPreviewDialog = true
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to take pictures", Toast.LENGTH_SHORT).show()
        }
    }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val copied = MediaHelper.copyUriToAppStorage(context, it, "wallpaper")
            val targetUri = copied ?: it
            viewModel.chatRepo.setChatCustomWallpaper(chatId, targetUri.toString())
            Toast.makeText(context, "Custom wallpaper updated", Toast.LENGTH_SHORT).show()
        }
    }

    // Save draft on exit
    BackHandler {
        viewModel.chatRepo.saveDraft(chatId, textInput)
        onBackClick()
    }

    // Scroll to bottom on new messages
    LaunchedEffect(messageList.size) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.size - 1)
        }
    }

    // Mention suggestions dropdown (@)
    val showMentionSuggestions = chat.isGroup && textInput.contains("@") && chat.memberNames.isNotEmpty()
    val mentionQuery = if (showMentionSuggestions) textInput.substringAfterLast("@") else ""
    val matchingMembers = if (showMentionSuggestions) {
        chat.memberNames.filter { it.contains(mentionQuery, ignoreCase = true) && it != "You" }
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (chat.isGroup) showGroupInfoSheet = true
                            }
                    ) {
                        UserAvatar(
                            name = chat.name,
                            avatarColor = chat.avatarColor,
                            avatarUrl = chat.avatarUrl,
                            size = 40.dp,
                            isOnline = chat.isOnline
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chat.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (chat.isGroup) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = if (chat.isTyping) chat.typingText
                                else if (chat.isGroup) "${chat.groupMembersCount} members • Tap for group info"
                                else if (chat.isOnline) "online"
                                else "last seen recently",
                                fontSize = 12.sp,
                                color = if (chat.isTyping || chat.isOnline) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.chatRepo.saveDraft(chatId, textInput)
                            onBackClick()
                        },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.startCall(
                                name = chat.name,
                                phone = chat.contactPhone,
                                avatarColor = chat.avatarColor,
                                isVideo = true,
                                isGroup = chat.isGroup,
                                participants = chat.memberNames
                            )
                        },
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video call")
                    }
                    IconButton(
                        onClick = {
                            viewModel.startCall(
                                name = chat.name,
                                phone = chat.contactPhone,
                                avatarColor = chat.avatarColor,
                                isVideo = false,
                                isGroup = chat.isGroup,
                                participants = chat.memberNames
                            )
                        },
                        modifier = Modifier.testTag("chat_voice_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice call")
                    }
                    Box {
                        IconButton(
                            onClick = { showChatMenu = true },
                            modifier = Modifier.testTag("chat_overflow_btn")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showChatMenu,
                            onDismissRequest = { showChatMenu = false }
                        ) {
                            if (chat.isGroup) {
                                DropdownMenuItem(
                                    text = { Text("Group info & permissions") },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                    onClick = {
                                        showChatMenu = false
                                        showGroupInfoSheet = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Search by date") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    showDateSearchDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (chat.isPinned) "Unpin chat" else "Pin chat") },
                                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    viewModel.chatRepo.togglePinChat(chatId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (chat.isLocked) "Unlock chat" else "Lock chat") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    viewModel.chatRepo.toggleLockChat(chatId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Schedule message") },
                                leadingIcon = { Icon(Icons.Default.ScheduleSend, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    showScheduleDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Change wallpaper (Gallery)") },
                                leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    wallpaperPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Disappearing messages") },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    viewModel.chatRepo.sendMessage(chatId, "⏱️ Disappearing messages set to 24 hours")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export chat data") },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                onClick = {
                                    showChatMenu = false
                                    viewModel.chatRepo.sendMessage(chatId, "📁 Chat archive exported successfully.")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear chat", color = CoralRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed) },
                                onClick = {
                                    showChatMenu = false
                                    messageList.forEach { viewModel.chatRepo.deleteMessage(chatId, it.id) }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(chat.customWallpaperHex ?: settings.wallpaper.colorHex))
        ) {
            val bgWallpaperUri = chat.customWallpaperUri ?: settings.customWallpaperUri
            if (!bgWallpaperUri.isNullOrEmpty()) {
                AsyncImage(
                    model = bgWallpaperUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.28f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Messages LazyColumn
                LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // E2E Security Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔒 Messages and calls are end-to-end encrypted. No one outside of this chat, not even ChatMitra, can read or listen to them.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(messageList, key = { it.id }) { message ->
                    MessageBubbleItem(
                        message = message,
                        bubbleStyle = settings.bubbleStyle,
                        onLongClick = { selectedMessageForMenu = message },
                        onReactionClick = { emoji ->
                            viewModel.chatRepo.addReaction(chatId, message.id, emoji)
                        },
                        onPollVote = { optionId ->
                            viewModel.chatRepo.votePoll(chatId, message.id, optionId)
                        },
                        onEventRsvp = {
                            viewModel.chatRepo.rsvpEvent(chatId, message.id)
                        },
                        onTranscribeVoice = {
                            viewModel.chatRepo.transcribeVoiceMessage(chatId, message.id)
                        }
                    )
                }
            }

            // @Mention Auto-complete Box if user typed @ in group
            if (showMentionSuggestions && matchingMembers.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Mention Member:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WhatsAppGreen)
                        matchingMembers.take(4).forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val beforeAt = textInput.substringBeforeLast("@")
                                        textInput = "$beforeAt@$member "
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = member, size = 26.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(member, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Reply banner if replying
            if (replyingTo != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(replyingTo!!.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(replyingTo!!.text, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { replyingTo = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Bottom Message Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text(if (sendSilently) "Silent message..." else "Message", fontSize = 15.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field"),
                            maxLines = 4
                        )

                        // Silent send toggle button
                        IconButton(
                            onClick = { sendSilently = !sendSilently },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (sendSilently) Icons.Default.VolumeOff else Icons.Outlined.Notifications,
                                contentDescription = "Silent Send",
                                tint = if (sendSilently) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_camera_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_attach_btn")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.chatRepo.sendMessage(
                                chatId = chatId,
                                text = textInput.trim(),
                                type = MessageType.TEXT,
                                replyTo = replyingTo,
                                isSilent = sendSilently
                            )
                            textInput = ""
                            replyingTo = null
                        } else {
                            // Record voice note simulation
                            coroutineScope.launch {
                                viewModel.chatRepo.sendMessage(
                                    chatId = chatId,
                                    text = "Voice message",
                                    type = MessageType.VOICE_NOTE,
                                    voiceDurationSeconds = 14
                                )
                            }
                        }
                    },
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_btn")
                ) {
                    Icon(
                        imageVector = if (textInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = "Send",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Share Content", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.Poll,
                        label = "Poll",
                        bgColor = Color(0xFF00796B),
                        onClick = {
                            showAttachmentSheet = false
                            showPollDialog = true
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.Event,
                        label = "Event",
                        bgColor = Color(0xFFD84315),
                        onClick = {
                            showAttachmentSheet = false
                            viewModel.chatRepo.sendMessage(
                                chatId = chatId,
                                text = "Weekly Architecture Sync",
                                type = MessageType.EVENT,
                                eventData = GroupEvent(
                                    id = "ev_${System.currentTimeMillis()}",
                                    title = "Weekly Architecture & QA Sync",
                                    description = "Reviewing v2.0 feature deployment and performance metrics.",
                                    dateTimeStr = "Tomorrow at 4:00 PM",
                                    attendeeNames = listOf("You", "Aarav Sharma")
                                )
                            )
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.ScheduleSend,
                        label = "Schedule",
                        bgColor = Color(0xFF7B1FA2),
                        onClick = {
                            showAttachmentSheet = false
                            showScheduleDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.Description,
                        label = "Document",
                        bgColor = Color(0xFF5F259F),
                        onClick = {
                            viewModel.chatRepo.sendMessage(chatId, "Project_Spec_2026.pdf", MessageType.DOCUMENT, mediaName = "Project_Spec_2026.pdf", mediaSizeText = "1.8 MB")
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        bgColor = Color(0xFFD32F2F),
                        onClick = {
                            showAttachmentSheet = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.Image,
                        label = "Gallery",
                        bgColor = Color(0xFF9C27B0),
                        onClick = {
                            showAttachmentSheet = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.Headphones,
                        label = "Audio",
                        bgColor = Color(0xFFE65100),
                        onClick = {
                            viewModel.chatRepo.sendMessage(chatId, "Audio Clip 🎵", MessageType.AUDIO, mediaName = "Voice_Note.mp3", mediaSizeText = "2.8 MB", isSilent = sendSilently)
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        bgColor = Color(0xFF0F9D58),
                        onClick = {
                            viewModel.chatRepo.sendMessage(chatId, "📍 Bengaluru Tech Park, Karnataka", MessageType.TEXT, isSilent = sendSilently)
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Default.Person,
                        label = "Contact",
                        bgColor = Color(0xFF1976D2),
                        onClick = {
                            viewModel.chatRepo.sendMessage(chatId, "👤 Contact: Vikram Patel (+91 98765 43212)", MessageType.TEXT, isSilent = sendSilently)
                            showAttachmentSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Media Send Preview Dialog (HD toggle, View Once 1x toggle, Caption)
    if (showMediaPreviewDialog && pendingMediaUri != null) {
        MediaSendPreviewDialog(
            mediaUri = pendingMediaUri!!,
            isVideo = pendingIsVideo,
            onDismiss = {
                showMediaPreviewDialog = false
                pendingMediaUri = null
            },
            onSend = { caption, isHd, isViewOnce ->
                val mediaUriStr = pendingMediaUri.toString()
                val isVideo = pendingIsVideo
                viewModel.chatRepo.sendMessage(
                    chatId = chatId,
                    text = caption,
                    type = if (isVideo) MessageType.VIDEO else MessageType.IMAGE,
                    mediaUrl = mediaUriStr,
                    mediaName = if (isVideo) "VID_${System.currentTimeMillis()}.mp4" else "IMG_${System.currentTimeMillis()}.jpg",
                    mediaSizeText = if (isHd) "HD 4.2 MB" else "Standard 1.4 MB",
                    isViewOnce = isViewOnce,
                    isSilent = sendSilently
                )
                showMediaPreviewDialog = false
                pendingMediaUri = null
            }
        )
    }

    // Message Action Sheet (Reactions, Reply privately, Star, Translate, Edit, Delete)
    if (selectedMessageForMenu != null) {
        val msg = selectedMessageForMenu!!
        ModalBottomSheet(onDismissRequest = { selectedMessageForMenu = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val emojis = listOf("❤️", "😂", "👍", "😮", "😢", "🙏", "🔥")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.chatRepo.addReaction(chatId, msg.id, emoji)
                                    selectedMessageForMenu = null
                                }
                                .padding(6.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                ListItem(
                    headlineContent = { Text("Reply") },
                    leadingContent = { Icon(Icons.Default.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        replyingTo = ReplySnapshot(msg.id, if (msg.isOutgoing) "You" else chat.name, msg.text, msg.type)
                        selectedMessageForMenu = null
                    }
                )

                if (chat.isGroup && !msg.isOutgoing) {
                    ListItem(
                        headlineContent = { Text("Reply privately") },
                        leadingContent = { Icon(Icons.Default.Message, contentDescription = null) },
                        modifier = Modifier.clickable {
                            selectedMessageForMenu = null
                            // Open direct message with sender
                            val directChat = allChats.find { it.id == msg.senderId }
                            if (directChat != null) {
                                viewModel.navigateTo(AppScreen.ChatDetail(directChat.id))
                            }
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text("Copy text") },
                    leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        selectedMessageForMenu = null
                    }
                )

                if (msg.isOutgoing) {
                    ListItem(
                        headlineContent = { Text("Edit message") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable {
                            val toEdit = msg
                            editMessageText = msg.text
                            selectedMessageForMenu = null
                            showEditDialog = toEdit
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text(if (msg.isStarred) "Unstar message" else "Star message") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.toggleStarMessage(chatId, msg.id)
                        selectedMessageForMenu = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Translate message") },
                    leadingContent = { Icon(Icons.Default.Translate, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val toTranslate = msg
                        selectedMessageForMenu = null
                        showTranslateDialog = toTranslate
                    }
                )

                ListItem(
                    headlineContent = { Text("Delete message", color = CoralRed) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed) },
                    modifier = Modifier.clickable {
                        viewModel.chatRepo.deleteMessage(chatId, msg.id)
                        selectedMessageForMenu = null
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit message dialog
    if (showEditDialog != null) {
        val target = showEditDialog!!
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editMessageText,
                    onValueChange = { editMessageText = it },
                    label = { Text("Updated text") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editMessageText.isNotBlank()) {
                        viewModel.chatRepo.editMessage(chatId, target.id, editMessageText.trim())
                        showEditDialog = null
                    }
                }) {
                    Text("Save Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Create Poll Dialog
    if (showPollDialog) {
        CreatePollDialog(
            onDismiss = { showPollDialog = false },
            onCreatePoll = { question, options, allowMultiple ->
                val poll = PollData(
                    id = "p_${System.currentTimeMillis()}",
                    question = question,
                    options = options.mapIndexed { idx, opt -> PollOption("opt_$idx", opt, emptyList()) },
                    allowMultipleAnswers = allowMultiple
                )
                viewModel.chatRepo.sendMessage(chatId, "📊 Poll: $question", MessageType.POLL, pollData = poll)
                showPollDialog = false
            }
        )
    }

    // Schedule Dialog
    if (showScheduleDialog) {
        ScheduleMessageDialog(
            onDismiss = { showScheduleDialog = false },
            onSchedule = { scheduledTime ->
                val draft = if (textInput.isNotBlank()) textInput else "Scheduled reminder"
                viewModel.chatRepo.sendMessage(
                    chatId = chatId,
                    text = draft,
                    isScheduled = true,
                    scheduledTimeFormatted = scheduledTime
                )
                textInput = ""
                showScheduleDialog = false
            }
        )
    }

    // Group Info Sheet
    if (showGroupInfoSheet && chat.isGroup) {
        GroupInfoSheet(
            chat = chat,
            onDismiss = { showGroupInfoSheet = false },
            onPromoteAdmin = { member -> viewModel.chatRepo.promoteAdmin(chatId, member) },
            onDemoteAdmin = { member -> viewModel.chatRepo.demoteAdmin(chatId, member) },
            onMuteMember = { member -> viewModel.chatRepo.muteMember(chatId, member) },
            onRemoveMember = { member -> viewModel.chatRepo.removeMember(chatId, member) },
            onApproveRequest = { applicant -> viewModel.chatRepo.approveJoinRequest(chatId, applicant) },
            onUpdatePermissions = { onlyAdminsSend, onlyAdminsEdit ->
                viewModel.chatRepo.updateGroupPermissions(chatId, onlyAdminsSend, onlyAdminsEdit)
            }
        )
    }

    // Date search jump dialog
    if (showDateSearchDialog) {
        AlertDialog(
            onDismissRequest = { showDateSearchDialog = false },
            title = { Text("Jump to Message by Date") },
            text = {
                Column {
                    Text("Select a date to jump to earlier messages in this conversation:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf("Today", "Yesterday", "Nov 5, 2026", "Nov 2, 2026").forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchDateText = d
                                    showDateSearchDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(d, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDateSearchDialog = false }) { Text("Close") }
            }
        )
    }

    // Translation Selector Dialog
    if (showTranslateDialog != null) {
        val targetMsg = showTranslateDialog!!
        AlertDialog(
            onDismissRequest = { showTranslateDialog = null },
            title = { Text("Translate Message") },
            text = {
                Column {
                    listOf("Hindi", "Arabic", "Spanish", "French", "Bengali").forEach { lang ->
                        ListItem(
                            headlineContent = { Text(lang, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.clickable {
                                viewModel.chatRepo.translateMessage(chatId, targetMsg.id, lang)
                                showTranslateDialog = null
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTranslateDialog = null }) { Text("Cancel") }
            }
        )
    }
}

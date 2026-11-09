package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            name = "You",
            phoneNumber = "+91 98765 43210",
            statusText = "Hey there! I am using ChatMitra 🚀",
            avatarColor = 0xFF008069,
            isOnline = true
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _lockedChatsUnlocked = MutableStateFlow(false)
    val lockedChatsUnlocked: StateFlow<Boolean> = _lockedChatsUnlocked.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialChats = listOf(
            Chat(
                id = "chat_1",
                name = "Aarav Sharma",
                isGroup = false,
                avatarColor = 0xFF008069,
                lastMessage = "Looking forward to our meetup tomorrow! 👍",
                lastMessageTime = "11:42 AM",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 0,
                isPinned = true,
                isOnline = true,
                contactPhone = "+91 98111 22334",
                folderCategory = "Personal"
            ),
            Chat(
                id = "chat_2",
                name = "Tech Innovators Group 💻",
                isGroup = true,
                groupMembersCount = 8,
                memberNames = listOf("You", "Aarav Sharma", "Priya Patel", "Rohan Verma", "Sneha Kulkarni", "Vikram Singh"),
                adminNames = listOf("You", "Priya Patel"),
                coAdminNames = listOf("Aarav Sharma"),
                pendingJoinRequests = listOf("Ananya Gupta", "Rahul Joshi"),
                avatarColor = 0xFF128C7E,
                lastMessage = "📊 Poll: Which feature should we build next?",
                lastMessageTime = "10:15 AM",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 3,
                isPinned = true,
                folderCategory = "Work",
                groupDescription = "Welcome to Tech Innovators. Discussions on Android, Jetpack Compose & Distributed Systems."
            ),
            Chat(
                id = "chat_3",
                name = "Priya Patel",
                isGroup = false,
                avatarColor = 0xFFE91E63,
                lastMessage = "Can you send the PDF report?",
                lastMessageTime = "Yesterday",
                lastMessageStatus = MessageStatus.DELIVERED,
                unreadCount = 1,
                isOnline = false,
                contactPhone = "+91 98222 33445",
                folderCategory = "Work"
            ),
            Chat(
                id = "chat_4",
                name = "Rohan Verma",
                isGroup = false,
                avatarColor = 0xFF3F51B5,
                lastMessage = "🎤 Voice message (0:18)",
                lastMessageTime = "Yesterday",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 0,
                isOnline = true,
                contactPhone = "+91 98333 44556",
                folderCategory = "Personal"
            ),
            Chat(
                id = "chat_5",
                name = "Family Hub ❤️",
                isGroup = true,
                groupMembersCount = 5,
                memberNames = listOf("You", "Mom", "Dad", "Sister", "Uncle"),
                adminNames = listOf("Mom", "You"),
                avatarColor = 0xFFFF5722,
                lastMessage = "Mom: Dinner is ready at 8 PM!",
                lastMessageTime = "Nov 5",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 0,
                folderCategory = "Personal"
            ),
            Chat(
                id = "chat_6",
                name = "Sneha Kulkarni",
                isGroup = false,
                avatarColor = 0xFF9C27B0,
                lastMessage = "Thanks a lot for the quick help 😊",
                lastMessageTime = "Nov 4",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 0,
                contactPhone = "+91 98444 55667",
                folderCategory = "Work"
            ),
            Chat(
                id = "chat_7",
                name = "Confidential Project 🔒",
                isGroup = false,
                avatarColor = 0xFF37474F,
                lastMessage = "Passphrase confirmed for vault",
                lastMessageTime = "Nov 3",
                lastMessageStatus = MessageStatus.READ,
                unreadCount = 0,
                isLocked = true,
                contactPhone = "+91 98999 00000",
                folderCategory = "Work"
            )
        )

        _chats.value = initialChats

        val initialMessages = mutableMapOf<String, List<Message>>()

        initialMessages["chat_1"] = listOf(
            Message(
                id = "m1_1",
                chatId = "chat_1",
                senderId = "chat_1",
                isOutgoing = false,
                text = "Hey! Are you free for a quick call regarding the app project?",
                formattedTime = "11:30 AM",
                status = MessageStatus.READ,
                reactions = mapOf("👍" to listOf("You"))
            ),
            Message(
                id = "m1_2",
                chatId = "chat_1",
                senderId = "user_me",
                isOutgoing = true,
                text = "Hi Aarav! Yes, absolutely. Everything on the Android UI looks super crisp with 3D elements.",
                formattedTime = "11:35 AM",
                status = MessageStatus.READ,
                reactions = mapOf("❤️" to listOf("Aarav Sharma"))
            ),
            Message(
                id = "m1_3",
                chatId = "chat_1",
                senderId = "chat_1",
                isOutgoing = false,
                text = "Looking forward to our meetup tomorrow! 👍",
                formattedTime = "11:42 AM",
                status = MessageStatus.READ
            )
        )

        initialMessages["chat_2"] = listOf(
            Message(
                id = "m2_1",
                chatId = "chat_2",
                senderId = "chat_3",
                isOutgoing = false,
                text = "Welcome to Tech Innovators! Let's build something awesome.",
                formattedTime = "9:00 AM",
                status = MessageStatus.READ
            ),
            Message(
                id = "m2_2",
                chatId = "chat_2",
                senderId = "user_me",
                isOutgoing = true,
                text = "Excited to collaborate with you all on ChatMitra! @Priya @Aarav",
                mentions = listOf("Priya", "Aarav"),
                formattedTime = "9:45 AM",
                status = MessageStatus.READ,
                reactions = mapOf("🔥" to listOf("Priya Patel", "Aarav Sharma"))
            ),
            Message(
                id = "m2_poll",
                chatId = "chat_2",
                senderId = "chat_3",
                isOutgoing = false,
                text = "Poll: Which feature should we build next?",
                type = MessageType.POLL,
                pollData = PollData(
                    id = "p_1",
                    question = "Which feature should we prioritize for v2.0?",
                    options = listOf(
                        PollOption("opt_1", "End-to-End Encrypted Group Video Calls", listOf("You", "Aarav Sharma", "Sneha Kulkarni")),
                        PollOption("opt_2", "Custom Channel Broadcasts & Communities", listOf("Priya Patel", "Rohan Verma")),
                        PollOption("opt_3", "Voice-to-Text Transcription", listOf("You", "Vikram Singh"))
                    ),
                    allowMultipleAnswers = true
                ),
                formattedTime = "10:15 AM",
                status = MessageStatus.READ
            )
        )

        initialMessages["chat_3"] = listOf(
            Message(
                id = "m3_1",
                chatId = "chat_3",
                senderId = "user_me",
                isOutgoing = true,
                text = "Hi Priya, here is the quarterly roadmap document.",
                formattedTime = "Yesterday",
                status = MessageStatus.READ
            ),
            Message(
                id = "m3_2",
                chatId = "chat_3",
                senderId = "chat_3",
                isOutgoing = false,
                text = "Can you send the PDF report?",
                formattedTime = "Yesterday",
                status = MessageStatus.DELIVERED
            )
        )

        initialMessages["chat_4"] = listOf(
            Message(
                id = "m4_1",
                chatId = "chat_4",
                senderId = "chat_4",
                isOutgoing = false,
                text = "Listen to this audio idea when you have a minute!",
                type = MessageType.VOICE_NOTE,
                voiceDurationSeconds = 18,
                voiceTranscription = "Hey, let's catch up on the architectural design for the multi-device sync module tomorrow morning.",
                formattedTime = "Yesterday",
                status = MessageStatus.READ
            )
        )

        _messages.value = initialMessages

        // Channels
        _channels.value = listOf(
            Channel(
                id = "ch_official",
                name = "ChatMitra Official 🚀",
                description = "Official announcements, new release updates, and tips for ChatMitra users.",
                isPublic = true,
                subscribersCount = 14200,
                isSubscribed = true,
                avatarColor = 0xFF008069,
                posts = listOf(
                    ChannelPost(
                        id = "post_1",
                        text = "🎉 ChatMitra v2.0 is officially released! Enjoy Multi-Device Sync, Group Polls, Channels, and Voice Transcription.",
                        time = "2 hours ago",
                        viewsCount = 3840,
                        sharesCount = 192
                    ),
                    ChannelPost(
                        id = "post_2",
                        text = "🔒 Security Spotlight: Did you know ChatMitra now supports 64-digit end-to-end encrypted backup keys? Check Settings > Privacy & Security to activate yours.",
                        time = "Yesterday",
                        viewsCount = 5120,
                        sharesCount = 240
                    )
                )
            ),
            Channel(
                id = "ch_tech",
                name = "Android & Tech Radar 📱",
                description = "Daily breakthroughs in mobile technology, Android 16, and modern app architecture.",
                isPublic = true,
                subscribersCount = 8900,
                isSubscribed = false,
                avatarColor = 0xFF1565C0,
                posts = listOf(
                    ChannelPost(
                        id = "post_tech_1",
                        text = "Jetpack Compose 1.8 brings enhanced predictive back gestures and ultra-smooth graphics rendering.",
                        time = "5 hours ago",
                        viewsCount = 1250,
                        sharesCount = 80
                    )
                )
            ),
            Channel(
                id = "ch_daily",
                name = "Daily Spark & Wisdom 💡",
                description = "Morning inspiration, focus tips, and productivity habits.",
                isPublic = true,
                subscribersCount = 6100,
                isSubscribed = false,
                avatarColor = 0xFF7B1FA2,
                posts = listOf(
                    ChannelPost(
                        id = "post_spark_1",
                        text = "“Consistency is the mother of mastery.” Wishing everyone a productive weekend ahead!",
                        time = "Today, 8:00 AM",
                        viewsCount = 980,
                        sharesCount = 65
                    )
                )
            )
        )

        // Communities
        _communities.value = listOf(
            Community(
                id = "comm_devs",
                name = "Global Android Builders 🌍",
                description = "A thriving community of developers collaborating on next-generation Android apps.",
                avatarColor = 0xFF00796B,
                membersCount = 1840,
                groupNames = listOf("General Lounge", "Architecture & Compose", "Showcase & Feedback", "Releases & QA")
            ),
            Community(
                id = "comm_startup",
                name = "Innovation & Founders Club 💼",
                description = "Connect with entrepreneurs, product thinkers, and tech founders.",
                avatarColor = 0xFF0288D1,
                membersCount = 920,
                groupNames = listOf("Pitch & Ideas", "Funding & Strategy", "Hiring & Mentorship")
            )
        )
    }

    fun updateCurrentUser(name: String, statusText: String, phone: String, bio: String = "") {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            statusText = statusText,
            phoneNumber = phone,
            aboutBio = if (bio.isNotBlank()) bio else _currentUser.value.aboutBio
        )
    }

    fun setCustomStatus(emoji: String, label: String) {
        _currentUser.value = _currentUser.value.copy(
            customStatusEmoji = emoji,
            customStatusLabel = label
        )
    }

    fun updateUserProfilePicture(avatarUriString: String) {
        val currentPhotos = _currentUser.value.avatarPhotos.toMutableList()
        if (avatarUriString.isNotBlank() && !currentPhotos.contains(avatarUriString)) {
            currentPhotos.add(0, avatarUriString)
        }
        _currentUser.value = _currentUser.value.copy(
            avatarUrl = avatarUriString,
            avatarPhotos = currentPhotos
        )
    }

    fun removeUserProfilePicture() {
        _currentUser.value = _currentUser.value.copy(
            avatarUrl = ""
        )
    }

    fun updateChatAvatar(chatId: String, avatarUriString: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(avatarUrl = avatarUriString) else chat
        }
    }

    fun setChatCustomWallpaper(chatId: String, wallpaperUriString: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(customWallpaperUri = wallpaperUriString) else chat
        }
    }

    fun requestVerification() {
        _currentUser.value = _currentUser.value.copy(
            verificationRequested = true,
            isVerified = true // Instant simulation of approved verified badge
        )
    }

    fun unlockLockedChats(pin: String): Boolean {
        _lockedChatsUnlocked.value = true
        return true
    }

    fun lockLockedChats() {
        _lockedChatsUnlocked.value = false
    }

    fun saveDraft(chatId: String, draft: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(draftText = draft) else it
        }
    }

    fun clearDraft(chatId: String) {
        saveDraft(chatId, "")
    }

    fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaName: String = "",
        mediaSizeText: String = "",
        voiceDurationSeconds: Int = 0,
        replyTo: ReplySnapshot? = null,
        pollData: PollData? = null,
        eventData: GroupEvent? = null,
        isScheduled: Boolean = false,
        scheduledTimeFormatted: String = "",
        isSilent: Boolean = false,
        mentions: List<String> = emptyList(),
        isViewOnce: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(now))
        val newMsgId = "msg_${UUID.randomUUID()}"

        val newMsg = Message(
            id = newMsgId,
            chatId = chatId,
            senderId = "user_me",
            isOutgoing = true,
            text = text,
            type = type,
            status = if (isScheduled) MessageStatus.SENDING else MessageStatus.SENDING,
            timestamp = now,
            formattedTime = timeStr,
            replyTo = replyTo,
            mediaUrl = mediaUrl,
            mediaName = mediaName,
            mediaSizeText = mediaSizeText,
            voiceDurationSeconds = voiceDurationSeconds,
            pollData = pollData,
            eventData = eventData,
            isScheduled = isScheduled,
            scheduledTimeFormatted = scheduledTimeFormatted,
            isSilent = isSilent,
            mentions = mentions,
            isViewOnce = isViewOnce
        )

        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        // Clear draft when sent
        clearDraft(chatId)

        // Update chat's last message
        val displayLast = when (type) {
            MessageType.VOICE_NOTE -> "🎤 Voice message ($voiceDurationSeconds s)"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.DOCUMENT -> "📄 $mediaName"
            MessageType.AUDIO -> "🎵 Audio"
            MessageType.STICKER -> "🎨 Sticker"
            MessageType.POLL -> "📊 Poll: ${pollData?.question ?: text}"
            MessageType.EVENT -> "📅 Event: ${eventData?.title ?: text}"
            else -> text
        }
        updateChatLastMessage(chatId, displayLast, timeStr, MessageStatus.SENT)

        if (!isScheduled) {
            scope.launch {
                delay(400)
                updateMessageStatus(chatId, newMsgId, MessageStatus.SENT)
                delay(600)
                updateMessageStatus(chatId, newMsgId, MessageStatus.DELIVERED)
                delay(800)
                updateMessageStatus(chatId, newMsgId, MessageStatus.READ)

                // Simulate responsive contact reply
                simulateContactReply(chatId, text)
            }
        }
    }

    fun addReaction(chatId: String, messageId: String, emoji: String, userName: String = "You") {
        val currentList = _messages.value[chatId].orEmpty()
        val updated = currentList.map { msg ->
            if (msg.id == messageId) {
                val currentReactions = msg.reactions.toMutableMap()
                val currentUsers = currentReactions[emoji].orEmpty().toMutableList()
                if (currentUsers.contains(userName)) {
                    currentUsers.remove(userName)
                    if (currentUsers.isEmpty()) currentReactions.remove(emoji)
                    else currentReactions[emoji] = currentUsers
                } else {
                    currentUsers.add(userName)
                    currentReactions[emoji] = currentUsers
                }
                msg.copy(reactions = currentReactions)
            } else msg
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    fun votePoll(chatId: String, messageId: String, optionId: String, voterName: String = "You") {
        val currentList = _messages.value[chatId].orEmpty()
        val updated = currentList.map { msg ->
            if (msg.id == messageId && msg.pollData != null) {
                val currentPoll = msg.pollData
                val updatedOptions = currentPoll.options.map { opt ->
                    if (opt.id == optionId) {
                        val currentVoters = opt.voterNames.toMutableList()
                        if (currentVoters.contains(voterName)) {
                            currentVoters.remove(voterName)
                        } else {
                            currentVoters.add(voterName)
                        }
                        opt.copy(voterNames = currentVoters)
                    } else if (!currentPoll.allowMultipleAnswers) {
                        // remove previous vote if single choice
                        opt.copy(voterNames = opt.voterNames.filter { it != voterName })
                    } else opt
                }
                msg.copy(pollData = currentPoll.copy(options = updatedOptions))
            } else msg
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    fun rsvpEvent(chatId: String, messageId: String, attendeeName: String = "You") {
        val currentList = _messages.value[chatId].orEmpty()
        val updated = currentList.map { msg ->
            if (msg.id == messageId && msg.eventData != null) {
                val currentEvent = msg.eventData
                val currentAttendees = currentEvent.attendeeNames.toMutableList()
                if (currentAttendees.contains(attendeeName)) currentAttendees.remove(attendeeName)
                else currentAttendees.add(attendeeName)
                msg.copy(eventData = currentEvent.copy(attendeeNames = currentAttendees))
            } else msg
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    fun transcribeVoiceMessage(chatId: String, messageId: String) {
        val currentList = _messages.value[chatId].orEmpty()
        val updated = currentList.map { msg ->
            if (msg.id == messageId) {
                val sampleTranscription = "“Hey! Just letting you know our team discussion went great. Let's sync up on the latest milestone soon.”"
                msg.copy(voiceTranscription = sampleTranscription)
            } else msg
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    fun bulkDeleteMessages(chatId: String, messageIds: Set<String>) {
        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to currentList.filterNot { messageIds.contains(it.id) })
    }

    fun bulkStarMessages(chatId: String, messageIds: Set<String>) {
        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to currentList.map {
            if (messageIds.contains(it.id)) it.copy(isStarred = true) else it
        })
    }

    fun bulkForwardMessages(targetChatId: String, messageIds: Set<String>, sourceChatId: String) {
        val sourceList = _messages.value[sourceChatId].orEmpty()
        val messagesToForward = sourceList.filter { messageIds.contains(it.id) }
        messagesToForward.forEach { msg ->
            sendMessage(
                chatId = targetChatId,
                text = "Forwarded: ${msg.text}",
                type = msg.type,
                mediaName = msg.mediaName,
                mediaSizeText = msg.mediaSizeText
            )
        }
    }

    fun promoteAdmin(chatId: String, memberName: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId && !chat.adminNames.contains(memberName)) {
                chat.copy(adminNames = chat.adminNames + memberName)
            } else chat
        }
    }

    fun demoteAdmin(chatId: String, memberName: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(adminNames = chat.adminNames.filter { it != memberName })
            } else chat
        }
    }

    fun muteMember(chatId: String, memberName: String) {
        // Log mute action in chat
        sendMessage(chatId, "🔇 $memberName has been muted by Admin.", MessageType.TEXT)
    }

    fun removeMember(chatId: String, memberName: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    memberNames = chat.memberNames.filter { it != memberName },
                    groupMembersCount = (chat.groupMembersCount - 1).coerceAtLeast(1)
                )
            } else chat
        }
        sendMessage(chatId, "⚠️ $memberName was removed from the group.", MessageType.TEXT)
    }

    fun approveJoinRequest(chatId: String, applicantName: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    pendingJoinRequests = chat.pendingJoinRequests.filter { it != applicantName },
                    memberNames = chat.memberNames + applicantName,
                    groupMembersCount = chat.groupMembersCount + 1
                )
            } else chat
        }
        sendMessage(chatId, "👋 $applicantName joined via invite link.", MessageType.TEXT)
    }

    fun updateGroupPermissions(chatId: String, onlyAdminsSend: Boolean, onlyAdminsEdit: Boolean) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    onlyAdminsCanSend = onlyAdminsSend,
                    onlyAdminsCanEditInfo = onlyAdminsEdit
                )
            } else chat
        }
    }

    fun toggleSubscribeChannel(channelId: String) {
        _channels.value = _channels.value.map { ch ->
            if (ch.id == channelId) {
                val nextSub = !ch.isSubscribed
                ch.copy(
                    isSubscribed = nextSub,
                    subscribersCount = if (nextSub) ch.subscribersCount + 1 else (ch.subscribersCount - 1).coerceAtLeast(0)
                )
            } else ch
        }
    }

    fun postChannelBroadcast(channelId: String, text: String) {
        val newPost = ChannelPost(
            id = "cp_${System.currentTimeMillis()}",
            text = text,
            time = "Just now",
            viewsCount = 1,
            sharesCount = 0
        )
        _channels.value = _channels.value.map { ch ->
            if (ch.id == channelId) {
                ch.copy(posts = listOf(newPost) + ch.posts)
            } else ch
        }
    }

    fun createChannel(name: String, description: String, isPublic: Boolean) {
        val newChannel = Channel(
            id = "ch_${UUID.randomUUID().toString().take(6)}",
            name = name,
            description = description,
            isPublic = isPublic,
            subscribersCount = 1,
            isSubscribed = true,
            avatarColor = 0xFF00897B,
            adminName = "You",
            posts = listOf(ChannelPost(id = "p_first", text = "Welcome to $name!", time = "Just now"))
        )
        _channels.value = listOf(newChannel) + _channels.value
    }

    fun createCommunity(name: String, description: String, groups: List<String>) {
        val newComm = Community(
            id = "comm_${UUID.randomUUID().toString().take(6)}",
            name = name,
            description = description,
            avatarColor = 0xFF128C7E,
            membersCount = 1,
            groupNames = if (groups.isEmpty()) listOf("General Discussion", "Announcements") else groups
        )
        _communities.value = listOf(newComm) + _communities.value
    }

    private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        val currentList = _messages.value[chatId].orEmpty()
        val updated = currentList.map {
            if (it.id == messageId) it.copy(status = status) else it
        }
        _messages.value = _messages.value + (chatId to updated)
        updateChatLastMessageStatus(chatId, status)
    }

    private fun updateChatLastMessage(chatId: String, lastMsg: String, timeStr: String, status: MessageStatus) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) {
                it.copy(
                    lastMessage = lastMsg,
                    lastMessageTime = timeStr,
                    lastMessageTimestamp = System.currentTimeMillis(),
                    lastMessageStatus = status
                )
            } else it
        }
    }

    private fun updateChatLastMessageStatus(chatId: String, status: MessageStatus) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(lastMessageStatus = status) else it
        }
    }

    private suspend fun simulateContactReply(chatId: String, userText: String) {
        val chat = _chats.value.find { it.id == chatId } ?: return
        if (chat.isGroup) {
            setTyping(chatId, true, "Priya is typing...")
            delay(1800)
            setTyping(chatId, false)

            val replyText = getSmartReply(userText, isGroup = true)
            receiveMessage(chatId, senderId = "chat_3", text = replyText)
        } else {
            setTyping(chatId, true, "${chat.name} is typing...")
            delay(1600)
            setTyping(chatId, false)

            val replyText = getSmartReply(userText, isGroup = false)
            receiveMessage(chatId, senderId = chatId, text = replyText)
        }
    }

    private fun setTyping(chatId: String, isTyping: Boolean, text: String = "typing...") {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isTyping = isTyping, typingText = text) else it
        }
    }

    fun receiveMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT
    ) {
        val now = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(now))
        val newMsgId = "msg_${UUID.randomUUID()}"

        val newMsg = Message(
            id = newMsgId,
            chatId = chatId,
            senderId = senderId,
            isOutgoing = false,
            text = text,
            type = type,
            status = MessageStatus.READ,
            timestamp = now,
            formattedTime = timeStr
        )

        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to (currentList + newMsg))

        updateChatLastMessage(chatId, text, timeStr, MessageStatus.READ)
    }

    private fun getSmartReply(input: String, isGroup: Boolean): String {
        val lower = input.lowercase().trim()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! Great to hear from you. How are you doing today?"
            lower.contains("how are you") ->
                "I am doing great, thank you! Hope everything is wonderful on your end as well."
            lower.contains("meeting") || lower.contains("meet") ->
                "Sure thing! Let's connect at 4 PM. I'll send over the details."
            lower.contains("photo") || lower.contains("pic") || lower.contains("image") ->
                "Awesome pictures! The quality came out really crisp."
            lower.contains("call") || lower.contains("video") ->
                "I'm ready whenever you'd like to jump on a voice or video call!"
            lower.contains("poll") ->
                "Just cast my vote in the poll! Great initiative."
            lower.contains("thanks") || lower.contains("thank you") ->
                "You're very welcome! Always happy to help."
            lower.contains("bye") || lower.contains("good night") ->
                "Take care! Talk to you soon 👋"
            isGroup -> "Got it! Team, let's keep the momentum going."
            else -> "Got your message! Let's catch up more in a bit."
        }
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to currentList.map {
            if (it.id == messageId) it.copy(isStarred = !it.isStarred) else it
        })
    }

    fun deleteMessage(chatId: String, messageId: String) {
        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to currentList.filterNot { it.id == messageId })
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        val nowStr = timeFormat.format(Date())
        val currentList = _messages.value[chatId].orEmpty()
        _messages.value = _messages.value + (chatId to currentList.map {
            if (it.id == messageId) it.copy(text = newText, isEdited = true, editedTime = nowStr) else it
        })
    }

    fun translateMessage(chatId: String, messageId: String, targetLang: String) {
        val currentList = _messages.value[chatId].orEmpty()
        val msg = currentList.find { it.id == messageId } ?: return

        val translated = translateTextEngine(msg.text, targetLang)
        _messages.value = _messages.value + (chatId to currentList.map {
            if (it.id == messageId) it.copy(translatedText = translated, targetLang = targetLang) else it
        })
    }

    private fun translateTextEngine(text: String, targetLang: String): String {
        return when (targetLang.lowercase()) {
            "hindi" -> "अनुवाद: $text (आपसे बात करके अच्छा लगा!)"
            "arabic" -> "ترجمة: $text (سعدت بالتحدث معك!)"
            "spanish" -> "Traducción: $text (¡Encantado de hablar contigo!)"
            "french" -> "Traduction: $text (Ravi de vous parler !)"
            "bengali" -> "অনুবাদ: $text (আপনার সাথে কথা বলে ভালো লাগলো!)"
            else -> "Translation: $text"
        }
    }

    fun togglePinChat(chatId: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun toggleArchiveChat(chatId: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isArchived = !it.isArchived) else it
        }
    }

    fun toggleLockChat(chatId: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isLocked = !it.isLocked) else it
        }
    }

    fun setChatFolder(chatId: String, folder: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(folderCategory = folder) else it
        }
    }

    fun createGroupChat(name: String, membersCount: Int) {
        val newChat = Chat(
            id = "chat_group_${UUID.randomUUID().toString().take(6)}",
            name = name,
            isGroup = true,
            groupMembersCount = membersCount + 1,
            avatarColor = 0xFF00796B,
            lastMessage = "You created group \"$name\"",
            lastMessageTime = "Just now",
            lastMessageStatus = MessageStatus.READ,
            unreadCount = 0,
            adminNames = listOf("You"),
            memberNames = listOf("You", "Aarav Sharma", "Priya Patel")
        )
        _chats.value = listOf(newChat) + _chats.value
        _messages.value = _messages.value + (newChat.id to listOf(
            Message(
                id = "m_init_${newChat.id}",
                chatId = newChat.id,
                senderId = "user_me",
                isOutgoing = true,
                text = "Welcome to $name!",
                formattedTime = "Now"
            )
        ))
    }

    fun addContactChat(name: String, phone: String) {
        val newChat = Chat(
            id = "chat_contact_${UUID.randomUUID().toString().take(6)}",
            name = name,
            isGroup = false,
            avatarColor = 0xFF00897B,
            lastMessage = "Hey! Let's chat on ChatMitra",
            lastMessageTime = "Just now",
            contactPhone = phone
        )
        _chats.value = listOf(newChat) + _chats.value
    }

    fun blockContact(chatId: String) {
        _chats.value = _chats.value.filterNot { it.id == chatId }
    }
}

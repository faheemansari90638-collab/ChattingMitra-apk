package com.example.data.model

data class Chat(
    val id: String,
    val name: String,
    val isGroup: Boolean = false,
    val groupMembersCount: Int = 0,
    val participantIds: List<String> = emptyList(),
    val avatarUrl: String = "",
    val avatarColor: Long = 0xFF008069,
    val lastMessage: String = "",
    val lastMessageTime: String = "Now",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageStatus: MessageStatus = MessageStatus.READ,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val typingText: String = "typing...",
    val contactPhone: String = "",
    val disappearingHours: Int = 0, // 0 = off, 24 = 24h, 168 = 7 days
    val folderCategory: String = "All", // "All", "Personal", "Work", "Groups"
    val draftText: String = "",
    // Group specific features
    val adminNames: List<String> = listOf("You"),
    val coAdminNames: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val inviteLink: String = "https://chatmitra.app/join/grp-${id.takeLast(6)}",
    val requireApprovalToJoin: Boolean = false,
    val pendingJoinRequests: List<String> = emptyList(),
    val onlyAdminsCanSend: Boolean = false,
    val onlyAdminsCanEditInfo: Boolean = false,
    val groupDescription: String = "Official ChatMitra discussion group. Be respectful and collaborative.",
    val groupRules: String = "1. No spam\n2. Respect privacy\n3. Keep on-topic",
    val customWallpaperHex: Long? = null,
    val customWallpaperUri: String? = null,
    val customRingtone: String = "Default",
    val customNotification: String = "Default",
    val mediaStorageMb: Float = 14.5f
)

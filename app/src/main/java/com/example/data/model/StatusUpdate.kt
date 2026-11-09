package com.example.data.model

data class StatusViewer(
    val userName: String,
    val viewedTime: String
)

data class StatusItem(
    val id: String,
    val text: String = "",
    val mediaUrl: String = "",
    val isVideo: Boolean = false,
    val backgroundColorHex: Long = 0xFF008069,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "Just now",
    val viewers: List<StatusViewer> = emptyList()
)

data class UserStatusStory(
    val userId: String,
    val userName: String,
    val avatarColor: Long = 0xFF008069,
    val avatarUrl: String = "",
    val items: List<StatusItem>,
    val isViewed: Boolean = false,
    val isMine: Boolean = false
)

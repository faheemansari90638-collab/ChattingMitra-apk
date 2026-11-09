package com.example.data.model

data class CallLog(
    val id: String,
    val callerName: String,
    val callerPhone: String,
    val callerAvatarColor: Long = 0xFF008069,
    val isVideo: Boolean = false,
    val isIncoming: Boolean = false,
    val isMissed: Boolean = false,
    val isGroupCall: Boolean = false,
    val participantCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "Today, 11:20 AM",
    val durationText: String = "2m 14s"
)

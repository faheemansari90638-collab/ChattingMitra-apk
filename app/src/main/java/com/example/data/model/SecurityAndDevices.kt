package com.example.data.model

data class DeviceSession(
    val id: String,
    val deviceName: String,
    val deviceType: String, // "Chrome on macOS", "Windows Desktop", "iPad Air"
    val location: String,
    val ipAddress: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)

data class SecurityAlert(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val isCritical: Boolean = false
)

data class SupportTicket(
    val id: String,
    val subject: String,
    val userEmail: String,
    val status: String = "Open", // "Open", "Resolved"
    val message: String,
    val date: String,
    val reply: String? = null
)

data class LargeMediaFile(
    val id: String,
    val name: String,
    val type: String, // "Video", "Document", "Photo", "Audio"
    val sizeMb: Float,
    val chatName: String,
    val formattedDate: String
)

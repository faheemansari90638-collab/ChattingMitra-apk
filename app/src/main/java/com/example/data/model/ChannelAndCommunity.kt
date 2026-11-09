package com.example.data.model

data class ChannelPost(
    val id: String,
    val text: String,
    val time: String,
    val viewsCount: Int = 120,
    val sharesCount: Int = 14,
    val reactions: Map<String, Int> = mapOf("❤️" to 42, "🔥" to 28, "👍" to 35)
)

data class Channel(
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean = true,
    val subscribersCount: Int = 1540,
    val isSubscribed: Boolean = false,
    val avatarColor: Long = 0xFF008069,
    val adminName: String = "ChatMitra Official",
    val posts: List<ChannelPost> = emptyList(),
    val isVerified: Boolean = true
)

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val avatarColor: Long = 0xFF128C7E,
    val membersCount: Int = 450,
    val groupNames: List<String> = listOf("General Discussion", "Announcements", "Events & Meetups"),
    val isJoined: Boolean = true
)

package com.example.data.model

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    DOCUMENT,
    AUDIO,
    VOICE_NOTE,
    STICKER,
    POLL,
    EVENT
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class PollOption(
    val id: String,
    val text: String,
    val voterNames: List<String> = emptyList()
)

data class PollData(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val allowMultipleAnswers: Boolean = false,
    val isClosed: Boolean = false
)

data class GroupEvent(
    val id: String,
    val title: String,
    val description: String,
    val dateTimeStr: String,
    val location: String = "ChatMitra Audio Call Room",
    val attendeeNames: List<String> = emptyList()
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val isOutgoing: Boolean,
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.READ,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "10:30 AM",
    val replyTo: ReplySnapshot? = null,
    val isStarred: Boolean = false,
    val isEdited: Boolean = false,
    val editedTime: String = "",
    val isPinned: Boolean = false,
    val mediaUrl: String = "",
    val mediaName: String = "",
    val mediaSizeText: String = "",
    val voiceDurationSeconds: Int = 0,
    val voiceTranscription: String? = null,
    val isTranscribing: Boolean = false,
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of user names
    val translatedText: String? = null,
    val targetLang: String? = null,
    val pollData: PollData? = null,
    val eventData: GroupEvent? = null,
    val isScheduled: Boolean = false,
    val scheduledTimeFormatted: String = "",
    val isSilent: Boolean = false,
    val mentions: List<String> = emptyList(),
    val isViewOnce: Boolean = false,
    val isViewedOnce: Boolean = false
)

data class ReplySnapshot(
    val id: String,
    val senderName: String,
    val text: String,
    val type: MessageType
)

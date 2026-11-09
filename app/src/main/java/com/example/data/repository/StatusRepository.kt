package com.example.data.repository

import com.example.data.model.StatusItem
import com.example.data.model.StatusViewer
import com.example.data.model.UserStatusStory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StatusRepository {
    private val _myStatus = MutableStateFlow<UserStatusStory?>(null)
    val myStatus: StateFlow<UserStatusStory?> = _myStatus.asStateFlow()

    private val _recentStatuses = MutableStateFlow<List<UserStatusStory>>(emptyList())
    val recentStatuses: StateFlow<List<UserStatusStory>> = _recentStatuses.asStateFlow()

    private val _viewedStatuses = MutableStateFlow<List<UserStatusStory>>(emptyList())
    val viewedStatuses: StateFlow<List<UserStatusStory>> = _viewedStatuses.asStateFlow()

    init {
        loadInitialStatuses()
    }

    private fun loadInitialStatuses() {
        val sampleMyStatus = UserStatusStory(
            userId = "user_me",
            userName = "My Status",
            avatarColor = 0xFF008069,
            items = listOf(
                StatusItem(
                    id = "my_s1",
                    text = "Exploring new possibilities with ChatMitra ✨ Clean 3D design & privacy first!",
                    backgroundColorHex = 0xFF008069,
                    formattedTime = "2 hours ago",
                    viewers = listOf(
                        StatusViewer("Aarav Sharma", "1 hour ago"),
                        StatusViewer("Priya Patel", "45 mins ago"),
                        StatusViewer("Rohan Verma", "12 mins ago")
                    )
                )
            ),
            isMine = true
        )
        _myStatus.value = sampleMyStatus

        val contactStories = listOf(
            UserStatusStory(
                userId = "c_aarav",
                userName = "Aarav Sharma",
                avatarColor = 0xFF008069,
                items = listOf(
                    StatusItem(
                        id = "s_aarav_1",
                        text = "Weekend hackathon vibes ☕ Code, build, repeat!",
                        backgroundColorHex = 0xFF128C7E,
                        formattedTime = "35 mins ago"
                    ),
                    StatusItem(
                        id = "s_aarav_2",
                        text = "Sunset at the bay 🌅 What a glorious view!",
                        backgroundColorHex = 0xFFE65100,
                        formattedTime = "20 mins ago"
                    )
                ),
                isViewed = false
            ),
            UserStatusStory(
                userId = "c_priya",
                userName = "Priya Patel",
                avatarColor = 0xFFE91E63,
                items = listOf(
                    StatusItem(
                        id = "s_priya_1",
                        text = "Celebrating the app milestone today! 🎉",
                        backgroundColorHex = 0xFFC2185B,
                        formattedTime = "2 hours ago"
                    )
                ),
                isViewed = false
            ),
            UserStatusStory(
                userId = "c_rohan",
                userName = "Rohan Verma",
                avatarColor = 0xFF3F51B5,
                items = listOf(
                    StatusItem(
                        id = "s_rohan_1",
                        text = "Road trip ahead! 🚗 Mountain air hits different.",
                        backgroundColorHex = 0xFF303F9F,
                        formattedTime = "5 hours ago"
                    )
                ),
                isViewed = true
            )
        )

        _recentStatuses.value = contactStories.filterNot { it.isViewed }
        _viewedStatuses.value = contactStories.filter { it.isViewed }
    }

    fun addMyTextStatus(text: String, backgroundHex: Long) {
        val newItem = StatusItem(
            id = "my_${UUID.randomUUID()}",
            text = text,
            backgroundColorHex = backgroundHex,
            formattedTime = "Just now",
            viewers = emptyList()
        )

        val current = _myStatus.value
        if (current == null) {
            _myStatus.value = UserStatusStory(
                userId = "user_me",
                userName = "My Status",
                avatarColor = 0xFF008069,
                items = listOf(newItem),
                isMine = true
            )
        } else {
            _myStatus.value = current.copy(items = current.items + newItem)
        }
    }

    fun markStoryAsViewed(userId: String) {
        val foundInRecent = _recentStatuses.value.find { it.userId == userId }
        if (foundInRecent != null) {
            _recentStatuses.value = _recentStatuses.value.filterNot { it.userId == userId }
            _viewedStatuses.value = listOf(foundInRecent.copy(isViewed = true)) + _viewedStatuses.value
        }
    }

    fun deleteMyStatusItem(itemId: String) {
        val current = _myStatus.value ?: return
        val remaining = current.items.filterNot { it.id == itemId }
        _myStatus.value = if (remaining.isEmpty()) null else current.copy(items = remaining)
    }
}

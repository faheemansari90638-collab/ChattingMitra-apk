package com.example.data.repository

import com.example.data.model.CallLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ActiveCallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCallState(
    val callId: String = "",
    val callerName: String = "",
    val callerPhone: String = "",
    val callerAvatarColor: Long = 0xFF008069,
    val isVideo: Boolean = false,
    val isGroupCall: Boolean = false,
    val participants: List<String> = listOf("You"),
    val activeSpeaker: String = "",
    val status: ActiveCallStatus = ActiveCallStatus.IDLE,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isFrontCamera: Boolean = true,
    val zoomLevel: Float = 1.0f,
    val networkQualityText: String = "HD 1080p • 32ms",
    val isCallWaitingActive: Boolean = false,
    val callWaitingName: String = ""
)

class CallRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _activeCall = MutableStateFlow(ActiveCallState())
    val activeCall: StateFlow<ActiveCallState> = _activeCall.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    private var durationJob: Job? = null

    init {
        loadInitialCallLogs()
    }

    private fun loadInitialCallLogs() {
        _callLogs.value = listOf(
            CallLog(
                id = "c_1",
                callerName = "Aarav Sharma",
                callerPhone = "+91 98111 22334",
                callerAvatarColor = 0xFF008069,
                isVideo = true,
                isIncoming = true,
                isMissed = false,
                isGroupCall = false,
                formattedDate = "Today, 11:20 AM",
                durationText = "4m 12s"
            ),
            CallLog(
                id = "c_grp_1",
                callerName = "Tech Innovators Group Call",
                callerPhone = "4 participants",
                callerAvatarColor = 0xFF128C7E,
                isVideo = true,
                isIncoming = false,
                isMissed = false,
                isGroupCall = true,
                participantCount = 4,
                formattedDate = "Today, 9:30 AM",
                durationText = "28m 10s"
            ),
            CallLog(
                id = "c_2",
                callerName = "Priya Patel",
                callerPhone = "+91 98222 33445",
                callerAvatarColor = 0xFFE91E63,
                isVideo = false,
                isIncoming = false,
                isMissed = false,
                isGroupCall = false,
                formattedDate = "Yesterday, 6:45 PM",
                durationText = "12m 05s"
            ),
            CallLog(
                id = "c_3",
                callerName = "Rohan Verma",
                callerPhone = "+91 98333 44556",
                callerAvatarColor = 0xFF3F51B5,
                isVideo = false,
                isIncoming = true,
                isMissed = true,
                isGroupCall = false,
                formattedDate = "Nov 5, 2:10 PM",
                durationText = "Missed"
            ),
            CallLog(
                id = "c_4",
                callerName = "Sneha Kulkarni",
                callerPhone = "+91 98444 55667",
                callerAvatarColor = 0xFF9C27B0,
                isVideo = true,
                isIncoming = false,
                isMissed = false,
                isGroupCall = false,
                formattedDate = "Nov 4, 8:30 PM",
                durationText = "8m 40s"
            )
        )
    }

    fun startOutgoingCall(
        name: String,
        phone: String,
        avatarColor: Long,
        isVideo: Boolean,
        isGroup: Boolean = false,
        initialParticipants: List<String> = emptyList()
    ) {
        val callId = UUID.randomUUID().toString()
        val participantsList = if (isGroup && initialParticipants.isNotEmpty()) {
            listOf("You") + initialParticipants
        } else {
            listOf("You", name)
        }

        _activeCall.value = ActiveCallState(
            callId = callId,
            callerName = name,
            callerPhone = phone,
            callerAvatarColor = avatarColor,
            isVideo = isVideo,
            isGroupCall = isGroup,
            participants = participantsList,
            activeSpeaker = name,
            status = ActiveCallStatus.OUTGOING_RINGING,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = isVideo,
            isFrontCamera = true,
            zoomLevel = 1.0f,
            networkQualityText = "HD 1080p • 32ms"
        )

        // Simulate contact answering after 1.8 seconds
        scope.launch {
            delay(1800)
            if (_activeCall.value.status == ActiveCallStatus.OUTGOING_RINGING) {
                _activeCall.value = _activeCall.value.copy(
                    status = ActiveCallStatus.CONNECTED,
                    networkQualityText = "HD 1080p • 28ms"
                )
                startDurationTimer()

                // Simulate call waiting alert after 10s if single call
                if (!isGroup) {
                    delay(10000)
                    if (_activeCall.value.status == ActiveCallStatus.CONNECTED) {
                        _activeCall.value = _activeCall.value.copy(
                            isCallWaitingActive = true,
                            callWaitingName = "Priya Patel"
                        )
                    }
                }
            }
        }
    }

    fun addParticipant(participantName: String) {
        val curr = _activeCall.value
        if (!curr.participants.contains(participantName)) {
            _activeCall.value = curr.copy(
                isGroupCall = true,
                participants = curr.participants + participantName
            )
        }
    }

    fun dismissCallWaiting() {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(isCallWaitingActive = false)
    }

    fun switchCallWaiting() {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(
            callerName = curr.callWaitingName,
            isCallWaitingActive = false,
            activeSpeaker = curr.callWaitingName
        )
    }

    fun triggerIncomingCall(name: String = "Aarav Sharma", phone: String = "+91 98111 22334", isVideo: Boolean = true) {
        _activeCall.value = ActiveCallState(
            callId = UUID.randomUUID().toString(),
            callerName = name,
            callerPhone = phone,
            callerAvatarColor = 0xFF008069,
            isVideo = isVideo,
            participants = listOf("You", name),
            status = ActiveCallStatus.INCOMING_RINGING
        )
    }

    fun acceptIncomingCall() {
        _activeCall.value = _activeCall.value.copy(status = ActiveCallStatus.CONNECTED)
        startDurationTimer()
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (isActive) {
                delay(1000)
                val current = _activeCall.value
                if (current.status == ActiveCallStatus.CONNECTED) {
                    // Alternate speaker in group call occasionally
                    val nextSpeaker = if (current.isGroupCall && current.participants.size > 2) {
                        current.participants.filter { it != "You" }.random()
                    } else current.callerName

                    _activeCall.value = current.copy(
                        durationSeconds = current.durationSeconds + 1,
                        activeSpeaker = nextSpeaker
                    )
                } else break
            }
        }
    }

    fun toggleMute() {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(isMuted = !curr.isMuted)
    }

    fun toggleSpeaker() {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(isSpeakerOn = !curr.isSpeakerOn)
    }

    fun switchCamera() {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(isFrontCamera = !curr.isFrontCamera)
    }

    fun setZoom(level: Float) {
        val curr = _activeCall.value
        _activeCall.value = curr.copy(zoomLevel = level.coerceIn(1.0f, 4.0f))
    }

    fun endCall() {
        durationJob?.cancel()
        val current = _activeCall.value
        if (current.status != ActiveCallStatus.IDLE) {
            val mins = current.durationSeconds / 60
            val secs = current.durationSeconds % 60
            val durationFormatted = String.format("%02d:%02d", mins, secs)

            val newLog = CallLog(
                id = "call_${System.currentTimeMillis()}",
                callerName = if (current.isGroupCall) "${current.callerName} (Group)" else current.callerName,
                callerPhone = current.callerPhone,
                callerAvatarColor = current.callerAvatarColor,
                isVideo = current.isVideo,
                isIncoming = false,
                isMissed = false,
                isGroupCall = current.isGroupCall,
                participantCount = current.participants.size,
                formattedDate = "Just now",
                durationText = durationFormatted
            )
            _callLogs.value = listOf(newLog) + _callLogs.value

            _activeCall.value = current.copy(status = ActiveCallStatus.ENDED)
            scope.launch {
                delay(400)
                _activeCall.value = ActiveCallState(status = ActiveCallStatus.IDLE)
            }
        }
    }

    fun clearCallLogs() {
        _callLogs.value = emptyList()
    }
}

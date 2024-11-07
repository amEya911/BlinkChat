package eu.tutorials.blinkchat.data.event

import androidx.lifecycle.LifecycleOwner

sealed class ChatRoomEvent {
    data class OnLoadChatRoomDetails(val chatRoomId: String): ChatRoomEvent()
    data class OnSetupAppLifecycleObserver(val lifecycleOwner: LifecycleOwner) : ChatRoomEvent()
    data class OnMessageTyping(val message: String) : ChatRoomEvent()
    data object DeleteMessages : ChatRoomEvent()
}
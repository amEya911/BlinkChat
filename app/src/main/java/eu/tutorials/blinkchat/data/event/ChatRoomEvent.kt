package eu.tutorials.blinkchat.data.event

import android.content.Context
import androidx.lifecycle.LifecycleOwner

sealed class ChatRoomEvent {
    data class OnLoadChatRoomDetails(val chatRoomId: String): ChatRoomEvent()
    data class OnSetupAppLifecycleObserver(val lifecycleOwner: LifecycleOwner) : ChatRoomEvent()
    data class OnMessageTyping(val message: String) : ChatRoomEvent()
    data object DeleteMessages : ChatRoomEvent()
    data object OnOtherUserMessageReceived: ChatRoomEvent()
    data object OnAddButtonClicked: ChatRoomEvent()
    data class OnCopyRoomLinkClicked(val chatRoomId: String, val context: Context): ChatRoomEvent()
}
package eu.tutorials.blinkchat.data.event.app

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner

sealed class ChatRoomEvent {
    data class OnLoadChatRoomDetails(val id: String?, val chatRoomId: String) : ChatRoomEvent()
    data class OnSetupAppLifecycleObserver(val lifecycleOwner: LifecycleOwner) : ChatRoomEvent()
    data class OnMessageTyping(val message: String, val isDeleteImage: Boolean) : ChatRoomEvent()
    data object DeleteMessages : ChatRoomEvent()
    data object OnOtherUserMessageReceived: ChatRoomEvent()
    data class OnCopyRoomLinkClicked(val chatRoomId: String, val context: Context): ChatRoomEvent()
    data class OnLaunchCamera(val context: Context, val activity: Activity) : ChatRoomEvent()
    data class OnCaptureImage(val updatedPhoto: Uri?): ChatRoomEvent()
    data object OnRetakePhoto: ChatRoomEvent()
    data object OnDismissPermissionDialog: ChatRoomEvent()
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): ChatRoomEvent()
    data class OnSendPhoto(val uri: Uri, val context: Context): ChatRoomEvent()
    data object OnDismissCamera: ChatRoomEvent()
    data class OnViewImage(val uri: Uri): ChatRoomEvent()
    data object OnDismissViewImage: ChatRoomEvent()
    data class OnImageOpened(val image: String): ChatRoomEvent()
}
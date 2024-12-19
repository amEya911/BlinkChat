package eu.tutorials.blinkchat.data.event

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner

sealed class ChatRoomEvent {
    data class OnLoadChatRoomDetails(val chatRoomId: String, val context: Context): ChatRoomEvent()
    data class OnSetupAppLifecycleObserver(val lifecycleOwner: LifecycleOwner) : ChatRoomEvent()
    data class OnMessageTyping(val message: String) : ChatRoomEvent()
    data object DeleteMessages : ChatRoomEvent()
    data object OnOtherUserMessageReceived: ChatRoomEvent()
    data class OnCopyRoomLinkClicked(val chatRoomId: String, val context: Context): ChatRoomEvent()
    data class OnLaunchCamera(val context: Context, val activity: Activity) : ChatRoomEvent()
    data class OnCaptureImage(val updatedPhoto: Bitmap?): ChatRoomEvent()
    data object OnRetakePhoto: ChatRoomEvent()
    data object OnAccessMedia: ChatRoomEvent()
    data object OnDismissPermissionDialog: ChatRoomEvent()
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): ChatRoomEvent()
    //ata class OnSendPhoto(val bitmap: Bitmap): ChatRoomEvent()
}
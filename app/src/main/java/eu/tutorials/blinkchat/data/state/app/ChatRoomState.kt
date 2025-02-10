package eu.tutorials.blinkchat.data.state.app

import android.net.Uri
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Message
import javax.crypto.SecretKey

data class ChatRoomState(
    val initiatorId: String = "",
    val recipientId: String = "",
    val secretKey: SecretKey? = null,
    val isCurrentUserInChatRoom: Boolean = false,
    val isOtherUserInChatRoom: Boolean = false,
    val currentUserContact: Contact? = null,
    val otherUserContact: Contact? = null,
    val error: String? = null,
    val currentUserMessage: Message = Message(),
    val otherUserMessage: Message = Message(),
    val contacts: List<Contact> = emptyList(),
    val isCameraVisible: Boolean = false,
    val capturedImage: Uri? = null,
    val isViewImageClicked: Boolean = false,
    val selectedViewImage: Uri? = null
)
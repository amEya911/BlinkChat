package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact

data class ChatRoomState(
    val initiatorId: String = "",
    val recipientId: String = "",
    val isCurrentUserInChatRoom: Boolean = false,
    val isOtherUserInChatRoom: Boolean = false,
    val currentUserContact: Contact? = null,
    val otherUserContact: Contact? = null,
    val error: String? = null,
    val currentUserMessage: String = "",
    val otherUserMessage: String = "",
    val readMessage: String? = null,
    val contacts: List<Contact> = emptyList()
)
package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact

data class ChatRoomState(
    val currentUserContact: Contact? = null,
    val otherUserContact: Contact? = null,
    val error: String? = null
)
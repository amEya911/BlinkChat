package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact

data class InboxState(
    val contacts: List<Contact> = emptyList(),
    val isContactClicked: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Contact> = emptyList(),
    val isAllContactsClicked: Boolean = false,
    val selectedContact: Contact? = null,
    val isEnterChatRoom: Boolean = false,
    val navigateToChatId: String? = null,
    val currentUserContact: Contact? = null
)

package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.ContactModel

data class InboxState(
    val contacts: List<ContactModel> = emptyList(),
    val searchQuery: String? = null,
    val searchResults: List<ContactModel> = emptyList(),
    val isContactClicked: Boolean = false,
    val selectedContact: ContactModel? = null,
)

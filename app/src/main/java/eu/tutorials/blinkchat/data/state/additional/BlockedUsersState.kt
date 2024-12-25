package eu.tutorials.blinkchat.data.state.additional

import eu.tutorials.blinkchat.data.model.Contact

data class BlockedUsersState(
    val isBottomSheetShow: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val selectedContact: Contact? = null,
    val searchQuery: String? = null,
    val searchResults: List<Contact> = emptyList()
)

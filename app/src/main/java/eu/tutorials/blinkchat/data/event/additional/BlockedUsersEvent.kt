package eu.tutorials.blinkchat.data.event.additional

import eu.tutorials.blinkchat.data.model.Contact

sealed class BlockedUsersEvent {
    data class OnLoadContacts(val contacts: List<Contact>): BlockedUsersEvent()
    data class OnSearchUsers(val searchQuery: String): BlockedUsersEvent()
    data class OnContactClicked(val contact: Contact): BlockedUsersEvent()
    data object OnDismissBottomSheet: BlockedUsersEvent()
    data object OnReset: BlockedUsersEvent()
}
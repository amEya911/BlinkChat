package eu.tutorials.blinkchat.data.event.additional

import eu.tutorials.blinkchat.data.model.Contact

sealed class AddBlockUsersEvent {
    data class OnLoadContacts(val contacts: List<Contact>): AddBlockUsersEvent()
    data class OnSearchUsers(val searchQuery: String): AddBlockUsersEvent()
    data object OnReset: AddBlockUsersEvent()
    data class OnContactClicked(val contact: Contact): AddBlockUsersEvent()
    data object OnDismissBottomSheet: AddBlockUsersEvent()
    data object OnStartRefresh: AddBlockUsersEvent()
    data object OnEndRefresh: AddBlockUsersEvent()
}
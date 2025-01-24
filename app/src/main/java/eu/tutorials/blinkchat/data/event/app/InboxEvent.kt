package eu.tutorials.blinkchat.data.event.app

import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.RecentChatContact

sealed class InboxEvent {
    data object OnAllContactsIconClicked: InboxEvent()
    data class OnContactClicked(val contact: Contact) : InboxEvent()
    data object OnContactDismissed: InboxEvent()
    data object OnLoadContacts : InboxEvent()
    data class SearchUsers(val searchQuery: String): InboxEvent()
    data object OnEnterChatRoom: InboxEvent()
    data object ResetEnterChatRoom : InboxEvent()
    data object OnScheduleAMeetClick: InboxEvent()
    data object OnScheduleDismissed: InboxEvent()
    data class OnScheduleConfirmed(val otherUserContact: Contact?, val date: String, val time: String) : InboxEvent()
    data class OnDeleteRecentChat(val recentChatContact: RecentChatContact): InboxEvent()
    data class OnBlockUser(val otherUserId: String): InboxEvent()
    data class OnUnblockUser(val otherUserId: String): InboxEvent()
    data class OnMuteUser(val otherUserId: String): InboxEvent()
    data class OnUnmuteUser(val otherUserId: String): InboxEvent()
    data object OnDismissPermissionDialog: InboxEvent()
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): InboxEvent()
    data object OnSnackbarDisplayed : InboxEvent()
}

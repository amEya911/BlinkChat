package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.RecentChatContact

data class InboxState(
    val contacts: List<Contact> = emptyList(),
    val recentContacts: List<RecentChatContact> = emptyList(),
    val isContactClicked: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Contact> = emptyList(),
    val isAllContactsClicked: Boolean = false,
    val selectedContact: Contact? = null,
    val isEnterChatRoom: Boolean = false,
    val navigateToChatId: String? = null,
    val currentUserContact: Contact? = null,
    val usersInChatRoom: List<String> = emptyList(),
    val isScheduleAMeetClicked: Boolean = false,
    val scheduleDate: String? = null,
    val scheduleTime: String? = null,
    val isSelectedContactBlocked: Boolean = false,
    val error: String? = null
)

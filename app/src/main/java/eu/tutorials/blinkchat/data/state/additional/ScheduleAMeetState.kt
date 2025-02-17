package eu.tutorials.blinkchat.data.state.additional

import eu.tutorials.blinkchat.data.model.Contact

data class ScheduleAMeetState(
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String? = null,
    val searchResults: List<Contact> = emptyList(),
    val showDialog: Boolean = false,
    val selectedContact: Contact? = null,
    val isRefreshing: Boolean = false
)

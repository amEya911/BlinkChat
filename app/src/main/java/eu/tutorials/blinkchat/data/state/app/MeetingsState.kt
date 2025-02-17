package eu.tutorials.blinkchat.data.state.app

import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting

data class MeetingsState(
    val meetings: List<Meeting> = emptyList(),
    val currentUserId: String = "",
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String? = null,
    val searchResults: List<Meeting> = emptyList(),
    val isMeetingClicked: Boolean = false,
    val selectedMeeting: Meeting? = null,
    val isRescheduleClicked: Boolean = false,
    val isSortByCreatedAt: Boolean = true,
    val snackbarMessage: String? = null,
    val isRefreshing: Boolean = false
)
package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting

data class MeetingsState(
    val meetings: List<Meeting> = emptyList(),
    val currentUserId: String = "",
    val contacts: List<Contact> = emptyList(),
    val isContactClicked: Boolean = false,
    val selectedMeeting: Meeting? = null
)
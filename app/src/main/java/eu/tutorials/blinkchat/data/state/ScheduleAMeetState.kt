package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Contact

data class ScheduleAMeetState(
    val showDialog: Boolean = false,
    val selectedContact: Contact? = null
)

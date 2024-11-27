package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.model.Meeting

data class MeetingsState(
    val meetings: List<Meeting> = emptyList()
)
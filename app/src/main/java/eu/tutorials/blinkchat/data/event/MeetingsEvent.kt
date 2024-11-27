package eu.tutorials.blinkchat.data.event

import eu.tutorials.blinkchat.data.model.Meeting

sealed class MeetingsEvent {
    data object OnLoadMeetings: MeetingsEvent()
    data class OnMeetingClicked(val meeting: Meeting): MeetingsEvent()
    data object OnMeetingDismissed: MeetingsEvent()
}
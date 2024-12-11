package eu.tutorials.blinkchat.data.event

import eu.tutorials.blinkchat.data.model.Meeting

sealed class MeetingsEvent {
    data object OnLoadMeetings: MeetingsEvent()
    data class OnMeetingClicked(val meeting: Meeting): MeetingsEvent()
    data object OnMeetingDismissed: MeetingsEvent()
    data class OnRescheduleConfirmed(val meeting: Meeting, val newDate: String, val newTime: String): MeetingsEvent()
    data object OnRescheduleClicked: MeetingsEvent()
    data object OnRescheduleDismissed: MeetingsEvent()
    data class OnCallOffClicked(val meeting: Meeting): MeetingsEvent()
    data object OnSortByCreatedAt: MeetingsEvent()
    data object OnSortByTime: MeetingsEvent()
}
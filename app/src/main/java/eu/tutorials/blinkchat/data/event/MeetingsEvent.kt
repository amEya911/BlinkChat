package eu.tutorials.blinkchat.data.event

sealed class MeetingsEvent {
    data object OnLoadMeetings: MeetingsEvent()
}
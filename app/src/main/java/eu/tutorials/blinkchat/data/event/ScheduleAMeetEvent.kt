package eu.tutorials.blinkchat.data.event

import eu.tutorials.blinkchat.data.model.Contact

sealed class ScheduleAMeetEvent {
    data class OnButtonClicked(val selectedContact: Contact?): ScheduleAMeetEvent()
    data object OnDismiss: ScheduleAMeetEvent()
}
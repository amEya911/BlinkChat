package eu.tutorials.blinkchat.data.event.additional

import eu.tutorials.blinkchat.data.model.Contact

sealed class ScheduleAMeetEvent {
    data class OnLoadContacts(val contacts: List<Contact>): ScheduleAMeetEvent()
    data class OnButtonClicked(val selectedContact: Contact?): ScheduleAMeetEvent()
    data object OnDismiss: ScheduleAMeetEvent()
    data class OnSearchUsers(val searchQuery: String): ScheduleAMeetEvent()
    data object OnStartRefresh: ScheduleAMeetEvent()
    data object OnEndRefresh: ScheduleAMeetEvent()
}
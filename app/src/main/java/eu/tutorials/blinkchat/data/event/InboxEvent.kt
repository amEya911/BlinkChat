package eu.tutorials.blinkchat.data.event

import android.app.Activity


sealed class InboxEvent {
    object OnAllContactsIconClicked: InboxEvent()
    data class LoadContacts(val activity: Activity) : InboxEvent()
    data class SearchUsers(val searchQuery: String): InboxEvent()
}

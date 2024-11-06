package eu.tutorials.blinkchat.data.event

import android.app.Activity
import eu.tutorials.blinkchat.data.model.Contact


sealed class InboxEvent {
    data object OnAllContactsIconClicked: InboxEvent()
    data class OnContactClicked(val contact: Contact) : InboxEvent()
    data object OnContactDismissed: InboxEvent()
    data class LoadContacts(val activity: Activity) : InboxEvent()
    data class SearchUsers(val searchQuery: String): InboxEvent()
    data object OnEnterChatRoom: InboxEvent()
    data object ResetEnterChatRoom : InboxEvent()
}

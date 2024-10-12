package eu.tutorials.blinkchat.data.event

import android.app.Activity
import eu.tutorials.blinkchat.data.model.ContactModel

sealed class InboxEvent {
    data class LoadContacts(val activity: Activity) : InboxEvent()
    data class SearchUsers(val searchQuery: String): InboxEvent()
    data class OnContactClicked(val contact: ContactModel): InboxEvent()
    data object OnContactDismiss: InboxEvent()
    data class StartChatWithContact(val contact: ContactModel): InboxEvent()
}

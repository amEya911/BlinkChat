package eu.tutorials.blinkchat.data.event.app

sealed class ProfileEvent {
    data class OnLoadCurrentContact(val id: String): ProfileEvent()
    data object OnNameClicked: ProfileEvent()
    data class OnNameConfirmed(val id: String, val newName: String) : ProfileEvent()
}
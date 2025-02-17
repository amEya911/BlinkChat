package eu.tutorials.blinkchat.data.event.additional

sealed class ProfileEvent {
    data class OnLoadCurrentContact(val id: String): ProfileEvent()
    data object OnNameClicked: ProfileEvent()
    data class OnNameConfirmed(val id: String, val newName: String): ProfileEvent()
    data object OnStartRefresh: ProfileEvent()
    data object OnEndRefresh: ProfileEvent()
}
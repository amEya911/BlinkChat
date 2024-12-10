package eu.tutorials.blinkchat.data.event

sealed class SettingsEvent {
    data object OnLoadBlockedUsers: SettingsEvent()
    data object Logout : SettingsEvent()
}
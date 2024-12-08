package eu.tutorials.blinkchat.data.event

sealed class SettingsEvent {
    data object OnDisplayBlockedUsers: SettingsEvent()
    data object OnDismissDisplayBlockedUsers: SettingsEvent()
}
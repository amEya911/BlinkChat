package eu.tutorials.blinkchat.data.event.app

sealed class SettingsEvent {
    data object OnLoadBlockedUsers: SettingsEvent()
    data object OnLogout: SettingsEvent()
    data object OnDeleteAccount: SettingsEvent()
}
package eu.tutorials.blinkchat.data.event.app

sealed class SettingsEvent {
    data object OnLoadBlockedUsers: SettingsEvent()
    data object OnLogoutClicked: SettingsEvent()
    data object OnLogout: SettingsEvent()
    data object OnLogoutDismissed: SettingsEvent()
    data object OnDeleteAccount: SettingsEvent()
    data object OnDeleteAccountClicked: SettingsEvent()
    data object OnDeleteAccountDismissed: SettingsEvent()
    data object OnThemeClicked: SettingsEvent()
    data class OnThemeChanged(val theme: AppTheme): SettingsEvent()
}

enum class AppTheme {
    SYSTEM_DEFAULT, LIGHT, DARK
}
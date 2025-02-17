package eu.tutorials.blinkchat.data.event.app

import android.app.Activity

sealed class SettingsEvent {
    data object OnLoadBlockedUsers: SettingsEvent()
    data object OnLogoutClicked: SettingsEvent()
    data object OnLogout: SettingsEvent()
    data object OnDeleteAccount: SettingsEvent()
    data object OnDeleteAccountClicked: SettingsEvent()
    data object OnThemeClicked: SettingsEvent()
    data class OnThemeChanged(val theme: AppTheme, val activity: Activity): SettingsEvent()
    data class OnNotificationsTypeChanged(val notificationType: NotificationsType): SettingsEvent()
    data object OnNotificationsClicked: SettingsEvent()
    data object OnStartRefresh: SettingsEvent()
}

enum class AppTheme {
    SYSTEM_DEFAULT, LIGHT, DARK
}

enum class NotificationsType {
    PRIVATE, PUBLIC
}
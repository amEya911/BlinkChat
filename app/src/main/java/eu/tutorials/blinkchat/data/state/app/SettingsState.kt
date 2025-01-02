package eu.tutorials.blinkchat.data.state.app

import eu.tutorials.blinkchat.data.event.app.AppTheme
import eu.tutorials.blinkchat.data.model.Contact


data class SettingsState(
    val blockedUsers: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val errorMessage: String? = null,
    val isLogoutClicked: Boolean = false,
    val isDeleteAccountClicked: Boolean = false,
    val isThemeClicked: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.SYSTEM_DEFAULT
)

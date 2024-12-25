package eu.tutorials.blinkchat.data.state.app

import eu.tutorials.blinkchat.data.model.Contact

data class SettingsState(
    val blockedUsers: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val errorMessage: String? = null
)

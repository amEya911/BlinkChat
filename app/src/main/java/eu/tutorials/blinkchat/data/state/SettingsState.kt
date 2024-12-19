package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContact

data class SettingsState(
    val isLoggedIn: Boolean = true,
    val blockedUsers: List<LocalContact> = emptyList()
)

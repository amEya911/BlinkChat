package eu.tutorials.blinkchat.data.state

import eu.tutorials.blinkchat.data.datasource.local.LocalContact

data class SettingsState(
    val blockedUsers: List<LocalContact> = emptyList(),
    val isShowBlockedUsersClicked: Boolean = false
)

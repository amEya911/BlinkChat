package eu.tutorials.blinkchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.SettingsEvent
import eu.tutorials.blinkchat.data.state.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
): ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnDisplayBlockedUsers -> {
                displayBlockedUsers()
            }

            SettingsEvent.OnDismissDisplayBlockedUsers -> {
                _settingsState.value = _settingsState.value.copy(
                    isShowBlockedUsersClicked = false
                )
            }
        }
    }

    private fun displayBlockedUsers() {
        viewModelScope.launch {
            val currentUserId = userRepository.currentUserId()
            if (currentUserId == null) {
                Log.e("SettingsViewModel", "CurrentUserId not loaded")
                return@launch
            }

            val blockedUserIds = fetchBlockedUsers(currentUserId)
            val localContacts = localRepository.getContacts()
            val blockedContacts = localContacts.filter { it.id in blockedUserIds }

            _settingsState.value = _settingsState.value.copy(
                isShowBlockedUsersClicked = true,
                blockedUsers = blockedContacts
            )
        }
    }

    private suspend fun fetchBlockedUsers(currentUserId: String): List<String> {
        return suspendCancellableCoroutine { continuation ->
            userRepository.getAllBlockedUsers(currentUserId) { blockedUserIds ->
                continuation.resume(blockedUserIds)
            }
        }
    }
}
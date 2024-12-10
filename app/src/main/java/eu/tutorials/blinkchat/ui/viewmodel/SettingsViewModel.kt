package eu.tutorials.blinkchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.SettingsEvent
import eu.tutorials.blinkchat.data.state.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
): ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnLoadBlockedUsers -> {
                loadBlockedUsers()
            }
            SettingsEvent.Logout -> {
                logout()
            }
        }
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            val currentUserId = userRepository.currentUserId()
            if (currentUserId == null) {
                Log.e("SettingsViewModel", "CurrentUserId not loaded")
                return@launch
            }

            userRepository.getAllBlockedUsers(currentUserId) { blockedUserIds ->
                viewModelScope.launch {
                    val localContacts = localRepository.getContacts()
                    val blockedContacts = localContacts.filter { it.id in blockedUserIds }

                    _settingsState.value = _settingsState.value.copy(
                        blockedUsers = blockedContacts
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                _settingsState.value = _settingsState.value.copy(
                    isLoggedIn = false
                )
                Log.d("SettingsViewModel", "User successfully logged out")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error logging out: ${e.message}")
            }
        }
    }
}
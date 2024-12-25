package eu.tutorials.blinkchat.ui.viewmodel.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.SettingsEvent
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.app.SettingsState
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
            SettingsEvent.OnLogout -> {
                logout()
            }

            SettingsEvent.OnDeleteAccount -> {
                deleteAccount()
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
                    val blockedContacts = localContacts.filter { it.id in blockedUserIds }.map { it.toContact() }

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
                _settingsState.value = _settingsState.value.copy(isLoading = true)
                userRepository.logout()
                _settingsState.value = _settingsState.value.copy(isLoading = false, isLoggedOut = true)

                Log.d("SettingsViewModel", "User successfully logged out")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error logging out: ${e.message}")
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to log out. Please try again."
                )
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            try {
                _settingsState.value = _settingsState.value.copy(isLoading = true)

                userRepository.deleteAccount { success ->
                    if (success) {
                        Log.d("SettingsViewModel", "User account deleted successfully.")
                        // Notify the UI about the successful deletion
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            isAccountDeleted = true
                        )
                    } else {
                        Log.e("SettingsViewModel", "Failed to delete user account.")
                        _settingsState.value = _settingsState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to delete account. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error deleting account: ${e.message}")
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

}
package eu.tutorials.blinkchat.ui.viewmodel.app

import android.app.Activity
import android.util.Log
import androidx.core.app.ActivityCompat.recreate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.NotificationsTypePreferences
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.ThemePreferences
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.AppTheme
import eu.tutorials.blinkchat.data.event.app.SettingsEvent
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.app.SettingsState
import eu.tutorials.blinkchat.util.ConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository,
    private val themePreferences: ThemePreferences,
    private val notificationsTypePreferences: NotificationsTypePreferences
): ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState(
        selectedTheme = themePreferences.loadTheme(),
        selectedNotificationsType = notificationsTypePreferences.loadNotificationsType()
    ))
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

            SettingsEvent.OnDeleteAccountClicked -> {
                _settingsState.value = _settingsState.value.copy(
                    isDeleteAccountClicked =! _settingsState.value.isDeleteAccountClicked
                )
            }
            SettingsEvent.OnLogoutClicked -> {
                _settingsState.value = _settingsState.value.copy(
                    isLogoutClicked =! _settingsState.value.isLogoutClicked
                )
            }

            SettingsEvent.OnThemeClicked -> {
                _settingsState.value = _settingsState.value.copy(isThemeClicked =! _settingsState.value.isThemeClicked)
            }
            is SettingsEvent.OnThemeChanged -> {
                themePreferences.saveTheme(event.theme)
                onThemeChanged(event.activity)
                _settingsState.value = _settingsState.value.copy(
                    selectedTheme = event.theme,
                    isThemeClicked = false
                )
            }

            SettingsEvent.OnNotificationsClicked -> {
                _settingsState.value = _settingsState.value.copy(isNotificationsClicked =! _settingsState.value.isNotificationsClicked)
            }

            is SettingsEvent.OnNotificationsTypeChanged -> {
                _settingsState.value = _settingsState.value.copy(
                    selectedNotificationsType = event.notificationType,
                    isNotificationsClicked = false
                )
                notificationsTypePreferences.saveNotificationsType(event.notificationType)
            }

            SettingsEvent.OnStartRefresh -> {
                _settingsState.value = _settingsState.value.copy(
                    isRefreshing = true
                )
                refreshData()
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            delay(500)

            while (!ConnectivityObserver.isOnline.value) {
                delay(1000)
            }

            try {
                loadBlockedUsers()
            } finally {
                _settingsState.value = _settingsState.value.copy(
                    isRefreshing = false
                )
            }
        }
    }

    private fun onThemeChanged(activity: Activity) {
        recreate(activity)
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
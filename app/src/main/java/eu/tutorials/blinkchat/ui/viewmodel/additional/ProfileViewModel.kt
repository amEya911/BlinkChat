package eu.tutorials.blinkchat.ui.viewmodel.additional

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.additional.ProfileEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.additional.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
): ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    fun onEvent(event: ProfileEvent) {
        when(event) {
            is ProfileEvent.OnLoadCurrentContact -> {
                getUserDetails(id = event.id)
            }
            ProfileEvent.OnNameClicked -> {
                _profileState.value = _profileState.value.copy(
                    isNameClicked = !_profileState.value.isNameClicked
                )
            }
            is ProfileEvent.OnNameConfirmed -> {
                _profileState.value = _profileState.value.copy(
                    isNameClicked = false,
                    currentUserContact = _profileState.value.currentUserContact.copy(
                        displayName = event.newName
                    )
                )
                addDisplayName(
                    id = event.id,
                    newName = event.newName
                )
            }

            ProfileEvent.OnEndRefresh -> {
                _profileState.value = _profileState.value.copy(
                    isRefreshing = false
                )
            }

            ProfileEvent.OnStartRefresh -> {
                _profileState.value = _profileState.value.copy(
                    isRefreshing = true
                )
            }
        }
    }

    private fun getUserDetails(id: String) {
        userRepository.getUserDetails(id) { contact ->
            _profileState.value = _profileState.value.copy(
                currentUserContact = contact ?: Contact("", "", "", null, null)
            )
        }
    }

    private fun addDisplayName(id: String, newName: String) {
        userRepository.addDisplayName(
            id = id,
            displayName = newName
        )
    }
}
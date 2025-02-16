package eu.tutorials.blinkchat.ui.viewmodel.additional

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.ProfileEvent
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
                userRepository.getUserDetails(event.id) { contact ->
                    _profileState.value = _profileState.value.copy(
                        currentUserContact = contact ?: Contact("", "", "", null, null)
                    )
                }
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
                userRepository.addDisplayName(
                    id = event.id,
                    displayName = event.newName
                )
            }
        }
    }
}
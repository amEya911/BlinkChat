package eu.tutorials.blinkchat.ui.viewmodel.additional

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.additional.AddBlockUsersEvent
import eu.tutorials.blinkchat.data.state.additional.AddBlockUsersState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AddBlockUsersViewModel @Inject constructor(): ViewModel() {

    private val _addBlockUsersState = MutableStateFlow(AddBlockUsersState())
    val addBlockUsersState: StateFlow<AddBlockUsersState> = _addBlockUsersState

    fun onEvent(event: AddBlockUsersEvent) {
        when (event) {
            is AddBlockUsersEvent.OnLoadContacts -> {
                _addBlockUsersState.value = _addBlockUsersState.value.copy(
                    contacts = event.contacts
                )
            }
            is AddBlockUsersEvent.OnSearchUsers -> {
                searchUsers(event.searchQuery)
            }

            AddBlockUsersEvent.OnReset -> {
                _addBlockUsersState.value = _addBlockUsersState.value.copy(
                    searchQuery = null
                )
            }

            is AddBlockUsersEvent.OnContactClicked -> {
                _addBlockUsersState.value = _addBlockUsersState.value.copy(
                    selectedContact = event.contact,
                    isBottomSheetShow = true
                )
            }

            AddBlockUsersEvent.OnDismissBottomSheet -> {
                _addBlockUsersState.value = _addBlockUsersState.value.copy(
                    isBottomSheetShow = false
                )
            }
        }
    }

    private fun searchUsers(query: String) {
        val filteredContacts = _addBlockUsersState.value.contacts.filter { contact ->
            contact.displayName.contains(query, ignoreCase = true) ||
                    contact.phoneNumber.contains(query)
        }
        _addBlockUsersState.value = _addBlockUsersState.value.copy(
            searchQuery = query,
            searchResults = filteredContacts
        )
    }
}
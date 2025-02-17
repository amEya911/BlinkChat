package eu.tutorials.blinkchat.ui.viewmodel.additional

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.additional.BlockedUsersEvent
import eu.tutorials.blinkchat.data.state.additional.BlockedUsersState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(): ViewModel() {

    private val _blockedUsersState = MutableStateFlow(BlockedUsersState())
    val blockedUsersState: StateFlow<BlockedUsersState> = _blockedUsersState

    fun onEvent(event: BlockedUsersEvent) {
        when (event) {
            is BlockedUsersEvent.OnLoadContacts -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    contacts = event.contacts
                )
            }

            is BlockedUsersEvent.OnSearchUsers -> {
                searchUsers(event.searchQuery)
            }

            BlockedUsersEvent.OnReset -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    selectedContact = null,
                    searchQuery = null
                )
            }

            is BlockedUsersEvent.OnContactClicked -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    isBottomSheetShow = true,
                    selectedContact = event.contact
                )
            }

            BlockedUsersEvent.OnDismissBottomSheet -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    isBottomSheetShow = false
                )
            }

            BlockedUsersEvent.OnEndRefresh -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    isRefreshing = false
                )
            }

            BlockedUsersEvent.OnStartRefresh -> {
                _blockedUsersState.value = _blockedUsersState.value.copy(
                    isRefreshing = true
                )
            }
        }
    }

    private fun searchUsers(query: String) {
        val filteredContacts = _blockedUsersState.value.contacts.filter { contact ->
            contact.displayName.contains(query, ignoreCase = true) ||
                    contact.phoneNumber.contains(query)
        }
        _blockedUsersState.value = _blockedUsersState.value.copy(
            searchQuery = query,
            searchResults = filteredContacts
        )
    }
}
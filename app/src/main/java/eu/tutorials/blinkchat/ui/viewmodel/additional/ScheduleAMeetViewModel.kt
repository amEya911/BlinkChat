package eu.tutorials.blinkchat.ui.viewmodel.additional

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.additional.ScheduleAMeetEvent
import eu.tutorials.blinkchat.data.state.additional.ScheduleAMeetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleAMeetViewModel @Inject constructor(): ViewModel() {

    private val _scheduleAMeetState = MutableStateFlow(ScheduleAMeetState())
    val scheduleAMeetState: StateFlow<ScheduleAMeetState> = _scheduleAMeetState

    fun onEvent(event: ScheduleAMeetEvent) {
        when (event) {
            is ScheduleAMeetEvent.OnLoadContacts -> {
                _scheduleAMeetState.value = _scheduleAMeetState.value.copy(
                    contacts = event.contacts
                )
            }

            is ScheduleAMeetEvent.OnButtonClicked -> {
                _scheduleAMeetState.value = _scheduleAMeetState.value.copy(
                    showDialog = true,
                    selectedContact = event.selectedContact
                )
            }

            ScheduleAMeetEvent.OnDismiss ->  _scheduleAMeetState.value = _scheduleAMeetState.value.copy(
                showDialog = false,
                selectedContact = null
            )

            is ScheduleAMeetEvent.OnSearchUsers -> {
                searchUsers(event.searchQuery)
            }
        }
    }

    private fun searchUsers(query: String) {
        val filteredContacts = _scheduleAMeetState.value.contacts.filter { contact ->
            contact.displayName.contains(query, ignoreCase = true) ||
                    contact.phoneNumber.contains(query)
        }
        _scheduleAMeetState.value = _scheduleAMeetState.value.copy(
            searchQuery = query,
            searchResults = filteredContacts
        )
    }
}
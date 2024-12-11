package eu.tutorials.blinkchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.ScheduleAMeetEvent
import eu.tutorials.blinkchat.data.state.ScheduleAMeetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleAMeetViewModel @Inject constructor(): ViewModel() {

    private val _scheduleAMeetState = MutableStateFlow(ScheduleAMeetState())
    val scheduleAMeetState: StateFlow<ScheduleAMeetState> = _scheduleAMeetState

    fun onEvent(event: ScheduleAMeetEvent) {
        when (event) {
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
        }
    }
}
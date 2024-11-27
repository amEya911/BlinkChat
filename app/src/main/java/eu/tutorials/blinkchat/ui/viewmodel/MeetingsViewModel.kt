package eu.tutorials.blinkchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.state.MeetingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MeetingsViewModel @Inject constructor(
    private val meetRepository: MeetRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private val _meetingsSate = MutableStateFlow(MeetingsState())
    val meetingsState: StateFlow<MeetingsState> = _meetingsSate

    init {
        Log.d("MeetingsViewModel", "Launching init")
        loadMeetings()
    }

    fun onEvent(event: MeetingsEvent) {
        when(event) {
            MeetingsEvent.OnLoadMeetings -> {
                loadMeetings()
            }
        }
    }

    private fun loadMeetings() {
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            meetRepository.listenForMeetings(currentUserId) { meeting: List<Meeting> ->
                Log.d("ScheduledMeets", "meeting: $meeting")
                _meetingsSate.value = _meetingsSate.value.copy(
                    meetings = meeting
                )
            }
        }
    }
}
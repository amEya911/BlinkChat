package eu.tutorials.blinkchat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.MeetingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingsViewModel @Inject constructor(
    private val meetRepository: MeetRepository,
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
): ViewModel() {

    private val _meetingsSate = MutableStateFlow(MeetingsState())
    val meetingsState: StateFlow<MeetingsState> = _meetingsSate

    init {
        loadMeetings()
    }

    fun onEvent(event: MeetingsEvent) {
        when(event) {
            MeetingsEvent.OnLoadMeetings -> {
                loadMeetings()
            }

            is MeetingsEvent.OnMeetingClicked -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isMeetingClicked = true,
                    selectedMeeting = event.meeting
                )
            }
            MeetingsEvent.OnMeetingDismissed -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isMeetingClicked = false
                )
            }

            is MeetingsEvent.OnRescheduleConfirmed -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isRescheduleClicked = false
                )
                rescheduleMeet(event.meeting, event.newDate, event.newTime)
            }

            MeetingsEvent.OnRescheduleClicked -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isRescheduleClicked = true
                )
            }
            MeetingsEvent.OnRescheduleDismissed -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isRescheduleClicked = false
                )
            }
        }
    }

    private fun loadMeetings() {
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            meetRepository.listenForMeetings(currentUserId) { meeting: List<Meeting> ->
                Log.d("ScheduledMeets", "meeting: $meeting")
                _meetingsSate.value = _meetingsSate.value.copy(
                    meetings = meeting,
                    currentUserId = currentUserId
                )
            }
        }
        viewModelScope.launch {
            try {
                val localContacts = localRepository.getContacts()
                val contacts = localContacts.map { it.toContact() }
                _meetingsSate.value = _meetingsSate.value.copy(
                    contacts = contacts
                )
            } catch (e: Exception) {
                Log.e("MeetingsViewModel", "Error loading contacts", e)
            }
        }
    }

    private fun rescheduleMeet(meeting: Meeting, newDate: String, newTime: String) {
        meetRepository.rescheduleMeet(
            meetingId = meeting.meetingId,
            createdBy = meeting.createdBy,
            createdWith = meeting.otherUserContact,
            newDate = newDate,
            newTime = newTime
        )
    }
}
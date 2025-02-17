package eu.tutorials.blinkchat.ui.viewmodel.app

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.event.app.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.app.MeetingsState
import eu.tutorials.blinkchat.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import eu.tutorials.blinkchat.util.DateTimeUtils.toLocalDateTime
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MeetingsViewModel @Inject constructor(
    private val meetRepository: MeetRepository,
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _meetingsSate = MutableStateFlow(MeetingsState())
    val meetingsState: StateFlow<MeetingsState> = _meetingsSate

    init {
        loadMeetings()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: MeetingsEvent) {
        when (event) {
            is MeetingsEvent.OnMeetingClicked -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isMeetingClicked = true,
                    selectedMeeting = event.meeting
                )
            }

            MeetingsEvent.OnMeetingDismissed -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isRescheduleClicked = false,
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

            is MeetingsEvent.OnCallOffClicked -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isMeetingClicked = false
                )
                deleteMeet(meeting = event.meeting, false)
            }

            MeetingsEvent.OnSortByCreatedAt -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isSortByCreatedAt = true,
                    meetings = _meetingsSate.value.meetings.sortedBy { it.createdAt },
                    searchResults = _meetingsSate.value.searchResults.sortedBy { it.createdAt }
                )
            }

            MeetingsEvent.OnSortByTime -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isSortByCreatedAt = false,
                    meetings = _meetingsSate.value.meetings.sortedByDescending { meeting ->
                        meeting.date.toLocalDateTime(meeting.time)
                    },
                    searchResults = _meetingsSate.value.searchResults.sortedByDescending { meeting ->
                        meeting.date.toLocalDateTime(meeting.time)
                    }
                )
            }

            is MeetingsEvent.OnSearchUsers -> {
                searchUsers(event.searchQuery)
            }

            MeetingsEvent.OnSnackbarDisplayed -> {
                _meetingsSate.value = _meetingsSate.value.copy(
                    snackbarMessage = null
                )
            }

            is MeetingsEvent.OnDeleteMeeting -> {
                deleteMeet(event.meeting, true)
            }

            MeetingsEvent.OnStartRefresh -> {
                _meetingsSate.value = _meetingsSate.value.copy(
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
                loadMeetings()
            } finally {
                _meetingsSate.value = _meetingsSate.value.copy(
                    isRefreshing = false
                )
            }
        }
    }

    private fun loadMeetings() {
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            meetRepository.listenForMeetings(currentUserId) { meeting: List<Meeting> ->
                Log.d("ScheduledMeets", "meeting: $meeting")
                val sortedMeetings = if (_meetingsSate.value.isSortByCreatedAt) {
                    meeting.sortedBy { it.createdAt }
                } else {
                    meeting.sortedByDescending { it.date.toLocalDateTime(it.time) }
                }
                _meetingsSate.value = _meetingsSate.value.copy(
                    meetings = sortedMeetings,
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
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            userRepository.fetchBlockedAndMutedData(
                meeting.otherUserContact.id,
                currentUserId
            ) { blocked, muted ->
                meetRepository.rescheduleMeet(
                    meetingId = meeting.meetingId,
                    secretKey = meeting.secretKey,
                    currentUserId = currentUserId,
                    otherUserId = meeting.otherUserContact.id,
                    newDate = newDate,
                    newTime = newTime,
                    isUserMuted = muted,
                    isBlocked = blocked
                ) { result, message ->
                    if (result) {
                        _meetingsSate.value = _meetingsSate.value.copy(
                            snackbarMessage = "Meet successfully rescheduled"
                        )
                    } else {
                        _meetingsSate.value = _meetingsSate.value.copy(
                            snackbarMessage = "Error rescheduling meet: $message"
                        )
                    }
                }

            }
        } else {
            Log.e("scheduleMeet", "currentUserId not loaded")
        }
    }

    private fun deleteMeet(meeting: Meeting, isExpired: Boolean) {
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            userRepository.fetchBlockedAndMutedData(
                meeting.otherUserContact.id,
                currentUserId
            ) { blocked, muted ->
                meetRepository.deleteMeet(
                    meetingId = meeting.meetingId,
                    currentUserId = currentUserId,
                    otherUserId = meeting.otherUserContact.id,
                    isUserMuted = muted || isExpired,
                    isBlocked = blocked
                ) { result, message ->
                    if (!isExpired) {
                        if (result) {
                            _meetingsSate.value = _meetingsSate.value.copy(
                                snackbarMessage = "Meet successfully deleted"
                            )
                        } else {
                            _meetingsSate.value = _meetingsSate.value.copy(
                                snackbarMessage = "Error deleting meet: $message"
                            )
                        }
                    }
                }

            }
        } else {
            Log.e("scheduleMeet", "currentUserId not loaded")
        }
    }

    private fun searchUsers(query: String) {
        val filteredContacts = _meetingsSate.value.meetings.filter { contact ->
            contact.otherUserContact.displayName.contains(query, ignoreCase = true) ||
                    contact.otherUserContact.phoneNumber.contains(query)
        }
        _meetingsSate.value = _meetingsSate.value.copy(
            searchQuery = query,
            searchResults = filteredContacts
        )
    }
}
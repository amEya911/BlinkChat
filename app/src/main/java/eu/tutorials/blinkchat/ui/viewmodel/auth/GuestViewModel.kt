package eu.tutorials.blinkchat.ui.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.event.auth.GuestEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.auth.GuestState
import eu.tutorials.blinkchat.util.HashUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GuestViewModel @Inject constructor(
    private val appRepository: AppRepository
): ViewModel() {

    private val _guestState = MutableStateFlow(GuestState())
    val guestState: StateFlow<GuestState> = _guestState

    fun onEvent(event: GuestEvent) {
        when (event) {
            is GuestEvent.OnCreateRoom -> {
                createRoom(context = event.context)
            }
        }
    }

    private fun createRoom(context: Context) {
        _guestState.value = _guestState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val initiatorUUID = UUID.randomUUID().toString()
                val recipientUUID = UUID.randomUUID().toString()
                val initiatorUserId = HashUtil.hashString(initiatorUUID)
                val recipientUserId = HashUtil.hashString(recipientUUID)

                appRepository.createChatRoom(
                    initiatorUser = Contact(initiatorUserId, "Guest", "", null, null),
                    recipientUser = Contact(recipientUserId, "Guest", "", null, null),
                    context = context,
                    recipientUserExists = false,
                    notifyOtherUser = false,
                    isGuest = true
                ) { createdRoomId ->
                    if (createdRoomId != null) {
                        val roomLink = "https://vanishtest.netlify.app/$createdRoomId?id=$recipientUserId"
                        _guestState.value = _guestState.value.copy(
                            initiatorId = initiatorUserId,
                            recipientId = recipientUserId,
                            chatRoomId = createdRoomId,
                            roomLink = roomLink,
                            isLoading = false
                        )
                    } else {
                        _guestState.value = _guestState.value.copy(
                            errorMessage = "Failed to create room",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _guestState.value = _guestState.value.copy(
                    errorMessage = e.localizedMessage ?: "An unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
}
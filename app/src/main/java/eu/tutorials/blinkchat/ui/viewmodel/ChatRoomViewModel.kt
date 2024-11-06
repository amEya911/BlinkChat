package eu.tutorials.blinkchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.state.ChatRoomState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chatRoomState = MutableStateFlow(ChatRoomState())
    val chatRoomState: StateFlow<ChatRoomState> = _chatRoomState

    fun loadChatRoomDetails(chatRoomId: String) {
        val currentUserId = userRepository.currentUserId()!!
        viewModelScope.launch {
            val result = appRepository.getChatRoomDetails(chatRoomId, currentUserId)
            _chatRoomState.value = result.fold(
                onSuccess = { (currentUser, otherUser) ->
                    _chatRoomState.value.copy(
                        currentUserContact = currentUser,
                        otherUserContact = otherUser
                    )
                },
                onFailure = { error -> _chatRoomState.value.copy(error = error.message) }
            )
        }
    }

}

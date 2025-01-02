package eu.tutorials.blinkchat.ui.viewmodel.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.ChatRoomEvent
import eu.tutorials.blinkchat.data.model.Image
import eu.tutorials.blinkchat.data.model.Message
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.app.ChatRoomState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _chatRoomState = MutableStateFlow(ChatRoomState())
    val chatRoomState: StateFlow<ChatRoomState> = _chatRoomState

    private val _visiblePermissionDialogQueue = MutableStateFlow<List<String>>(emptyList())
    val visiblePermissionDialogQueue: StateFlow<List<String>> = _visiblePermissionDialogQueue

    private var chatRoomId: String? = null
    private lateinit var currentUserId: String

    fun onEvent(event: ChatRoomEvent) {
        when (event) {
            is ChatRoomEvent.OnLoadChatRoomDetails -> {
                this.chatRoomId = event.chatRoomId
                this.currentUserId = event.id ?: userRepository.currentUserId() ?: ""
                loadChatRoomDetails(event.chatRoomId)
            }

            is ChatRoomEvent.OnSetupAppLifecycleObserver -> {
                setupAppLifecycleObserver(event.lifecycleOwner)
            }

            is ChatRoomEvent.OnMessageTyping -> {
                _chatRoomState.value = _chatRoomState.value.copy(
                    currentUserMessage = _chatRoomState.value.currentUserMessage.copy(
                        messageText = event.message
                    )
                )
                if (event.isDeleteImage) {
                    _chatRoomState.value = _chatRoomState.value.copy(
                        currentUserMessage = _chatRoomState.value.currentUserMessage.copy(
                            imageUrls = emptyList()
                        )
                    )
                }
                chatRoomId?.let { id ->
                    viewModelScope.launch {
                        appRepository.updateTypingMessage(
                            id,
                            event.message,
                            currentUserId,
                            _chatRoomState.value.initiatorId,
                            _chatRoomState.value.recipientId,
                            event.isDeleteImage
                        )
                    }
                }
            }
            ChatRoomEvent.DeleteMessages -> {
                deleteMessages()
            }
            ChatRoomEvent.OnOtherUserMessageReceived -> {
                chatRoomId?.let { id ->
                    _chatRoomState.value.otherUserMessage.let {
                        appRepository.updateReadMessages(
                            id,
                            it.messageText,
                            currentUserId, _chatRoomState.value.initiatorId, _chatRoomState.value.recipientId
                        )
                    }
                }
            }

            is ChatRoomEvent.OnCopyRoomLinkClicked -> {
                val url = "https://vanishtest.netlify.app/${event.chatRoomId}"
                copyToClipboard(url, event.context)
            }

            is ChatRoomEvent.OnLaunchCamera -> {
                if (!chatRoomState.value.isCameraVisible) {
                    _chatRoomState.value = _chatRoomState.value.copy(isCameraVisible = true)
                }
            }

            is ChatRoomEvent.OnDismissCamera -> {
                _chatRoomState.value = _chatRoomState.value.copy(isCameraVisible = false)
            }

            is ChatRoomEvent.OnCaptureImage -> {
                _chatRoomState.value = _chatRoomState.value.copy(
                    capturedImage = event.updatedPhoto
                )
            }

            ChatRoomEvent.OnRetakePhoto -> {
                _chatRoomState.value = _chatRoomState.value.copy(capturedImage = null)
            }

            is ChatRoomEvent.OnSendPhoto -> {
                chatRoomId?.let { id ->
                    viewModelScope.launch {
                        appRepository.updateImage(
                            id,
                            event.uri,
                            currentUserId,
                            _chatRoomState.value.initiatorId,
                            _chatRoomState.value.recipientId,
                            event.context
                        )
                    }
                }
                _chatRoomState.value = _chatRoomState.value.copy(
                    isCameraVisible = false,
                    capturedImage = null,
                    currentUserMessage = _chatRoomState.value.currentUserMessage.copy(
                        imageUrls = _chatRoomState.value.currentUserMessage.imageUrls.orEmpty() + Image(
                            url = event.uri.toString(),
                            opened = false
                        )
                    )
                )
            }

            ChatRoomEvent.OnDismissPermissionDialog -> {
                _visiblePermissionDialogQueue.value = _visiblePermissionDialogQueue.value.drop(_visiblePermissionDialogQueue.value.size)
            }

            is ChatRoomEvent.OnPermissionResult -> {
                Log.d("Permissions1", "Permission: ${event.permission}, Granted: ${event.isGranted}")
                if (!event.isGranted && !_visiblePermissionDialogQueue.value.contains(event.permission)) {
                    _visiblePermissionDialogQueue.value += event.permission
                }
            }

            is ChatRoomEvent.OnViewImage -> {
                _chatRoomState.value = _chatRoomState.value.copy(
                    isViewImageClicked = true,
                    selectedViewImage = event.uri
                )
            }

            ChatRoomEvent.OnDismissViewImage -> {
                _chatRoomState.value = _chatRoomState.value.copy(
                    isViewImageClicked = false,
                    selectedViewImage = null
                )
            }

            is ChatRoomEvent.OnImageOpened -> {
                _chatRoomState.value = _chatRoomState.value.copy(
                    otherUserMessage = _chatRoomState.value.otherUserMessage.copy(
                        imageUrls = _chatRoomState.value.otherUserMessage.imageUrls?.map { image ->
                            if (image.url == event.image) image.copy(opened = true) else image
                        }
                    )
                )

                chatRoomId?.let { id ->
                    viewModelScope.launch {
                        appRepository.updateImageStatus(
                            chatRoomId = id,
                            image = event.image,
                            currentUserId = currentUserId,
                            initiatorUserId = _chatRoomState.value.initiatorId,
                            recipientUserId = _chatRoomState.value.recipientId
                        )
                    }
                }
            }
        }
    }

    private fun loadChatRoomDetails(chatRoomId: String) {
        this.chatRoomId = chatRoomId
        viewModelScope.launch {
            val result = appRepository.getChatRoomDetails(chatRoomId, currentUserId)
            result.onSuccess { (userPair, idPair) ->
                val (currentUser, otherUser) = userPair
                val (initiatorId, recipientId) = idPair
                _chatRoomState.value = _chatRoomState.value.copy(
                    currentUserContact = currentUser,
                    otherUserContact = otherUser,
                    initiatorId = initiatorId,
                    recipientId = recipientId
                )
                updatePresence(true)
                otherUser?.let {
                    userRepository.listenForPresenceUpdates(chatRoomId) { activeUsers ->
                        _chatRoomState.value = _chatRoomState.value.copy(
                            isCurrentUserInChatRoom = when (currentUserId) {
                                _chatRoomState.value.initiatorId -> activeUsers["initiator"] == true
                                _chatRoomState.value.recipientId -> activeUsers["recipient"] == true
                                else -> false
                            },
                            isOtherUserInChatRoom = when (otherUser.id) {
                                _chatRoomState.value.initiatorId -> activeUsers["initiator"] == true
                                _chatRoomState.value.recipientId -> activeUsers["recipient"] == true
                                else -> false
                            }
                        )

                        if (!_chatRoomState.value.isCurrentUserInChatRoom || !_chatRoomState.value.isOtherUserInChatRoom) {
                            onEvent(ChatRoomEvent.DeleteMessages)
                        }
                    }
                }
                viewModelScope.launch {
                    appRepository.listenForMessages(
                        chatRoomId,
                        currentUserId,
                        initiatorId,
                        recipientId
                    ).collectLatest { (message, image) ->
                        _chatRoomState.value = _chatRoomState.value.copy(
                            otherUserMessage = _chatRoomState.value.otherUserMessage.copy(
                                messageText = message,
                                imageUrls = image
                            )
                        )
                    }
                }

                viewModelScope.launch {
                    appRepository.listenForCurrentUserImages(
                        chatRoomId,
                        currentUserId,
                        initiatorId,
                        recipientId
                    ).collectLatest { images ->
                        _chatRoomState.value = _chatRoomState.value.copy(
                            currentUserMessage = _chatRoomState.value.currentUserMessage.copy(
                                imageUrls = images
                            )
                        )
                    }
                }

                viewModelScope.launch {
                    appRepository.listenForReadMessages(
                        chatRoomId,
                        currentUserId,
                        initiatorId,
                        recipientId
                    ) { readMessage ->
                        _chatRoomState.value = _chatRoomState.value.copy(
                            otherUserMessage = _chatRoomState.value.otherUserMessage.copy(
                                readMessage = readMessage
                            )
                        )
                    }
                }

            }.onFailure { error ->
                _chatRoomState.value = ChatRoomState(error = error.message)
            }
            try {
                val localContacts = localRepository.getContacts()
                val contacts = localContacts.map { it.toContact() }
                _chatRoomState.value = _chatRoomState.value.copy(
                    contacts = contacts
                )
            } catch (e: Exception) {
                Log.e("MeetingsViewModel", "Error loading contacts", e)
            }
        }
    }

    private fun updatePresence(isPresent: Boolean) {
        chatRoomId?.let { id ->
            viewModelScope.launch {
                userRepository.setUserPresence(
                    id,
                    currentUserId,
                    isPresent,
                    _chatRoomState.value.initiatorId,
                    _chatRoomState.value.recipientId
                )
            }
        }
        Log.d("Presence", isPresent.toString())
        Log.d("Presence", currentUserId)
    }

    private fun setupAppLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> updatePresence(true)
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> {updatePresence(false)}
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    private fun deleteMessages() {
        _chatRoomState.value = _chatRoomState.value.copy(
            currentUserMessage = Message(),
            otherUserMessage = Message()
        )
        chatRoomId?.let { id ->
            viewModelScope.launch {
                appRepository.deleteMessages(id)
            }
        }
    }

    private fun copyToClipboard(url: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("chatRoomLink", url)
        clipboard.setPrimaryClip(clip)
    }
}

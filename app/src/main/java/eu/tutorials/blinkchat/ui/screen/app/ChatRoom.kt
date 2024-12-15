package eu.tutorials.blinkchat.ui.screen.app

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.tutorials.blinkchat.data.event.ChatRoomEvent
import eu.tutorials.blinkchat.ui.component.chatroom.CameraScreen
import eu.tutorials.blinkchat.ui.component.chatroom.ChatBottomBar
import eu.tutorials.blinkchat.ui.component.chatroom.ChatInput
import eu.tutorials.blinkchat.ui.component.chatroom.ChatRoomTopBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.viewmodel.ChatRoomViewModel

@Composable
fun ChatRoom(
    chatRoomId: String,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnLoadChatRoomDetails(chatRoomId, context))
        chatRoomViewModel.onEvent(ChatRoomEvent.OnSetupAppLifecycleObserver(lifecycleOwner))
    }

    LaunchedEffect(key1 = chatRoomState.otherUserMessage) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnOtherUserMessageReceived)
    }

    if (chatRoomState.isCameraVisible) {
        CameraScreen(
            onPhotoCaptured = { bitmap ->
                chatRoomViewModel.onEvent(ChatRoomEvent.OnCaptureImage(bitmap))
            },
            lastCapturedPhoto = chatRoomState.capturedImage,
            onRetakePhoto = {
                chatRoomViewModel.onEvent(ChatRoomEvent.OnRetakePhoto)
            },
            onAccessMedia = {
                chatRoomViewModel.onEvent(ChatRoomEvent.OnAccessMedia)
            },
            onSendPhoto = { bitmap ->
                //chatRoomViewModel.onEvent(ChatRoomEvent.OnSendPhoto(bitmap))
            }
        )
    } else {
        Scaffold(
            topBar = {
                val otherUserId = chatRoomState.otherUserContact?.id
                val associatedContact = chatRoomState.contacts.find { it.id == otherUserId }

                if (associatedContact != null) {
                    ChatRoomTopBar(associatedContact, chatRoomState.isOtherUserInChatRoom)
                } else {
                    Text(text = "Loading...")
                }
            },
            containerColor = BackgroundColor,
            bottomBar = {
                activity?.let {
                    ChatBottomBar(
                        onRoomLinkClicked = {
                            chatRoomViewModel.onEvent(
                                ChatRoomEvent.OnCopyRoomLinkClicked(
                                    chatRoomId,
                                    context
                                )
                            )
                        },
                        onEvent = chatRoomViewModel::onEvent,
                        chatRoomState,
                        context,
                        activity,
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            Text(text = chatRoomState.otherUserMessage)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )

                    ChatInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        chatRoomState = chatRoomState,
                        onMessageTyping = { newText ->
                            chatRoomViewModel.onEvent(ChatRoomEvent.OnMessageTyping(newText))
                        }
                    )
                }
            }
        }
    }
}


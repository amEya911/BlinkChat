package eu.tutorials.blinkchat.ui.screen.app

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.data.event.app.ChatRoomEvent
import eu.tutorials.blinkchat.ui.component.chatroom.CameraScreen
import eu.tutorials.blinkchat.ui.component.chatroom.ChatBottomBar
import eu.tutorials.blinkchat.ui.component.chatroom.ChatInput
import eu.tutorials.blinkchat.ui.component.chatroom.ChatRoomTopBar
import eu.tutorials.blinkchat.ui.component.chatroom.FullscreenImageViewer
import eu.tutorials.blinkchat.ui.viewmodel.app.ChatRoomViewModel

@Composable
fun ChatRoom(
    id: String? = null,
    chatRoomId: String,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value
    val lifecycleOwner = LocalLifecycleOwner.current

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MaterialTheme.colorScheme.secondaryContainer)

    LaunchedEffect(Unit) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnLoadChatRoomDetails(context, id, chatRoomId))
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
            onSendPhoto = { bitmap ->
                chatRoomViewModel.onEvent(ChatRoomEvent.OnSendPhoto(bitmap, context))
            },
            onBack = {
                chatRoomViewModel.onEvent(ChatRoomEvent.OnRetakePhoto)
                chatRoomViewModel.onEvent(ChatRoomEvent.OnDismissCamera)
            }
        )
    } else if (chatRoomState.isViewImageClicked) {
        chatRoomState.selectedViewImage?.let { selectedImageUri ->
            FullscreenImageViewer(
                imageUrl = selectedImageUri.toString(),
                onClose = { chatRoomViewModel.onEvent(ChatRoomEvent.OnDismissViewImage)}
            )
        }
    } else {
        Scaffold(
            topBar = {
                val otherUserId = chatRoomState.otherUserContact?.id
                val associatedContact = chatRoomState.contacts.find { it.id == otherUserId }

                ChatRoomTopBar(
                    associatedContact?.displayName,
                    associatedContact?.photoUri,
                    chatRoomState.isOtherUserInChatRoom
                )

            },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                activity?.let {
                    ChatBottomBar(
                        onRoomLinkClicked = {
                            chatRoomViewModel.onEvent(
                                ChatRoomEvent.OnCopyRoomLinkClicked(
                                    isGuest = (id != null),
                                    chatRoomId,
                                    context
                                )
                            )
                        },
                        onEvent = chatRoomViewModel::onEvent,
                        chatRoomState,
                        context,
                        activity,
                        chatRoomViewModel
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            Column {
                                val imageUrls = chatRoomState.otherUserMessage.imageUrls
                                Log.d(
                                    "imageUrls",
                                    chatRoomState.otherUserMessage.imageUrls.toString()
                                )
                                if (!imageUrls.isNullOrEmpty()) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                    ) {
                                        items(imageUrls) { image ->

                                            val imageUrl = image.url
                                            val isOpened = image.opened

                                            val uri = try {
                                                Uri.parse(imageUrl)
                                            } catch (e: Exception) {
                                                null
                                                //TODO
                                            }

                                            if (uri != null) {
                                                Button(
                                                    onClick = {
                                                        chatRoomViewModel.onEvent(
                                                            ChatRoomEvent.OnViewImage(uri)
                                                        )
                                                        if (!isOpened) {
                                                            chatRoomViewModel.onEvent(ChatRoomEvent.OnImageOpened(imageUrl))
                                                        }
                                                    },
                                                    modifier = Modifier.padding(4.dp)
                                                ) {
                                                    if (isOpened) {
                                                        Text(text = "Opened")
                                                    } else {
                                                        Text(text = "Photo")
                                                    }
                                                }
                                            } else {
                                                Text(text = "Invalid image URL")
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = chatRoomState.otherUserMessage.messageText,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ChatInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        chatRoomState = chatRoomState,
                        onMessageTyping = { newText ->
                            chatRoomViewModel.onEvent(
                                ChatRoomEvent.OnMessageTyping(
                                    newText,
                                    false
                                )
                            )
                        },
                        onViewImage = { uri ->
                            chatRoomViewModel.onEvent(ChatRoomEvent.OnViewImage(uri))
                        },
                        onDismissImage = { chatRoomViewModel.onEvent(ChatRoomEvent.OnDismissViewImage) }
                    )
                }
            }
        }
    }
}
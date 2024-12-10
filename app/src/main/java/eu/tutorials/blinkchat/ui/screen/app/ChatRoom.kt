package eu.tutorials.blinkchat.ui.screen.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.tutorials.blinkchat.data.event.ChatRoomEvent
import eu.tutorials.blinkchat.ui.component.chatroom.ChatInputBar
import eu.tutorials.blinkchat.ui.component.chatroom.ChatRoomTopBar
import eu.tutorials.blinkchat.ui.component.inbox.MenuItem
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.viewmodel.ChatRoomViewModel

@Composable
fun ChatRoom(
    chatRoomId: String,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    Log.d("ChatRoom", "Launching ChatRoom with chatRoomId: $chatRoomId")
    val context = LocalContext.current
    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnLoadChatRoomDetails(chatRoomId))
        chatRoomViewModel.onEvent(ChatRoomEvent.OnSetupAppLifecycleObserver(lifecycleOwner))
    }

    LaunchedEffect(key1 = chatRoomState.otherUserMessage) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnOtherUserMessageReceived)
    }

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
        bottomBar = { ChatInputBar(onEvent = chatRoomViewModel::onEvent, chatRoomState) },
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

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color.Gray,
                    thickness = 1.dp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        if (!chatRoomState.isOtherUserInChatRoom || !chatRoomState.isCurrentUserInChatRoom) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "To start typing, wait for other user to enter the chat room",
                                    color = Color.Red,
                                    fontSize = 18.sp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                BasicTextField(
                                    value = chatRoomState.currentUserMessage,
                                    onValueChange = { newText ->
                                        chatRoomViewModel.onEvent(ChatRoomEvent.OnMessageTyping(newText))
                                    },
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    textStyle = TextStyle(
                                        color = Color.Transparent,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Default
                                    ),
                                    cursorBrush = SolidColor(Color.White)
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        val readMessage = chatRoomState.readMessage
                                        val currentMessage = chatRoomState.currentUserMessage

                                        if (readMessage != null && currentMessage.startsWith(readMessage)) {
                                            withStyle(style = SpanStyle(color = Color.Green)) {
                                                append(readMessage)
                                            }
                                            withStyle(style = SpanStyle(color = Color.Yellow)) {
                                                append(currentMessage.removePrefix(readMessage))
                                            }
                                        } else {
                                            withStyle(style = SpanStyle(color = Color.Yellow)) {
                                                append(currentMessage)
                                            }
                                        }
                                    },
                                    style = TextStyle(
                                        color = Color.Transparent,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Default
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (chatRoomState.isAddButtonClicked) {
                AddButtonClicked(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    onRoomLinkClicked = { chatRoomViewModel.onEvent(ChatRoomEvent.OnCopyRoomLinkClicked(chatRoomId, context)) }
                )
            }
        }
    }
}

@Composable
fun AddButtonClicked(
    modifier: Modifier = Modifier,
    onRoomLinkClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor =  MaterialTheme.colorScheme.secondary.copy(
                alpha = 0.5f
            )
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                text = "Copy Room Link",
                icon = Icons.Default.CopyAll,
                onClick = onRoomLinkClicked
            )
        }
    }
}

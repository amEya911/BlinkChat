package eu.tutorials.blinkchat.ui.screen.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.ChatRoomEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.ChatRoomState
import eu.tutorials.blinkchat.ui.component.ChatRoomTopBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.LightGray
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
import eu.tutorials.blinkchat.ui.viewmodel.ChatRoomViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ChatRoom(
    chatRoomId: String,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    Log.d("ChatRoom", "Launching ChatRoom with chatRoomId: $chatRoomId")
    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        chatRoomViewModel.onEvent(ChatRoomEvent.OnLoadChatRoomDetails(chatRoomId))
        chatRoomViewModel.onEvent(ChatRoomEvent.OnSetupAppLifecycleObserver(lifecycleOwner))
    }

    Scaffold(
        topBar = {
            chatRoomState.otherUserContact?.let {
                ChatRoomTopBar(it, chatRoomState.isOtherUserInChatRoom)
            } ?: Text(text = "Loading...")
        },
        containerColor = BackgroundColor,
        bottomBar = { ChatInputBar(chatRoomViewModel, chatRoomState) },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    //Text(text = "Other User: ${chatRoomState.isOtherUserInChatRoom}")
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
                    //Text(text = "Current User: ${chatRoomState.isCurrentUserInChatRoom}")
                    Text(text = chatRoomState.currentUserMessage)
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(viewModel: ChatRoomViewModel, chatRoomState: ChatRoomState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!chatRoomState.isOtherUserInChatRoom || !chatRoomState.isCurrentUserInChatRoom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for the other user to join the chat room.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }
            TextField(
                value = chatRoomState.currentUserMessage,
                onValueChange = { newText ->
                    viewModel.onEvent(ChatRoomEvent.OnMessageTyping(newText))
                },
                placeholder = { Text(text = "Type a message", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldColor,
                    unfocusedContainerColor = TextFieldColor,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(id = R.drawable.eraser),
                    contentDescription = "Erase",
                    tint = Color.Black
                )
            }
        }
    }
}

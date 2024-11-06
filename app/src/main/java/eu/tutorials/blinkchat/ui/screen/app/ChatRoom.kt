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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.R
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
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = LightGray)

    Log.d("ChatRoom", "Launching ChatRoom with chatRoomId: $chatRoomId")
    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value

    LaunchedEffect(chatRoomId) { chatRoomViewModel.loadChatRoomDetails(chatRoomId) }

    Scaffold(
        topBar = {
            chatRoomState.otherUserContact?.let {
                ChatRoomTopBar(it)
            } ?: Text(text = "Loading...")
        },
        containerColor = BackgroundColor,
        bottomBar = { ChatInputBar() },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                chatRoomState.currentUserContact?.let {
                    repeat(15) {  // Adjust number as needed
                        Text(text = "Current User Id: ${chatRoomState.currentUserContact.id}")
                        Text(text = "Other User Id: ${chatRoomState.otherUserContact?.id}")
                    }
                } ?: Text(text = "Error: ${chatRoomState.error ?: "Unknown error"}")
            }
        }
    }
}

@Composable
fun ChatInputBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.CenterVertically)
            .background(LightGray)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
        }
        TextField(
            value = "",
            onValueChange = { },
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
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Black)
        }
        IconButton(onClick = { }) {
            Icon(painter = painterResource(id = R.drawable.eraser), contentDescription = "Send", tint = Color.Black)
        }
    }
}

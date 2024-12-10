package eu.tutorials.blinkchat.ui.component.chatroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.ChatRoomEvent
import eu.tutorials.blinkchat.data.state.ChatRoomState
import eu.tutorials.blinkchat.ui.theme.LightGray

@Composable
fun ChatInputBar(
    onEvent: (ChatRoomEvent) -> Unit,
    chatRoomState: ChatRoomState
) {
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
            IconButton(onClick = { onEvent(ChatRoomEvent.OnAddButtonClicked) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onEvent(ChatRoomEvent.OnMessageTyping(""))
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.eraser),
                    contentDescription = "Erase",
                    tint = Color.Black
                )
            }
        }
    }
}
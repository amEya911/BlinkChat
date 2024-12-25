package eu.tutorials.blinkchat.ui.component.chatroom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.data.state.app.ChatRoomState

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    chatRoomState: ChatRoomState,
    onMessageTyping: (String) -> Unit
) {
    if (!chatRoomState.isOtherUserInChatRoom || !chatRoomState.isCurrentUserInChatRoom) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "To start typing, wait for other user to enter the chat room",
                color = Color.Red,
                fontSize = 18.sp
            )
        }
    } else {
        Box(modifier = modifier) {
            BasicTextField(
                value = chatRoomState.currentUserMessage.messageText,
                onValueChange = onMessageTyping,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = Color.Transparent,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default
                ),
                cursorBrush = SolidColor(Color.White)
            )
            Text(
                text = buildAnnotatedString {
                    val readMessage = chatRoomState.otherUserMessage.readMessage
                    val currentMessage = chatRoomState.currentUserMessage.messageText

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
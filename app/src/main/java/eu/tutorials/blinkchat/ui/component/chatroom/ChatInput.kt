package eu.tutorials.blinkchat.ui.component.chatroom

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.data.event.app.ChatRoomEvent
import eu.tutorials.blinkchat.data.state.app.ChatRoomState

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    chatRoomState: ChatRoomState,
    onMessageTyping: (String) -> Unit,
    onViewImage: (Uri) -> Unit,
    onDismissImage: () -> Unit
) {
    Log.d("Anish", "hi: ${chatRoomState.currentUserMessage.imageUrls}")
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
        Column(modifier = modifier) {
            val imageUrls = chatRoomState.currentUserMessage.imageUrls
            if (!imageUrls.isNullOrEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(imageUrls) { image ->

                        Log.d("Anish", "image: $image")

                        val imageUrl = image.url
                        val isOpened = image.opened

                        val uri = try {
                            Uri.parse(imageUrl)  // Try to parse the String URL into Uri
                        } catch (e: Exception) {
                            null  // Handle invalid URLs gracefully
                        }

                        Log.d("Anish", "uri: $uri")

                        if (uri != null) {
                            Button(
                                onClick = {
                                    // Handle the event when the image is clicked
                                    onViewImage(Uri.parse(imageUrl))
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
}
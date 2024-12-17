package eu.tutorials.blinkchat.ui.screen.guest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.GuestEvent
import eu.tutorials.blinkchat.ui.viewmodel.GuestViewModel

@Composable
fun Guest(
    onButtonClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    guestViewModel: GuestViewModel = hiltViewModel()
) {
    val state = guestViewModel.guestState.collectAsState().value
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                if (state.roomLink.isEmpty()) {
                    Button(
                        onClick = {
                            guestViewModel.onEvent(GuestEvent.OnCreateRoom(context))
                        }
                    ) {
                        Text(text = "Create a Room")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.roomLink.isNotEmpty()) {
                    Text(text = "Room Link: ")
                    CopyableText(
                        label = state.roomLink,
                        context = context
                    )
                    Button(
                        onClick = {
                            val chatRoomId = state.chatRoomId
                            onButtonClick(chatRoomId, state.initiatorId)
                        }
                    ) {
                        Text(text = "Get in")
                    }
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CopyableText(label: String, context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Text(
        text = label,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable {
                val clip = ClipData.newPlainText("Room Link", label)
                clipboard.setPrimaryClip(clip)
            }
            .padding(8.dp)
    )
}

package eu.tutorials.blinkchat.ui.screen.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.auth.GuestEvent
import eu.tutorials.blinkchat.ui.viewmodel.auth.GuestViewModel
import kotlinx.coroutines.launch

@Composable
fun Guest(
    onButtonClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    guestViewModel: GuestViewModel = hiltViewModel()
) {
    val state = guestViewModel.guestState.collectAsState().value
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (state.roomLink.isEmpty()) {
                        Button(
                            onClick = {
                                guestViewModel.onEvent(GuestEvent.OnCreateRoom(context))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Create a Room",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.roomLink.isNotEmpty()) {
                        Text(
                            text = "Room Link:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CopyableText(
                            label = state.roomLink,
                            context = context,
                            snackbarHostState = snackbarHostState
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val chatRoomId = state.chatRoomId
                                onButtonClick(chatRoomId, state.initiatorId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Join Room",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CopyableText(
    label: String,
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge.copy(
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable {
                val clip = ClipData.newPlainText("Room Link", label)
                clipboard.setPrimaryClip(clip)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Copied to Clipboard!")
                }
            }
            .padding(8.dp)
    )
}


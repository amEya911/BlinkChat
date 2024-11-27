package eu.tutorials.blinkchat.ui.screen.app

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.state.InboxState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.inbox.ChatItem
import eu.tutorials.blinkchat.ui.component.inbox.InboxContactPress
import eu.tutorials.blinkchat.ui.component.inbox.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor

@Composable
fun Inbox(
    inboxState: InboxState,
    onEvent: (InboxEvent) -> Unit,
    modifier: Modifier = Modifier,
    onStartChatWithContact: (String) -> Unit
) {
    val activity = LocalContext.current as? Activity
    var searchQuery = inboxState.searchQuery

    LaunchedEffect(key1 = true) {
        onEvent(InboxEvent.LoadRecentChats)
    }

    LaunchedEffect(inboxState.contacts) {
        if (inboxState.contacts.isEmpty()) {
            activity?.let { onEvent(InboxEvent.LoadContacts(it)) }
        }
    }

    LaunchedEffect(key1 = inboxState.isEnterChatRoom) {
        if (inboxState.isEnterChatRoom) {
            onStartChatWithContact(inboxState.navigateToChatId!!)
            onEvent(InboxEvent.ResetEnterChatRoom)
        }
    }

    val contentModifier = if (inboxState.isContactClicked || inboxState.isScheduleAMeetClicked) Modifier.blur(20.dp) else Modifier

    Scaffold(
        topBar = {
            AppBar(
                title = "Chats",
                showIcon = true,
                onIconClick = { onEvent(InboxEvent.OnAllContactsIconClicked) },
                iconResId = Icons.Default.AccountCircle
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(contentModifier)
        ) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                TextField(
                    value = searchQuery ?: "",
                    onValueChange = {
                        searchQuery = it
                        onEvent(InboxEvent.SearchUsers(it))
                    },
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp)),
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

                val displayedContacts =
                    if (inboxState.searchQuery.isNullOrBlank()) inboxState.contacts else inboxState.searchResults

                if (inboxState.isAllContactsClicked || !inboxState.searchQuery.isNullOrBlank()) {
                    LazyColumn {
                        items(displayedContacts.sortedBy { it.displayName }) { contact ->
                            ChatItem(inboxState = inboxState, contact = contact, onEvent = onEvent)
                        }
                    }
                } else {
                    LazyColumn {
                        items(inboxState.recentContacts.reversed()) { contact ->
                            ChatItem(inboxState = inboxState, contact = contact, onEvent = onEvent)
                        }
                    }
                }
            }
        }
        if (inboxState.isContactClicked) {
            InboxContactPress(
                state = inboxState,
                onEvent = onEvent
            ) { contact ->
                ChatItem(
                    inboxState = inboxState,
                    contact = contact,
                    onEvent = {}
                )
            }
        }
        if (inboxState.isScheduleAMeetClicked) {
            ScheduleMeetDialog(
                onEvent = onEvent
            )
        }
    }
}
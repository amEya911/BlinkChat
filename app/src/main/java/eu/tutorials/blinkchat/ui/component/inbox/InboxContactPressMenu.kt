package eu.tutorials.blinkchat.ui.component.inbox

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.InboxState

@Composable
fun InboxContactPress(
    state: InboxState,
    onEvent: (InboxEvent) -> Unit,
    contactComposable: @Composable (contact: Contact) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isContactPressed = state.selectedContact != null

    AnimatedVisibility(
        visible = isContactPressed,
        enter = fadeIn(animationSpec = tween(25)) + expandHorizontally(animationSpec = spring())
    ) {
        state.selectedContact?.let { contact ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = { onEvent(InboxEvent.OnContactDismissed) },
                        interactionSource = interactionSource,
                        indication = null
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        contactComposable(contact)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ContactPressMenu(
                        inboxState = state,
                        isRecentChat = !state.isAllContactsClicked,
                        onEnterClick = {
                            onEvent(InboxEvent.OnEnterChatRoom)
                            onEvent(InboxEvent.OnContactDismissed)
                        },
                        onCancelClick = { onEvent(InboxEvent.OnContactDismissed) },
                        onScheduleAMeet = {
                            onEvent(InboxEvent.OnScheduleAMeetClick)
                            onEvent(InboxEvent.OnContactDismissed)
                        },
                        onDeleteRecentChat = {
                            if (!state.isAllContactsClicked) {
                                val recentChatContact = state.recentContacts.find {
                                    it.contact.id == contact.id
                                }
                                if (recentChatContact != null) {
                                    onEvent(InboxEvent.OnDeleteRecentChat(recentChatContact))
                                } else {
                                    Log.w("ContactPressMenu", "No matching recent chat found for contact ID: ${contact.id}")
                                }
                            }
                        },
                        onBlockUser = { onEvent(InboxEvent.OnBlockUser(state.selectedContact.id)) },
                        onUnblockUser = { onEvent(InboxEvent.OnUnblockUser(state.selectedContact.id))}
                    )
                }
            }
        }
    }
}

@Composable
fun ContactPressMenu(
    inboxState: InboxState,
    isRecentChat: Boolean,
    onEnterClick: () -> Unit,
    onCancelClick: () -> Unit,
    onScheduleAMeet: () -> Unit,
    onDeleteRecentChat: () -> Unit,
    onBlockUser: () -> Unit,
    onUnblockUser: () -> Unit
) {
    Card(
        modifier = Modifier
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
            if (!inboxState.isSelectedContactBlocked) {
                MenuItem(
                    text = "Enter",
                    icon = Icons.Default.Chat,
                    onClick = onEnterClick
                )
            }

            MenuDivider()

            if (!inboxState.isSelectedContactBlocked) {
                MenuItem(
                    text = "Schedule a Meet",
                    icon = Icons.Default.Schedule,
                    onClick = onScheduleAMeet
                )
            }

            MenuDivider()

            if (isRecentChat) {
                MenuItem(
                    text = "Delete Recent Chat",
                    icon = Icons.Default.Delete,
                    onClick = onDeleteRecentChat
                )
                MenuDivider()
            }

            MenuItem(
                text = if (!inboxState.isSelectedContactBlocked) "Block ${inboxState.selectedContact?.displayName}" else "Unblock ${inboxState.selectedContact?.displayName}",
                icon = Icons.Default.Block,
                onClick = if (!inboxState.isSelectedContactBlocked) onBlockUser else onUnblockUser
            )

            MenuDivider()

            MenuItem(
                text = "Cancel",
                icon = Icons.Default.Clear,
                onClick = onCancelClick
            )
        }
    }
}

@Composable
fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondary,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(
        thickness = 0.3.dp,
        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)
    )
}
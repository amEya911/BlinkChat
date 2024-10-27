package eu.tutorials.blinkchat.ui.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.ContactModel
import eu.tutorials.blinkchat.data.state.InboxState
import eu.tutorials.blinkchat.ui.theme.TextFieldColor

@Composable
fun Inbox(
    inboxState: InboxState,
    onEvent: (InboxEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as? Activity
    var searchQuery = inboxState.searchQuery

    LaunchedEffect(inboxState.contacts) {
        if (inboxState.contacts.isEmpty()) {
            activity?.let { onEvent(InboxEvent.LoadContacts(it)) }
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
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
                    ChatItem(contact = contact)
                }
            }
        }

    }
}

@Composable
fun ChatItem(contact: ContactModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (contact.photoUri != null) {
            AsyncImage(
                model = contact.photoUri,
                contentDescription = "photoUri",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile_image),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = contact.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.4f)
    )
}


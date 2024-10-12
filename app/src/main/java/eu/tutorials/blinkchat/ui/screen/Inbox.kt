package eu.tutorials.blinkchat.ui.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.ContactModel
import eu.tutorials.blinkchat.R

@Composable
fun Inbox(
    viewModel: InboxViewModel = hiltViewModel(),
    onStartChatWithContact: (ContactModel) -> Unit
) {
    val inboxState by viewModel.inboxState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (activity != null) {
            viewModel.onEvent(InboxEvent.LoadContacts(activity))
        }
    }

    if (inboxState.isContactClicked) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(InboxEvent.OnContactDismiss)
            },
            title = {
                Text(text = "Confirmation")
            },
            text = {
                Text(text = "Do you want to get in a chat room with ${inboxState.selectedContact?.displayName}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(InboxEvent.StartChatWithContact(inboxState.selectedContact!!))
                    onStartChatWithContact(inboxState.selectedContact!!)
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onEvent(InboxEvent.OnContactDismiss)
                }) {
                    Text("No")
                }
            }
        )
    }


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Contacts",
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 24.sp
            )

            // Search TextField
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onEvent(
                        InboxEvent.SearchUsers(it)
                    )
                },
                placeholder = { Text(text = "Search") },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(40.dp)),
                singleLine = true,
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color.LightGray,
                    unfocusedContainerColor = Color.LightGray,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray)
                }
            )

            val displayedContacts = if (inboxState.searchQuery.isNullOrBlank()) {
                inboxState.contacts
            } else {
                inboxState.searchResults
            }

            LazyColumn(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxSize()
            ) {
                items(displayedContacts.sortedBy { it.displayName }) { contact ->
                    Contact(
                        contact,
                        onClick = {
                            viewModel.onEvent(InboxEvent.OnContactClicked(contact))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Contact(
    contact: ContactModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(125.dp)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!contact.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = "photo uri",
                    modifier = Modifier
                        .size(75.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_image),
                    contentDescription = "default image",
                    modifier = Modifier
                        .size(75.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = contact.displayName, fontSize = 20.sp)
                Text(text = contact.phoneNumber)
            }
        }
    }
}

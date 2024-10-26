package eu.tutorials.blinkchat.ui.screen

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(viewModel: InboxViewModel = hiltViewModel()) {
    val inboxState = viewModel.inboxState.collectAsState().value
    val activity = LocalContext.current as? Activity
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        activity?.let { viewModel.onEvent(InboxEvent.LoadContacts(it)) }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Text("Chats", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.onEvent(InboxEvent.OnAllContactsIconClicked) }) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(30.dp))
                        }
                    }
                },
                expandedHeight = 100.dp,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onEvent(InboxEvent.SearchUsers(it))
                },
                placeholder = { Text("Search", fontSize = 14.sp, color = Color.Black.copy(alpha = 0.4f)) },
                singleLine = true,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
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

            val displayedContacts = if (inboxState.searchQuery.isNullOrBlank()) inboxState.contacts else inboxState.searchResults

            if (inboxState.isAllContactsClicked || !inboxState.searchQuery.isNullOrBlank()) {
                LazyColumn {
                    items(displayedContacts.sortedBy { it.displayName }) { contact ->
                        Text(contact.displayName, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

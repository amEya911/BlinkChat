package eu.tutorials.blinkchat.ui.screen.additional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.app.ProfileEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.additional.ProfileState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.UserDisplay
import eu.tutorials.blinkchat.ui.component.inbox.MenuDivider
import eu.tutorials.blinkchat.ui.component.rememberInternetConnectionState

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    profileState: ProfileState,
    currentUserId: String,
    onEvent: (ProfileEvent) -> Unit,
    onBackClicked: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onEvent(ProfileEvent.OnLoadCurrentContact(id = currentUserId))
    }

    var nameState by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(profileState.currentUserContact.displayName) {
        nameState = TextFieldValue(profileState.currentUserContact.displayName)
    }

    Scaffold(
        topBar = {
            AppBar(
                title = if (profileState.isNameClicked) "Edit Name" else "Profile",
                navigationIcon = Icons.Default.ArrowBackIosNew,
                onNavigationIconClicked = {
                    if (profileState.isNameClicked) onEvent(ProfileEvent.OnNameClicked) else onBackClicked()
                },
                isOnline = rememberInternetConnectionState()
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (profileState.isNameClicked) {
                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Edit Name") },
                        trailingIcon = {
                            IconButton(onClick = {
                                onEvent(
                                    ProfileEvent.OnNameConfirmed(
                                        id = profileState.currentUserContact.id,
                                        newName = nameState.text
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    UserDisplay(photoUri = profileState.currentUserContact.photoUri, size = 80.dp)

                    Spacer(modifier = Modifier.height(48.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent(ProfileEvent.OnNameClicked) },
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = AnnotatedString(profileState.currentUserContact.displayName),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = profileState.currentUserContact.phoneNumber,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}
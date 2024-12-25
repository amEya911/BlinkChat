package eu.tutorials.blinkchat.ui.screen.additional

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.additional.BlockedUsersEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.additional.BlockedUsersState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.BlockUnblockConfirmation
import eu.tutorials.blinkchat.ui.component.CustomTextField
import eu.tutorials.blinkchat.ui.component.UserItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsers(
    modifier: Modifier = Modifier,
    blockedUsers: List<Contact> = emptyList(),
    onBackClicked: () -> Unit,
    onUnblockClicked: (String) -> Unit,
    onAddBlockUsers: () -> Unit,
    blockedUsersState: BlockedUsersState,
    onEvent: (BlockedUsersEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        onEvent(BlockedUsersEvent.OnLoadContacts(blockedUsers))
    }
    if (blockedUsersState.isBottomSheetShow) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onEvent(BlockedUsersEvent.OnDismissBottomSheet) }
        ) {
            blockedUsersState.selectedContact?.let {
                BlockUnblockConfirmation(
                    isBlock = false,
                    user = blockedUsersState.selectedContact,
                    onConfirm = {
                        onUnblockClicked(blockedUsersState.selectedContact.id)
                        onEvent(BlockedUsersEvent.OnReset)
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onEvent(BlockedUsersEvent.OnDismissBottomSheet)
                            }
                        }
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Blocked Accounts",
                iconResId = Icons.Default.Add,
                onIconClick = { onAddBlockUsers() },
                navigationIcon = Icons.Default.ArrowBackIosNew,
                onNavigationIconClicked = onBackClicked
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
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                CustomTextField(
                    value = blockedUsersState.searchQuery ?: "",
                    onValueChange = { query ->
                        onEvent(BlockedUsersEvent.OnSearchUsers(query))
                    },
                    placeholderText = "Search",
                    modifier = Modifier.padding(16.dp)
                )

                val displayedContacts = if (blockedUsersState.searchQuery.isNullOrBlank()) {
                    blockedUsersState.contacts
                } else {
                    blockedUsersState.searchResults
                }

                LazyColumn {
                    items(displayedContacts) { user ->
                        UserItem(
                            user = user,
                            buttonName = "Unblock",
                            onClick = {
                                keyboardController?.hide()
                                onEvent(BlockedUsersEvent.OnContactClicked(user))
                                //onUnblockClicked(user.id)
                                //onEvent(BlockedUsersEvent.OnReset)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
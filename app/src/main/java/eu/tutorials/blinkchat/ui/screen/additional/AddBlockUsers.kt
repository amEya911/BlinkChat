package eu.tutorials.blinkchat.ui.screen.additional

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.additional.AddBlockUsersEvent
import eu.tutorials.blinkchat.data.event.additional.BlockedUsersEvent
import eu.tutorials.blinkchat.data.event.additional.ScheduleAMeetEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.additional.AddBlockUsersState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.BlockUnblockConfirmation
import eu.tutorials.blinkchat.ui.component.CustomTextField
import eu.tutorials.blinkchat.ui.component.UserItem
import eu.tutorials.blinkchat.ui.component.rememberInternetConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlockUsers(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    onBackClicked: () -> Unit,
    onBlockUser: (String) -> Unit,
    addBlockUsersState: AddBlockUsersState,
    onEvent: (AddBlockUsersEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        onEvent(AddBlockUsersEvent.OnLoadContacts(contacts))
    }

    if (addBlockUsersState.isBottomSheetShow) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onEvent(AddBlockUsersEvent.OnDismissBottomSheet) }
        ) {
            addBlockUsersState.selectedContact?.let {
                BlockUnblockConfirmation(
                    isBlock = true,
                    user = addBlockUsersState.selectedContact,
                    onConfirm = {
                        onBlockUser(addBlockUsersState.selectedContact.id)
                        onEvent(AddBlockUsersEvent.OnReset)
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onEvent(AddBlockUsersEvent.OnDismissBottomSheet)
                            }
                        }
                    }
                )
            }
        }
    }

    PullToRefreshBox(
        state = refreshState,
        isRefreshing = addBlockUsersState.isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                onEvent(AddBlockUsersEvent.OnStartRefresh)
                delay(15)
                onEvent(AddBlockUsersEvent.OnEndRefresh)
            }
        },
        indicator = {}
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    title = "Block Users",
                    navigationIcon = Icons.Default.ArrowBackIosNew,
                    onNavigationIconClicked = onBackClicked,
                    isOnline = rememberInternetConnectionState()
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = refreshState.distanceFraction * 300f
                },
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
                        value = addBlockUsersState.searchQuery ?: "",
                        onValueChange = { query ->
                            onEvent(AddBlockUsersEvent.OnSearchUsers(query))
                        },
                        placeholderText = "Search",
                        modifier = Modifier.padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )

                    val displayedContacts = if (addBlockUsersState.searchQuery.isNullOrBlank()) {
                        addBlockUsersState.contacts
                    } else {
                        addBlockUsersState.searchResults
                    }

                    LazyColumn {
                        items(displayedContacts.sortedBy { it.displayName }) { user ->
                            UserItem(
                                user = user,
                                buttonName = "Block",
                                onClick = {
                                    keyboardController?.hide()
                                    onEvent(AddBlockUsersEvent.OnContactClicked(user))
                                },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}


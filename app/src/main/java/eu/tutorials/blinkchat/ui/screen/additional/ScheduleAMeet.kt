package eu.tutorials.blinkchat.ui.screen.additional

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.additional.ScheduleAMeetEvent
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.additional.ScheduleAMeetState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.CustomTextField
import eu.tutorials.blinkchat.ui.component.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.component.UserItem
import eu.tutorials.blinkchat.ui.component.rememberInternetConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAMeet(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    onBackClicked: () -> Unit,
    onScheduleConfirmed: (Contact, String, String) -> Unit,
    scheduleAMeetState: ScheduleAMeetState,
    onEvent: (ScheduleAMeetEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        onEvent(ScheduleAMeetEvent.OnLoadContacts(contacts))
    }

    PullToRefreshBox(
        state = refreshState,
        isRefreshing = scheduleAMeetState.isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                onEvent(ScheduleAMeetEvent.OnStartRefresh)
                delay(15)
                onEvent(ScheduleAMeetEvent.OnEndRefresh)
            }
        },
        indicator = {}
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    title = "Schedule A Meet",
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
            containerColor = MaterialTheme.colorScheme.background,
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
                        value = scheduleAMeetState.searchQuery ?: "",
                        onValueChange = { query ->
                            onEvent(ScheduleAMeetEvent.OnSearchUsers(query))
                        },
                        placeholderText = "Search",
                        modifier = Modifier.padding(16.dp)
                    )

                    val displayedContacts = if (scheduleAMeetState.searchQuery.isNullOrBlank()) {
                        scheduleAMeetState.contacts
                    } else {
                        scheduleAMeetState.searchResults
                    }

                    LazyColumn {
                        items(displayedContacts.sortedBy { it.displayName }) { user ->
                            UserItem(
                                user = user,
                                buttonName = "Schedule",
                                onClick = {
                                    keyboardController?.hide()
                                    onEvent(ScheduleAMeetEvent.OnButtonClicked(selectedContact = user))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    if (scheduleAMeetState.showDialog && scheduleAMeetState.selectedContact != null) {
        ScheduleMeetDialog(
            onConfirm = { date, time ->
                onScheduleConfirmed(scheduleAMeetState.selectedContact, date, time)
                onEvent(ScheduleAMeetEvent.OnDismiss)
            },
            onDismiss = {
                onEvent(ScheduleAMeetEvent.OnDismiss)
            }
        )
    }
}


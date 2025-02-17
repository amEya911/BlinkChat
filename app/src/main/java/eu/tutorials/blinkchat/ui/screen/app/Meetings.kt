package eu.tutorials.blinkchat.ui.screen.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.event.app.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.app.MeetingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.CustomTextField
import eu.tutorials.blinkchat.ui.component.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.component.meetings.MeetingContactPress
import eu.tutorials.blinkchat.ui.component.meetings.MeetingItem
import eu.tutorials.blinkchat.ui.component.rememberInternetConnectionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Meetings(
    modifier: Modifier = Modifier,
    meetingsState: MeetingsState,
    onEvent: (MeetingsEvent) -> Unit,
    onAddClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val contentModifier =
        if (meetingsState.isMeetingClicked || meetingsState.isRescheduleClicked) Modifier.blur(20.dp) else Modifier
    val keyboardController = LocalSoftwareKeyboardController.current
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(onBack = {
        onBackPressed()
    })

    PullToRefreshBox(
        state = refreshState,
        isRefreshing = meetingsState.isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                onEvent(MeetingsEvent.OnStartRefresh)
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!(meetingsState.isMeetingClicked || meetingsState.isRescheduleClicked)) {
                    AppBar(
                        title = "Meetings",
                        onIconClick = { onAddClicked() },
                        iconResId = Icons.Default.Add,
                        isOnline = rememberInternetConnectionState()
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .graphicsLayer {
                    translationY = refreshState.distanceFraction * 300f
                }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .then(contentModifier)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CustomTextField(
                        value = meetingsState.searchQuery ?: "",
                        onValueChange = { query ->
                            onEvent(MeetingsEvent.OnSearchUsers(query))
                        },
                        placeholderText = "Search",
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SegmentedButton(
                        isSortByCreatedAt = meetingsState.isSortByCreatedAt,
                        onSortByCreatedAt = { onEvent(MeetingsEvent.OnSortByCreatedAt) },
                        onSortByTime = { onEvent(MeetingsEvent.OnSortByTime) }
                    )


                    Spacer(modifier = Modifier.height(12.dp))

                    val displayedMeetings = if (meetingsState.searchQuery.isNullOrBlank()) {
                        meetingsState.meetings
                    } else {
                        meetingsState.searchResults
                    }

                    val updatedMatchedContacts = displayedMeetings.map { meeting ->
                        val updatedOtherUserContact =
                            meetingsState.contacts.find { it.id == meeting.otherUserContact.id }
                        if (updatedOtherUserContact != null) {
                            meeting.copy(otherUserContact = updatedOtherUserContact)
                        } else {
                            meeting.copy(
                                otherUserContact = meeting.otherUserContact.copy(
                                    displayName = meeting.otherUserContact.phoneNumber
                                )
                            )
                        }
                    }

                    LazyColumn {
                        items(updatedMatchedContacts.reversed()) { meeting ->
                            MeetingItem(meeting, meetingsState, onEvent)
                        }
                    }
                }
            }
            if (meetingsState.isMeetingClicked) {
                keyboardController?.hide()
                MeetingContactPress(
                    meetingsState = meetingsState,
                    onEvent = onEvent
                ) { meeting ->
                    MeetingItem(
                        meeting = meeting,
                        meetingsState = meetingsState,
                        onEvent = onEvent
                    )
                }
            }
            if (meetingsState.isRescheduleClicked) {
                ScheduleMeetDialog(
                    onDismiss = { onEvent(MeetingsEvent.OnMeetingDismissed) },
                    onConfirm = { date, time ->
                        meetingsState.selectedMeeting?.let { meeting ->
                            onEvent(
                                MeetingsEvent.OnRescheduleConfirmed(
                                    meeting,
                                    date,
                                    time
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SegmentedButton(
    isSortByCreatedAt: Boolean,
    onSortByCreatedAt: () -> Unit,
    onSortByTime: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSortByCreatedAt) MaterialTheme.colorScheme.inversePrimary else Color.Transparent)
                .clickable { onSortByCreatedAt() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Created At",
                fontSize = 16.sp,
                color = if (isSortByCreatedAt) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isSortByCreatedAt) MaterialTheme.colorScheme.inversePrimary else Color.Transparent)
                .clickable { onSortByTime() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Time",
                fontSize = 16.sp,
                color = if (!isSortByCreatedAt) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

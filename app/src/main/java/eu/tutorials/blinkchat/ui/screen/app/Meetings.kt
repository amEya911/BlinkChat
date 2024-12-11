package eu.tutorials.blinkchat.ui.screen.app

import android.util.Log
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.MeetingsEvent
import eu.tutorials.blinkchat.data.state.MeetingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.component.meetings.MeetingContactPress
import eu.tutorials.blinkchat.ui.component.meetings.MeetingItem
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor

@Composable
fun Meetings(
    modifier: Modifier = Modifier,
    meetingsState: MeetingsState,
    onEvent: (MeetingsEvent) -> Unit,
    onAddClicked: () -> Unit
) {
    val contentModifier = if (meetingsState.isMeetingClicked || meetingsState.isRescheduleClicked) Modifier.blur(20.dp) else Modifier

    Scaffold(
        topBar = {
            AppBar(
                title = "Meetings",
                onIconClick = { onAddClicked()},
                iconResId = Icons.Default.Add
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TextField(
                    value = "",
                    onValueChange = {},
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Created At",
                        fontSize = 16.sp,
                        color = if (meetingsState.isSortByCreatedAt) Color.Blue else Color.Gray,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onEvent(MeetingsEvent.OnSortByCreatedAt)
                            }
                            .padding(8.dp)
                    )
                    Text(
                        text = "Time",
                        fontSize = 16.sp,
                        color = if (!meetingsState.isSortByCreatedAt) Color.Blue else Color.Gray,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onEvent(MeetingsEvent.OnSortByTime)
                            }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val updatedMatchedContacts = meetingsState.meetings.map { meeting ->
                    val updatedOtherUserContact = meetingsState.contacts.find { it.id == meeting.otherUserContact.id }
                    Log.d("nhi-bey", "updatedOtherUserContact: $updatedOtherUserContact")
                    if (updatedOtherUserContact != null) {
                        meeting.copy(otherUserContact = updatedOtherUserContact)
                    } else {
                        meeting
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
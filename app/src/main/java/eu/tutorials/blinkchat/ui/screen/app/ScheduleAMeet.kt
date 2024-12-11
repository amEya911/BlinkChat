package eu.tutorials.blinkchat.ui.screen.app

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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.ScheduleAMeetEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.ScheduleAMeetState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@Composable
fun ScheduleAMeet(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    onBackClicked: () -> Unit,
    onScheduleConfirmed: (Contact, String, String) -> Unit,
    scheduleAMeetState: ScheduleAMeetState,
    onEvent: (ScheduleAMeetEvent) -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Schedule A Meet",
                navigationIcon = Icons.Default.ArrowBackIosNew,
                onNavigationIconClicked = onBackClicked
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = BackgroundColor
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
                LazyColumn {
                    items(contacts.sortedBy { it.displayName }) { user ->
                        UserItem(
                            user = user,
                            buttonName = "Schedule",
                            onClick = {
                                onEvent(ScheduleAMeetEvent.OnButtonClicked(selectedContact = user))
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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


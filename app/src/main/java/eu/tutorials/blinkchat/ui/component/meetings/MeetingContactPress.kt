package eu.tutorials.blinkchat.ui.component.meetings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.app.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.state.app.MeetingsState
import eu.tutorials.blinkchat.ui.component.inbox.MenuDivider
import eu.tutorials.blinkchat.ui.component.inbox.MenuItem

@Composable
fun MeetingContactPress(
    meetingsState: MeetingsState,
    onEvent: (MeetingsEvent) -> Unit,
    contactComposable: @Composable (meeting: Meeting) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isContactPressed = meetingsState.selectedMeeting != null

    AnimatedVisibility(
        visible = isContactPressed,
        enter = fadeIn(animationSpec = tween(25)) + expandHorizontally(animationSpec = spring())
    ) {
        meetingsState.selectedMeeting?.let { meeting ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = { onEvent(MeetingsEvent.OnMeetingDismissed) },
                        interactionSource = interactionSource,
                        indication = null
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            contactComposable(meeting)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        MeetingPressMenu(
                            onCancelClicked = {onEvent(MeetingsEvent.OnMeetingDismissed)},
                            onRescheduleMeet = {onEvent(MeetingsEvent.OnRescheduleClicked)},
                            onCallOffClicked = {onEvent(MeetingsEvent.OnCallOffClicked(meeting))}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingPressMenu(
    onCancelClicked:() -> Unit,
    onRescheduleMeet: () -> Unit,
    onCallOffClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                text = "Reschedule",
                iconVector = Icons.Default.Edit,
                onClick = onRescheduleMeet
            )

            MenuDivider()

            MenuItem(
                text = "Call Off",
                iconVector = Icons.Default.DeleteForever,
                onClick = onCallOffClicked
            )

            MenuItem(
                text = "Cancel",
                iconVector = Icons.Default.Clear,
                onClick = onCancelClicked
            )
        }
    }
}
package eu.tutorials.blinkchat.ui.component.meetings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.app.MeetingsEvent
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.state.app.MeetingsState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MeetingItem(
    meeting: Meeting,
    meetingsState: MeetingsState,
    onEvent: (MeetingsEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onEvent(MeetingsEvent.OnMeetingClicked(meeting))
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (meeting.otherUserContact.photoUri != null) {
            AsyncImage(
                model = meeting.otherUserContact.photoUri,
                contentDescription = "photoUri",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile_image),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = meeting.otherUserContact.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            val formattedDate = remember(meeting.date) { formatMeetingDate(meeting.date) }
            val createdBy = if (meetingsState.currentUserId == meeting.createdBy.id) "You" else "Them"

            Text(text = "${meeting.time} $formattedDate", textAlign = TextAlign.End)
            Text(text = "Created by: $createdBy")
        }
    }

    HorizontalDivider(
        color = Color.White.copy(alpha = 0.4f)
    )
}

fun formatMeetingDate(date: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val parsedDate = LocalDate.parse(date, formatter)
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        when (parsedDate) {
            today -> "Today"
            tomorrow -> "Tom"
            else -> parsedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    } else {
        date
    }
}

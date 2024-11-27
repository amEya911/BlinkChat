package eu.tutorials.blinkchat.ui.screen.app

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.state.MeetingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
import eu.tutorials.blinkchat.ui.viewmodel.MeetingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle


@Composable
fun Meetings(
    modifier: Modifier = Modifier,
    meetingsViewModel: MeetingsViewModel = hiltViewModel()
) {
    val meetingsState = meetingsViewModel.meetingsState.collectAsState().value

    Scaffold(
        topBar = {
            AppBar(
                title = "Meetings",
                showIcon = true,
                onIconClick = { },
                iconResId = R.drawable.calendar
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
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

                LazyColumn {
                    items(meetingsState.meetings.reversed()) { meeting ->
                        MeetingItem(meeting, meetingsState)
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingItem(
    meeting: Meeting,
    meetingsState: MeetingsState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {

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
            Text(text = "${meeting.time} $formattedDate", textAlign = TextAlign.End)
            Text(text = "Created by: ${meeting.createdBy.displayName}")
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

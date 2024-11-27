package eu.tutorials.blinkchat.ui.component.inbox

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.InboxEvent
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleMeetDialog(
    onEvent: (InboxEvent) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val selectedDate = remember { mutableStateOf<String?>(null) }
    val selectedTime = remember { mutableStateOf<String?>(null) }
    val step = remember { mutableStateOf(1) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = { onEvent(InboxEvent.OnScheduleDismissed) },
                interactionSource = interactionSource,
                indication = null
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Schedule a Meet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (step.value == 1) {
                        Text(
                            text = "Select a Date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DatePicker(
                            onDateSelected = { date ->
                                selectedDate.value = date
                                step.value = 2
                            }
                        )
                    } else if (step.value == 2) {
                        Text(
                            text = "Select a Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedDate.value?.let {
                            TimePicker(selectedDate = it, onTimeSelected = { time ->
                                selectedTime.value = time
                            })
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            if (step.value == 1) {
                                onEvent(InboxEvent.OnScheduleDismissed)
                            } else {
                                step.value = 1
                            }
                        }) {
                            Text(if (step.value == 1) "Cancel" else "Back")
                        }

                        TextButton(onClick = {
                            val date = selectedDate.value
                            val time = selectedTime.value

                            if (step.value == 1) {
                                if (date == null) {
                                    Toast.makeText(context, "Please select a date.", Toast.LENGTH_SHORT).show()
                                } else {
                                    step.value = 2
                                }
                            } else if (step.value == 2) {
                                if (time == null) {
                                    Toast.makeText(context, "Please select a time.", Toast.LENGTH_SHORT).show()
                                } else {
                                    onEvent(InboxEvent.OnScheduleConfirmed(date!!, time))
                                    onEvent(InboxEvent.OnScheduleDismissed)
                                }
                            }
                        }) {
                            Text(if (step.value == 1) "Next" else "Confirm")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DatePicker(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.add(Calendar.DAY_OF_MONTH, 7)
    val maxDate = calendar.timeInMillis

    calendar.timeInMillis = System.currentTimeMillis()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(
                Locale("en", "IN"),
                "%02d/%02d/%d",
                selectedDay,
                selectedMonth + 1,
                selectedYear
            )
            onDateSelected(formattedDate)
        },
        currentYear,
        currentMonth,
        currentDay
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
        datePicker.maxDate = maxDate
    }

    Button(
        onClick = { datePickerDialog.show() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("Pick Date")
    }
}

@Composable
fun TimePicker(selectedDate: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    val today = String.format(Locale("en", "IN"), "%02d/%02d/%d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR))

    val isToday = selectedDate == today
    Log.d("ScheduleMeet", "isToday: $isToday")

    val minHour = if (isToday) currentHour else 0
    val minMinute = if (isToday && currentMinute > 0) currentMinute else 0

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            if (isToday) {
                if (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute)) {
                    Toast.makeText(context, "Cannot pick a time before the current time.", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }
            }

            val formattedTime = String.format(
                Locale("en", "IN"),
                "%02d:%02d",
                selectedHour,
                selectedMinute
            )
            onTimeSelected(formattedTime)
        },
        minHour,
        minMinute,
        true
    )

    Button(
        onClick = { timePickerDialog.show() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("Pick Time")
    }
}






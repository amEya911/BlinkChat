package eu.tutorials.blinkchat.ui.component

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleMeetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val selectedDate = remember { mutableStateOf<String?>(null) }
    val selectedTime = remember { mutableStateOf<String?>(null) }
    val step = remember { mutableStateOf(1) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
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

                    when (step.value) {
                        1 -> {
                            DatePicker(onDateSelected = { date ->
                                if (date.isNullOrEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Invalid date. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    selectedDate.value = date
                                    step.value = 2
                                }
                            })
                        }

                        2 -> {
                            selectedDate.value?.let {
                                TimePicker(selectedDate = it, onTimeSelected = { time ->
                                    if (time.isNullOrEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Invalid time. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        selectedTime.value = time
                                        step.value = 3
                                    }
                                })
                            }
                        }

                        3 -> {
                            Text(
                                text = "Confirm your selection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Date: ${selectedDate.value}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Time: ${selectedTime.value}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                onConfirm(selectedDate.value!!, selectedTime.value!!)
                                onDismiss()
                            }) {
                                Text(text = "Submit")
                            }

                            Button(
                                onClick = { step.value = 1 }
                            ) {
                                Text(text = "reselect")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatePicker(onDateSelected: (String?) -> Unit) {
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

    datePickerDialog.show()
}

@Composable
fun TimePicker(selectedDate: String, onTimeSelected: (String?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    val today = String.format(
        Locale("en", "IN"),
        "%02d/%02d/%d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR)
    )

    val isToday = selectedDate == today

    fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                if (isToday) {
                    if (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute)) {
                        Toast.makeText(
                            context,
                            "Cannot pick a time before the current time.",
                            Toast.LENGTH_SHORT
                        ).show()
                        showTimePickerDialog()
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
            currentHour,
            currentMinute,
            true
        )
        timePickerDialog.show()
    }

    showTimePickerDialog()
}


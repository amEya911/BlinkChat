package eu.tutorials.blinkchat.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toLocalDateTime(time: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return LocalDateTime.parse("$this $time", formatter)
    }
}
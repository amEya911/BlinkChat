package eu.tutorials.blinkchat.data.model

import android.graphics.Bitmap

data class Message(
    val messageText: String? = null,
    val readMessage: String? = null,
    val image: Bitmap? = null
)




package eu.tutorials.blinkchat.data.model

data class Message(
    val messageText: String = "",
    val readMessage: String? = null,
    val imageUrls: List<String>? = null
)
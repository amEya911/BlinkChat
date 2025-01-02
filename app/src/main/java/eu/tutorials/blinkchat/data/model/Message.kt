package eu.tutorials.blinkchat.data.model

data class Message(
    val messageText: String = "",
    val readMessage: String? = null,
    val imageUrls: List<Image>? = null
)

data class Image(
    val url: String,
    val opened: Boolean = false
)
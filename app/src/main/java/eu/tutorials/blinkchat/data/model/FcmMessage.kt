package eu.tutorials.blinkchat.data.model

data class FcmMessage(
    val data: Map<String, String>,
    val tokens: List<String>
)


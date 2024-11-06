package eu.tutorials.blinkchat.data.model

data class Contact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photoThumbnailUri: String?,
    val photoUri: String?
)

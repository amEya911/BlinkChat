package eu.tutorials.blinkchat.data.model

import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContact

data class Contact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photoThumbnailUri: String?,
    val photoUri: String?
)

fun LocalContact.toContact(): Contact {
    return Contact(
        id = this.id,
        displayName = this.displayName,
        phoneNumber = this.phoneNumber,
        photoUri = this.photoUri,
        photoThumbnailUri = this.photoThumbnailUri
    )
}

package eu.tutorials.blinkchat.data.model

import javax.crypto.SecretKey

data class Meeting(
    val meetingId: String,
    val secretKey: SecretKey,
    val createdBy: Contact,
    val otherUserContact: Contact,
    val date: String,
    val time: String,
    val createdAt: Long
)

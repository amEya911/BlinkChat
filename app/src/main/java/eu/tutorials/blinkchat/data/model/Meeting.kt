package eu.tutorials.blinkchat.data.model

data class Meeting(
    val meetingId: String,
    val createdBy: Contact,
    val otherUserContact: Contact,
    val date: String,
    val time: String
)

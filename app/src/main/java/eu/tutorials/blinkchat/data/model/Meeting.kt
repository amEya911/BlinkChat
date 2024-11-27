package eu.tutorials.blinkchat.data.model

data class Meeting(
    val createdBy: Contact,
    val otherUserContact: Contact,
    val date: String,
    val time: String
)

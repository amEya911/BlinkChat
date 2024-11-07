package eu.tutorials.blinkchat.data.model

data class Message(
    val chatId: String,
    val attachmentUrl: String? = null,
    val messageText: String? = null,
)

//enum class AttachmentType {
//    TEXT, IMAGE, GIF, VIDEO, AUDIO, LOCATION, CONTACT, FILE
//}
//enum class SentMessageStatus {
//    SENT, DELIVERED, READ, PENDING, FAILED
//}

//val messageStatus: SentMessageStatus
//val attachmentType: AttachmentType

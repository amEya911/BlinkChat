package eu.tutorials.blinkchat.data.model

data class Message(
    val id: Int,
    val chatId: Int,
    val attachmentType: AttachmentType,
    val attachmentUrl: String? = null,
    val messageText: String? = null,
    val time: String,
    val isSentMessage: Boolean,
    val sender: Contact,
    val messageStatus: SentMessageStatus
)

enum class AttachmentType {
    TEXT, IMAGE, GIF, VIDEO, AUDIO, LOCATION, CONTACT, FILE
}
enum class SentMessageStatus {
    SENT, DELIVERED, READ, PENDING, FAILED
}

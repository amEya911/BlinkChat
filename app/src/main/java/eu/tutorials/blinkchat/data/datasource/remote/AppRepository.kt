package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Message
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AppRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    fun createChatRoom(
        initiatorUser: Contact,
        recipientUser: Contact,
        callback: (String?) -> Unit
    ) {
        val chatRoomId = UUID.randomUUID().toString()
        val chatRoomData = mapOf(
            "chatRoomId" to chatRoomId,
            "initiatorUser" to initiatorUser,
            "recipientUser" to recipientUser,
            "initiatorMessage" to Message(),
            "recipientMessage" to Message(),
            "createdAt" to System.currentTimeMillis(),
            "activeUsers" to mapOf(
                "initiator" to false,
                "recipient" to false
            )
        )
        firestore.collection("chatRooms").document(chatRoomId).set(chatRoomData)
            .addOnSuccessListener { callback(chatRoomId) }
            .addOnFailureListener { callback(null) }
    }

    suspend fun getChatRoomDetails(
        chatRoomId: String,
        currentId: String
    ): Result<Pair<Pair<Contact?, Contact?>, Pair<String, String>>> {
        return try {
            firestore.collection("chatRooms").document(chatRoomId).get().await().let { snapshot ->
                if (!snapshot.exists()) return Result.failure(Exception("Chat room not found"))

                val initiatorUser = (snapshot.get("initiatorUser") as? Map<*, *>)?.toContact()
                val recipientUser = (snapshot.get("recipientUser") as? Map<*, *>)?.toContact()

                val initiatorId = initiatorUser?.id ?: throw Exception("Initiator user ID is missing")
                val recipientId = recipientUser?.id ?: throw Exception("Recipient user ID is missing")

                when (currentId) {
                    initiatorId -> Result.success(
                        Pair(
                            Pair(initiatorUser, recipientUser),
                            Pair(initiatorId, recipientId)
                        )
                    )

                    recipientId -> Result.success(
                        Pair(
                            Pair(recipientUser, initiatorUser),
                            Pair(initiatorId, recipientId)
                        )
                    )

                    else -> Result.failure(Exception("Current user is not part of this chat room"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setUserPresence(
        chatRoomId: String,
        currentUserId: String,
        isOnline: Boolean,
        initiatorUserId: String,
        recipientUserId: String
    ) {
        val userId = when (currentUserId) {
            initiatorUserId -> "initiator"
            recipientUserId -> "recipient"
            else -> return
        }
        firestore.collection("chatRooms").document(chatRoomId)
            .update("activeUsers.$userId", isOnline)
            .addOnFailureListener { Log.e("AppRepo", "Failed to update presence: ${it.message}") }
    }

    fun listenForPresenceUpdates(
        chatRoomId: String,
        onPresenceUpdate: (Map<String, Boolean>) -> Unit
    ) {
        firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for presence updates", e)
                    return@addSnapshotListener
                }
                val activeUsers = snapshot?.get("activeUsers") as? Map<String, Boolean>
                onPresenceUpdate(activeUsers ?: emptyMap())
            }
    }

    private fun Map<*, *>.toContact(): Contact = Contact(
        id = this["id"] as? String ?: "",
        displayName = this["displayName"] as? String ?: "",
        phoneNumber = this["phoneNumber"] as? String ?: "",
        photoUri = this["photoUri"] as? String,
        photoThumbnailUri = this["photoThumbnailUri"] as? String
    )

    fun updateTypingMessage(
        chatRoomId: String,
        messageText: String,
        currentUserId: String,
        initiatorUserId: String,
        recipientUserId: String
    ) {
        val messageField = when (currentUserId) {
            initiatorUserId -> "initiatorMessage"
            recipientUserId -> "recipientMessage"
            else -> return
        }

        firestore.collection("chatRooms").document(chatRoomId)
            .update("$messageField.messageText", messageText)
            .addOnFailureListener {
                Log.e("AppRepo", "Failed to update typing message: ${it.message}")
            }
    }

    fun updateReadMessages(
        chatRoomId: String,
        messageText: String,
        currentUserId: String,
        initiatorId: String,
        recipientId: String,
    ) {
        Log.d("AppRepo", "Inside updateReadMessage: $messageText")

        val messageField = when (currentUserId) {
            initiatorId -> "recipientMessage"
            recipientId -> "initiatorMessage"
            else -> return
        }

        firestore.collection("chatRooms").document(chatRoomId)
            .update("$messageField.readMessage", messageText)
            .addOnFailureListener {
                Log.e("AppRepo", "Failed to update readMessage: ${it.message}")
            }
    }

    fun listenForMessages(
        chatRoomId: String,
        currentUserId: String,
        initiatorId: String,
        recipientId: String,
        onMessageReceived: (String) -> Unit
    ) {
        val messageField = when (currentUserId) {
            initiatorId -> "recipientMessage.messageText"
            recipientId -> "initiatorMessage.messageText"
            else -> return
        }

        firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for messages", e)
                    return@addSnapshotListener
                }

                snapshot?.getString(messageField)?.let { newMessage ->
                    onMessageReceived(newMessage)
                }
            }
    }

    fun listenForReadMessages(
        chatRoomId: String,
        currentUserId: String,
        initiatorId: String,
        recipientId: String,
        onMessageReceived: (String) -> Unit
    ) {
        val messageField = when (currentUserId) {
            initiatorId -> "initiatorMessage.readMessage"
            recipientId -> "recipientMessage.readMessage"
            else -> return
        }

        firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for messages", e)
                    return@addSnapshotListener
                }

                snapshot?.getString(messageField)?.let { newMessage ->
                    onMessageReceived(newMessage)
                }
            }
    }

    fun deleteMessages(chatRoomId: String) {
        val updates = mapOf(
            "initiatorMessage.messageText" to "",
            "recipientMessage.messageText" to ""
        )

        firestore.collection("chatRooms").document(chatRoomId)
            .update(updates)
            .addOnFailureListener { Log.e("AppRepo", "Failed to delete messages: ${it.message}") }
    }
}

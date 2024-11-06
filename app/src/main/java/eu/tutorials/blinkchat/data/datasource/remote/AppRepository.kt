package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
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
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .set(chatRoomData)
            .addOnSuccessListener {
                Log.d("AppRepo", "Chat room created. ID: $chatRoomId")
                callback(chatRoomId)
            }
            .addOnFailureListener { e ->
                Log.d("AppRepo", "Error creating chat room: ${e.message}")
                callback(null)
            }
    }

    suspend fun getChatRoomDetails(chatRoomId: String, currentId: String): Result<Pair<Contact?, Contact?>> {
        return try {
            val documentSnapshot = firestore.collection("chatRooms")
                .document(chatRoomId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                // Extract initiator and recipient user data
                val initiatorData = documentSnapshot.get("initiatorUser") as? Map<*, *>
                val recipientData = documentSnapshot.get("recipientUser") as? Map<*, *>

                // Parse initiator user data
                val initiatorUser = initiatorData?.let {
                    Contact(
                        id = it["id"] as? String ?: "",
                        displayName = it["displayName"] as? String ?: "",
                        phoneNumber = it["phoneNumber"] as? String ?: "",
                        photoUri = it["photoUri"] as? String,
                        photoThumbnailUri = it["photoThumbnailUri"] as? String
                    )
                }

                // Parse recipient user data
                val recipientUser = recipientData?.let {
                    Contact(
                        id = it["id"] as? String ?: "",
                        displayName = it["displayName"] as? String ?: "",
                        phoneNumber = it["phoneNumber"] as? String ?: "",
                        photoUri = it["photoUri"] as? String,
                        photoThumbnailUri = it["photoThumbnailUri"] as? String
                    )
                }

                // Determine current and other user
                val (currentUserContact, otherUserContact) = when (currentId) {
                    initiatorUser?.id -> Pair(initiatorUser, recipientUser)
                    recipientUser?.id -> Pair(recipientUser, initiatorUser)
                    else -> Pair(null, null)
                }

                if (currentUserContact != null && otherUserContact != null) {
                    Result.success(Pair(currentUserContact, otherUserContact))
                } else {
                    Result.failure(Exception("Current user is not part of this chat room"))
                }
            } else {
                Result.failure(Exception("Chat room not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}



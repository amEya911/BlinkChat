package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.util.Crypto
import java.util.UUID
import javax.inject.Inject

class RecentChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val secretKey = Crypto.stringToKey("YourSharedKeyString")

    fun updateRecentChats(userId: String, otherUser: Contact, chatRoomId: String) {
        val recentChatId = UUID.randomUUID().toString()
        val recentChatEntry = mapOf(
            "recentChatId" to recentChatId,
            "userContact" to otherUser,
            "chatRoomId" to chatRoomId,
            "lastUpdated" to System.currentTimeMillis()
        )

        val encryptedRecentChatEntry = recentChatEntry.mapValues { (key, value) ->
            when (key) {
                "recentChatId", "chatRoomId" -> value
                "userContact" -> Crypto.encryptContact(value as Contact, secretKey)
                else -> if (value is String) Crypto.encrypt(value, secretKey) else value
            }
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val recentChats =
                        document.get("recentChats") as? List<Map<String, Any>> ?: emptyList()
                    val updatedChats = recentChats.filter {  chat ->
                        val encryptedUserContactMap = chat["userContact"] as? String
                        val userContact = encryptedUserContactMap?.let {
                            try {
                                Crypto.decryptContact(it, secretKey) // Returns a Contact directly
                            } catch (e: Exception) {
                                Log.e("listenToRecentChats", "Failed to decrypt contact", e)
                                null
                            }
                        }
                        userContact?.id != otherUser.id
                    }
                    val newRecentChats = updatedChats + encryptedRecentChatEntry

                    firestore.collection("users").document(userId)
                        .update("recentChats", newRecentChats)
                        .addOnSuccessListener {
                            Log.d("ChatRoom", "Recent chats updated for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatRoom", "Failed to update recent chats for user: $userId", e)
                        }
                } else {
                    firestore.collection("users").document(userId)
                        .set(mapOf("recentChats" to listOf(encryptedRecentChatEntry)))
                        .addOnSuccessListener {
                            Log.d("ChatRoom", "Recent chats created for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatRoom", "Failed to create recent chats for user: $userId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatRoom", "Failed to retrieve user document: $userId", e)
            }
    }

    fun listenToRecentChats(currentUserId: String, onResult: (List<Pair<Contact, String>>) -> Unit) {
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    Log.e(
                        "listenToRecentChats",
                        "Error listening for updates: ${exception.message}",
                        exception
                    )
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val recentChats =
                        document["recentChats"] as? List<Map<String, Any>> ?: emptyList()

                    val contactList = recentChats.mapNotNull { chat ->
                        val encryptedUserContactMap = chat["userContact"] as? String
                        val recentChatId = chat["recentChatId"] as? String

                        val userContact = encryptedUserContactMap?.let {
                            try {
                                Crypto.decryptContact(it, secretKey) // Returns a Contact directly
                            } catch (e: Exception) {
                                Log.e("listenToRecentChats", "Failed to decrypt contact", e)
                                null
                            }
                        }

                        if (userContact != null && recentChatId != null) {
                            userContact to recentChatId
                        } else {
                            null
                        }
                    }
                    onResult(contactList)
                } else {
                    Log.w(
                        "listenToRecentChats",
                        "Document does not exist or has no recentChats field."
                    )
                    onResult(emptyList())
                }
            }
        }

    fun listenForPresence(currentUserId: String, callback: (List<String?>) -> Unit) {
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for presence updates", e)
                    return@addSnapshotListener
                }
                val chatRoomIds = extractChatRoomIds(snapshot)
                fetchActiveUserNamesWithListener(chatRoomIds, currentUserId, callback)
            }
    }

    private fun extractChatRoomIds(snapshot: DocumentSnapshot?): List<String?> {
        if (snapshot != null && snapshot.exists()) {
            val recentChats = snapshot.get("recentChats") as? List<Map<String, Any>>
            return recentChats?.map { it["chatRoomId"] as? String } ?: emptyList()
        }
        return emptyList()
    }

    private fun fetchActiveUserNamesWithListener(
        chatRoomIds: List<String?>,
        currentUserId: String,
        callback: (List<String>) -> Unit
    ) {
        val activeUserNames = mutableSetOf<String>()

        if (chatRoomIds.isEmpty()) {
            Log.d("AppRepo", "No chat rooms found, calling callback with empty list.")
            callback(activeUserNames.toList())
            return
        }

        for (chatRoomId in chatRoomIds) {
            if (chatRoomId == null) continue

            firestore.collection("chatRooms").document(chatRoomId)
                .addSnapshotListener { chatRoomSnapshot, e ->
                    if (e != null) {
                        Log.e("AppRepo", "Error listening to chat room $chatRoomId", e)
                        return@addSnapshotListener
                    }
                    if (chatRoomSnapshot != null && chatRoomSnapshot.exists()) {
                        val initiatorUser =
                            chatRoomSnapshot.get("initiatorUser") as? Map<String, Any>
                        val recipientUser =
                            chatRoomSnapshot.get("recipientUser") as? Map<String, Any>
                        val activeUsers =
                            chatRoomSnapshot.get("activeUsers") as? Map<String, Boolean>

                        if (initiatorUser != null && recipientUser != null && activeUsers != null) {
                            val initiatorUserId = initiatorUser["id"] as? String
                            val recipientUserId = recipientUser["id"] as? String

                            if (initiatorUserId != null && recipientUserId != null) {
                                val isCurrentUserInitiator = currentUserId == initiatorUserId
                                val isCurrentUserRecipient = currentUserId == recipientUserId
                                if (isCurrentUserInitiator) {
                                    val isRecipientActive = activeUsers["recipient"] == true
                                    if (isRecipientActive) {
                                        if (!activeUserNames.contains(recipientUserId)) {
                                            activeUserNames.add(recipientUserId)
                                            Log.d(
                                                "AppRepo",
                                                "Added active user ID: $recipientUserId (Recipient)"
                                            )
                                        }
                                    } else {
                                        if (activeUserNames.contains(recipientUserId)) {
                                            activeUserNames.remove(recipientUserId)
                                            Log.d(
                                                "AppRepo",
                                                "Removed inactive user ID: $recipientUserId (Recipient)"
                                            )
                                        }
                                    }
                                    callback(activeUserNames.toList())
                                }
                                if (isCurrentUserRecipient) {
                                    val isInitiatorActive = activeUsers["initiator"] == true
                                    if (isInitiatorActive) {
                                        if (!activeUserNames.contains(initiatorUserId)) {
                                            activeUserNames.add(initiatorUserId)
                                            Log.d(
                                                "AppRepo",
                                                "Added active user ID: $initiatorUserId (Initiator)"
                                            )
                                        }
                                    } else {
                                        if (activeUserNames.contains(initiatorUserId)) {
                                            activeUserNames.remove(initiatorUserId)
                                            Log.d(
                                                "AppRepo",
                                                "Removed inactive user ID: $initiatorUserId (Initiator)"
                                            )
                                        }
                                    }
                                    callback(activeUserNames.toList())
                                }
                            }
                        }
                    } else {
                        Log.e("AppRepo", "Chat room $chatRoomId snapshot does not exist")
                    }
                }
        }
    }

    fun deleteRecentChat(
        recentChatId: String,
        currentUserId: String
    ) {
        deleteRecentChatForUser(recentChatId, currentUserId)
    }

    private fun deleteRecentChatForUser(
        recentChatId: String,
        userId: String
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val recentChats =
                        document.get("recentChats") as? List<Map<String, Any>> ?: emptyList()

                    val updatedRecentChats = recentChats.filterNot { meet ->
                        meet["recentChatId"] == recentChatId
                    }

                    firestore.collection("users").document(userId)
                        .update("recentChats", updatedRecentChats)
                        .addOnSuccessListener {
                            Log.d(
                                "RecentChats",
                                "Recent Chat successfully deleted for $userId"
                            )
                        }.addOnFailureListener { e ->
                            Log.e("RecentChats", "Failed to delete Recent Chat for $userId", e)
                        }

                } else {
                    Log.e("RecentChats", "User document does not exist: $userId")
                }


            }.addOnFailureListener { e ->
                Log.e("RecentChats", "Error retrieving user document: $userId", e)
            }

    }

}
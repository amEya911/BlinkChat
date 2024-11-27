package eu.tutorials.blinkchat.data.datasource.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AppRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun createChatRoom(
        initiatorUser: Contact,
        recipientUser: Contact,
        context: Context,
        recipientUserExists: Boolean,
        callback: (String?) -> Unit
    ) {
        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection. Room creation failed.", Toast.LENGTH_LONG).show()
            callback(null)
            return
        }

        checkChatRoomExists(initiatorUser, recipientUser) { existingChatRoomId ->
            if (existingChatRoomId != null) {
                updateRecentChats(initiatorUser.id, recipientUser, existingChatRoomId)
                if (recipientUserExists) {
                    updateRecentChats(recipientUser.id, initiatorUser, existingChatRoomId)
                }
                callback(existingChatRoomId)
            } else {
                createNewChatRoom(initiatorUser, recipientUser, context, recipientUserExists, callback)
            }
        }
    }

    private fun checkChatRoomExists(
        initiatorUser: Contact,
        recipientUser: Contact,
        callback: (String?) -> Unit
    ) {
        firestore.collection("chatRooms")
            .whereEqualTo("initiatorUser.id", initiatorUser.id)
            .whereEqualTo("recipientUser.id", recipientUser.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingChatRoomId = querySnapshot.documents.first().id
                    callback(existingChatRoomId)
                    return@addOnSuccessListener
                }

                firestore.collection("chatRooms")
                    .whereEqualTo("initiatorUser.id", recipientUser.id)
                    .whereEqualTo("recipientUser.id", initiatorUser.id)
                    .get()
                    .addOnSuccessListener { reverseQuerySnapshot ->
                        if (!reverseQuerySnapshot.isEmpty) {
                            val existingChatRoomId = reverseQuerySnapshot.documents.first().id
                            callback(existingChatRoomId)
                        } else {
                            callback(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ChatRoom", "Error checking reverse chat room: ${exception.message}", exception)
                        callback(null)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatRoom", "Error checking chat room: ${exception.message}", exception)
                callback(null)
            }
    }

    private fun createNewChatRoom(
        initiatorUser: Contact,
        recipientUser: Contact,
        context: Context,
        recipientUserExists: Boolean,
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
            .addOnSuccessListener {
                callback(chatRoomId)
                updateRecentChats(initiatorUser.id, recipientUser, chatRoomId)
                if (recipientUserExists) {
                    updateRecentChats(recipientUser.id, initiatorUser, chatRoomId)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to create chat room: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("ChatRoom", "Error creating chat room: ${exception.message}", exception)
                callback(null)
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

    private fun fetchActiveUserNamesWithListener(chatRoomIds: List<String?>, currentUserId: String, callback: (List<String>) -> Unit) {
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
                        val initiatorUser = chatRoomSnapshot.get("initiatorUser") as? Map<String, Any>
                        val recipientUser = chatRoomSnapshot.get("recipientUser") as? Map<String, Any>
                        val activeUsers = chatRoomSnapshot.get("activeUsers") as? Map<String, Boolean>

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
                                            Log.d("AppRepo", "Added active user ID: $recipientUserId (Recipient)")
                                        }
                                    } else {
                                        if (activeUserNames.contains(recipientUserId)) {
                                            activeUserNames.remove(recipientUserId)
                                            Log.d("AppRepo", "Removed inactive user ID: $recipientUserId (Recipient)")
                                        }
                                    }
                                    callback(activeUserNames.toList())
                                }
                                if (isCurrentUserRecipient) {
                                    val isInitiatorActive = activeUsers["initiator"] == true
                                    if (isInitiatorActive) {
                                        if (!activeUserNames.contains(initiatorUserId)) {
                                            activeUserNames.add(initiatorUserId)
                                            Log.d("AppRepo", "Added active user ID: $initiatorUserId (Initiator)")
                                        }
                                    } else {
                                        if (activeUserNames.contains(initiatorUserId)) {
                                            activeUserNames.remove(initiatorUserId)
                                            Log.d("AppRepo", "Removed inactive user ID: $initiatorUserId (Initiator)")
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

    private fun updateRecentChats(userId: String, otherUser: Contact, chatRoomId: String) {
        val recentChatEntry = mapOf(
            "userId" to otherUser.id,
            "chatRoomId" to chatRoomId,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val recentChats = document.get("recentChats") as? List<Map<String, Any>> ?: emptyList()
                    val updatedChats = recentChats.filter { it["userId"] != otherUser.id }
                    val newRecentChats = updatedChats + recentChatEntry

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
                        .set(mapOf("recentChats" to listOf(recentChatEntry)))
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
        recipientId: String
    ): Flow<String> = callbackFlow {
        val messageField = when (currentUserId) {
            initiatorId -> "recipientMessage.messageText"
            recipientId -> "initiatorMessage.messageText"
            else -> null
        } ?: return@callbackFlow

        val listenerRegistration = firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for messages", e)
                    return@addSnapshotListener
                }
                val message = snapshot?.getString(messageField) ?: ""
                trySend(message).isSuccess
            }
        awaitClose { listenerRegistration.remove() }
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

package eu.tutorials.blinkchat.data.datasource.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Image
import eu.tutorials.blinkchat.data.model.Message
import eu.tutorials.blinkchat.util.Crypto
import eu.tutorials.blinkchat.util.Ids
import eu.tutorials.blinkchat.util.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.SecretKey
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val recentChatRepository: RecentChatRepository,
    private val notificationRepository: NotificationRepository
) {
    fun createChatRoom(
        initiatorUser: Contact,
        recipientUser: Contact,
        context: Context,
        isGuest: Boolean,
        recipientUserExists: Boolean,
        notifyOtherUser: Boolean,
        callback: (String?) -> Unit
    ) {
        checkChatRoomExists(initiatorUser, recipientUser) { existingChatRoomId ->
            if (existingChatRoomId != null) {
                recentChatRepository.updateRecentChats(
                    initiatorUser.id,
                    recipientUser,
                    existingChatRoomId
                )
                if (recipientUserExists) {
                    recentChatRepository.updateRecentChats(
                        recipientUser.id,
                        initiatorUser,
                        existingChatRoomId
                    )
                }
                callback(existingChatRoomId)
            } else {
                createNewChatRoom(
                    initiatorUser,
                    recipientUser,
                    context,
                    isGuest,
                    recipientUserExists
                ) { newChatRoomId ->
                    if (newChatRoomId != null) {
                        callback(newChatRoomId)

                        if (notifyOtherUser) {
                            Log.d("Notif1", "callback: $newChatRoomId")
                            notificationRepository.notifyOtherUser(
                                currentUserId = initiatorUser.id,
                                otherUserId = recipientUser.id,
                                title = "New room created",
                                body = initiatorUser.id,
                                type = NotificationType.CreateRoom.type,
                                deepLink = "https://vanishtest.netlify.app/$newChatRoomId"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkChatRoomExists(
        initiatorUser: Contact,
        recipientUser: Contact,
        callback: (String?) -> Unit
    ) {
        val roomIdentifier = generateRoomIdentifier(initiatorUser.id, recipientUser.id)

        firestore.collection("chatRooms")
            .whereEqualTo("roomIdentifier", roomIdentifier)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingChatRoomId = querySnapshot.documents.first().id
                    callback(existingChatRoomId)
                    return@addOnSuccessListener
                }

                firestore.collection("chatRooms")
                    .whereEqualTo("roomIdentifier", roomIdentifier)
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
                        Log.e(
                            "ChatRoom",
                            "Error checking reverse chat room: ${exception.message}",
                            exception
                        )
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
        isGuest: Boolean,
        recipientUserExists: Boolean,
        callback: (String?) -> Unit
    ) {
        val chatRoomId = UUID.randomUUID().toString()
        val secretKey = Crypto.generateKey()

        val roomIdentifier = generateRoomIdentifier(initiatorUser.id, recipientUser.id)

        val chatRoomData = mapOf(
            "chatRoomId" to chatRoomId,
            "roomIdentifier" to roomIdentifier,
            "secretKey" to Crypto.keyToString(secretKey),
            "initiatorUser" to Crypto.encryptContact(initiatorUser, secretKey),
            "recipientUser" to Crypto.encryptContact(recipientUser, secretKey),
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
                if (!isGuest) {
                    recentChatRepository.updateRecentChats(
                        initiatorUser.id,
                        recipientUser,
                        chatRoomId
                    )
                    if (recipientUserExists) {
                        recentChatRepository.updateRecentChats(
                            recipientUser.id,
                            initiatorUser,
                            chatRoomId
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Failed to create chat room: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("ChatRoom", "Error creating chat room: ${exception.message}", exception)
                callback(null)
            }
    }

    private fun generateRoomIdentifier(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return sortedIds.joinToString("_").hashSHA256()
    }

    private fun String.hashSHA256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }


    suspend fun getChatRoomDetails(
        chatRoomId: String,
        currentId: String
    ): Result<Triple<Pair<Contact?, Contact?>, Pair<String, String>, SecretKey>> {
        return try {
            firestore.collection("chatRooms").document(chatRoomId).get().await().let { snapshot ->
                if (!snapshot.exists()) return Result.failure(Exception("Chat room not found"))

                val secretKeyString = snapshot.getString("secretKey") ?: return Result.failure(
                    Exception("Secret key not found")
                )
                val secretKey = Crypto.stringToKey(secretKeyString)

                val encryptedInitiator = snapshot.get("initiatorUser") as? String
                val encryptedRecipient = snapshot.get("recipientUser") as? String

                val initiatorUser = encryptedInitiator?.let { Crypto.decryptContact(it, secretKey) }
                val recipientUser = encryptedRecipient?.let { Crypto.decryptContact(it, secretKey) }

                val initiatorId =
                    initiatorUser?.id ?: throw Exception("Initiator user ID is missing")
                val recipientId =
                    recipientUser?.id ?: throw Exception("Recipient user ID is missing")

                when (currentId) {
                    initiatorId -> Result.success(
                        Triple(
                            Pair(initiatorUser, recipientUser),
                            Pair(initiatorId, recipientId),
                            secretKey
                        )
                    )

                    recipientId -> Result.success(
                        Triple(
                            Pair(recipientUser, initiatorUser),
                            Pair(initiatorId, recipientId),
                            secretKey
                        )
                    )

                    else -> Result.failure(Exception("Current user is not part of this chat room"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateTypingMessage(
        chatRoomId: String,
        secretKey: SecretKey,
        messageText: String,
        currentUserId: String,
        initiatorUserId: String,
        recipientUserId: String,
        isDeleteImage: Boolean
    ) {
        val messageField = when (currentUserId) {
            initiatorUserId -> "initiatorMessage"
            recipientUserId -> "recipientMessage"
            else -> return
        }

        val encryptedMessageText = Crypto.encrypt(messageText, secretKey)

        val updates = mutableMapOf<String, Any>(
            "$messageField.messageText" to encryptedMessageText
        )

        if (isDeleteImage) {
            updates["$messageField.imageUrls"] = emptyList<String>()
        }

        firestore.collection("chatRooms").document(chatRoomId)
            .update(updates)
            .addOnFailureListener {
                Log.e("AppRepo", "Failed to update typing message: ${it.message}")
            }
    }

    fun updateImage(
        chatRoomId: String,
        secretKey: SecretKey,
        image: Uri,
        currentUserId: String,
        initiatorUserId: String,
        recipientUserId: String,
        context: Context
    ) {
        val bucketName = "vanish-bucket-app"
        val s3Key = "images/${UUID.randomUUID()}.jpg"

        val s3Client = AmazonS3Client(
            CognitoCachingCredentialsProvider(
                context,
                Ids.IDENTITY_POOL_ID,
                Regions.AP_SOUTH_1
            )
        )

        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(image)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Thread {
            try {
                s3Client.putObject(bucketName, s3Key, tempFile)
                val imageUrl = s3Client.getUrl(bucketName, s3Key).toString()
                sendImage(
                    chatRoomId,
                    secretKey,
                    imageUrl,
                    currentUserId,
                    initiatorUserId,
                    recipientUserId
                )
            } catch (e: Exception) {
                Log.e("AppRepo", "Failed to upload image to S3: ${e.message}", e)
            } finally {
                tempFile.delete()
            }
        }.start()
    }

    private fun sendImage(
        chatRoomId: String,
        secretKey: SecretKey,
        image: String,
        currentUserId: String,
        initiatorUserId: String,
        recipientUserId: String
    ) {
        val messageField = when (currentUserId) {
            initiatorUserId -> "initiatorMessage"
            recipientUserId -> "recipientMessage"
            else -> return
        }

        val encryptedImage = Crypto.encrypt(image, secretKey)

        val imageMap = mapOf(
            "url" to encryptedImage,
            "opened" to false
        )

        firestore.collection("chatRooms").document(chatRoomId)
            .update("$messageField.imageUrls", FieldValue.arrayUnion(imageMap))
            .addOnFailureListener {
                Log.e("AppRepo", "Failed to update image: ${it.message}")
            }
    }

    fun updateImageStatus(
        chatRoomId: String,
        secretKey: SecretKey,
        image: String,
        currentUserId: String,
        initiatorUserId: String,
        recipientUserId: String
    ) {
        val messageField = when (currentUserId) {
            initiatorUserId -> "recipientMessage.imageUrls"
            recipientUserId -> "initiatorMessage.imageUrls"
            else -> return
        }

        firestore.collection("chatRooms").document(chatRoomId).get()
            .addOnSuccessListener { snapshot ->
                val imageUrls = snapshot.get(messageField) as? List<Map<String, Any>>

                val updatedImageUrls = imageUrls?.map { imageMap ->
                    val encryptedUrl = imageMap["url"] as? String
                    val decryptedUrl = encryptedUrl?.let { Crypto.decrypt(it, secretKey) }

                    if (decryptedUrl == image) {
                        imageMap.toMutableMap().apply {
                            this["opened"] = true
                        }
                    } else {
                        imageMap
                    }
                }

                if (updatedImageUrls != null) {
                    firestore.collection("chatRooms").document(chatRoomId)
                        .update(messageField, updatedImageUrls)
                        .addOnSuccessListener {
                            Log.d("AppRepo", "Image status updated successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AppRepo", "Failed to update image status: ${e.message}")
                        }
                } else {
                    Log.e("AppRepo", "Image list is null or empty.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AppRepo", "Failed to fetch chat room document: ${e.message}")
            }
    }

    fun updateReadMessages(
        chatRoomId: String,
        secretKey: SecretKey,
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

        val encryptedMessageText = Crypto.encrypt(messageText, secretKey)

        firestore.collection("chatRooms").document(chatRoomId)
            .update("$messageField.readMessage", encryptedMessageText)
            .addOnFailureListener {
                Log.e("AppRepo", "Failed to update readMessage: ${it.message}")
            }
    }

    fun listenForMessages(
        chatRoomId: String,
        secretKey: SecretKey,
        currentUserId: String,
        initiatorId: String,
        recipientId: String
    ): Flow<Pair<String, List<Image>?>> = callbackFlow {
        val messageField = when (currentUserId) {
            initiatorId -> "recipientMessage"
            recipientId -> "initiatorMessage"
            else -> null
        } ?: return@callbackFlow

        val listenerRegistration = firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for messages", e)
                    return@addSnapshotListener
                }
                val encryptedMessageText = snapshot?.getString("$messageField.messageText") ?: ""
                val messageText =
                    if (encryptedMessageText.isEmpty()) encryptedMessageText else Crypto.decrypt(
                        encryptedMessageText,
                        secretKey
                    )
                val imageUrls = snapshot?.get("$messageField.imageUrls") as? List<Map<String, Any>>
                val images = imageUrls?.mapNotNull { imageMap ->
                    val encryptedUrl = imageMap["url"] as? String
                    val url = encryptedUrl?.let { Crypto.decrypt(it, secretKey) }
                    val opened = imageMap["opened"] as? Boolean
                    if (url != null && opened != null) Image(url, opened) else null
                }
                Log.d("imageUrls", "AppRepo: $images")

                trySend(Pair(messageText, images)).isSuccess
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun listenForCurrentUserImages(
        chatRoomId: String,
        secretKey: SecretKey,
        currentUserId: String,
        initiatorId: String,
        recipientId: String
    ): Flow<List<Image>?> = callbackFlow {
        val messageField = when (currentUserId) {
            initiatorId -> "initiatorMessage.imageUrls"
            recipientId -> "recipientMessage.imageUrls"
            else -> return@callbackFlow
        }

        val listenerRegistration = firestore.collection("chatRooms").document(chatRoomId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for messages", e)
                    return@addSnapshotListener
                }

                val imageUrls = snapshot?.get(messageField) as? List<Map<String, Any>>
                val images = imageUrls?.mapNotNull { imageMap ->
                    val encryptedUrl = imageMap["url"] as String
                    val url = Crypto.decrypt(encryptedUrl, secretKey)
                    val opened = imageMap["opened"] as? Boolean
                    if (opened != null) Image(url, opened) else null
                }

                trySend(images).isSuccess
            }
        awaitClose { listenerRegistration.remove() }
    }


    fun listenForReadMessages(
        chatRoomId: String,
        secretKey: SecretKey,
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
                    val decryptedNewMessage =
                        if (newMessage.isEmpty()) newMessage else Crypto.decrypt(
                            newMessage,
                            secretKey
                        )
                    onMessageReceived(decryptedNewMessage)
                }
            }
    }

    fun deleteMessages(
        chatRoomId: String,
        context: Context,
        initiatorImages: List<Image>?,
        recipientImages: List<Image>?
    ) {
        Log.d("AppRepo1", "initiatorImages: $initiatorImages")
        Log.d("AppRepo1", "recipientImages: $recipientImages")

        val bucketName = "vanish-bucket-app"
        val s3Client = AmazonS3Client(
            CognitoCachingCredentialsProvider(
                context,
                Ids.IDENTITY_POOL_ID,
                Regions.AP_SOUTH_1
            )
        )

        val deleteFromS3 = { images: List<Image>? ->
            images?.forEach { image ->
                try {
                    val imageUrl = Uri.parse(image.url)
                    val pathSegments = imageUrl.pathSegments
                    val objectKey =
                        pathSegments.subList(pathSegments.indexOf("images"), pathSegments.size)
                            .joinToString("/")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            s3Client.deleteObject(bucketName, objectKey)
                            Log.d("AppRepo1", "Deleted image from S3: $objectKey")
                        } catch (e: Exception) {
                            Log.e("AppRepo1", "Failed to delete image from S3: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AppRepo1", "Failed to parse image URL: ${e.message}", e)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            deleteFromS3(initiatorImages)
            deleteFromS3(recipientImages)

            val updates = mapOf(
                "initiatorMessage" to Message(),
                "recipientMessage" to Message()
            )

            firestore.collection("chatRooms").document(chatRoomId)
                .update(updates)
                .addOnFailureListener { Log.e("AppRepo1", "Failed to delete messages: ${it.message}") }
        }
    }

    fun deleteChatRoom(chatRoomId: String) {
        firestore.collection("chatRooms").document(chatRoomId)
            .delete()
            .addOnSuccessListener {
                Log.d("ChatRoom", "Chat room deleted successfully: $chatRoomId")
            }
            .addOnFailureListener { e ->
                Log.e("ChatRoom", "Error deleting chat room", e)
            }
    }
}

package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.util.HashUtil
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) {
    fun addUserDetails(contact: Contact) {
        firestore.collection("users").document(contact.id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("UserRepository", "User already exists: ${contact.displayName}")
                    notificationRepository.getFCMToken { token ->
                        token?.let {
                            notificationRepository.addFcmToken(contact.id, it)
                        } ?: Log.d("UserRepository", "FCM Token is null.")
                    }
                } else {
                    firestore.collection("users").document(contact.id)
                        .set(contact)
                        .addOnSuccessListener {
                            Log.d("UserRepository", "User details added for ${contact.displayName}")
                            notificationRepository.getFCMToken { token ->
                                token?.let {
                                    notificationRepository.addFcmToken(contact.id, it)
                                } ?: Log.d("UserRepository", "FCM Token is null.")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserRepository", "Error adding user details: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Error checking user existence: ${e.message}")
            }
    }

    fun currentUserId(): String? {
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        return if (phoneNumber != null) {
            HashUtil.hashPhoneNumber(phoneNumber)
        } else {
            null
        }
    }

    fun getUserDetails(userId: String, onResult: (Contact?) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val id = document.id
                    val displayName = document.getString("displayName") ?: "Unknown"
                    val phoneNumber = document.getString("phoneNumber") ?: "Unknown Number"
                    val photoThumbnailUri = document.getString("photoThumbnailUri")
                    val photoUri = document.getString("photoUri")

                    val contact = Contact(
                        id = id,
                        displayName = displayName,
                        phoneNumber = phoneNumber,
                        photoThumbnailUri = photoThumbnailUri,
                        photoUri = photoUri
                    )

                    onResult(contact)
                } else {
                    Log.e("UserRepository", "User does not exist.")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Error fetching user details: ${e.message}")
                onResult(null)
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

    fun blockUser(
        currentUserId: String,
        otherUserId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIds =
                        document.get("blockedUserIds") as? List<Map<String, Any>> ?: emptyList()
                    val newUserIds = userIds + otherUserId

                    firestore.collection("users").document(currentUserId)
                        .update("blockedUserIds", newUserIds)
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d("BlockUser", "Successfully blocked $otherUserId")
                        }.addOnFailureListener { e->
                            onResult(false, e.localizedMessage)
                            Log.d("BlockUser", "Failed to block $otherUserId")
                        }
                } else {
                    firestore.collection("users").document(currentUserId)
                        .set(mapOf("blockedUserIds" to listOf(otherUserId)))
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d("BlockUser", "Successfully blocked $otherUserId")
                        }.addOnFailureListener { e->
                            onResult(false, e.localizedMessage)
                            Log.d("BlockUser", "Failed to block $otherUserId")
                        }
                }

            }.addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("BlockUser", "Failed to retrieve user document: $currentUserId", e)
            }
    }

    fun checkIfUserIsBlocked(
        currentUserId: String,
        otherUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val blockedUserIds = snapshot.get("blockedUserIds") as? List<String>
                    Log.d("Cringe", "blockedUserIds: $blockedUserIds")
                    val isBlocked = blockedUserIds?.contains(otherUserId) == true
                    Log.d("Cringe", "isBlocked: $isBlocked")
                    onResult(isBlocked)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("BlockUser", "Error fetching blocked user data", e)
                onResult(false)
            }
    }

    fun getAllBlockedUsers(currentUserId: String, onResult: (List<String>) -> Unit) {
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val blockedUserIds = snapshot.get("blockedUserIds") as? List<String>
                    if (blockedUserIds != null) {
                        onResult(blockedUserIds)
                    } else {
                        onResult(emptyList())
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("BlockUser", "Error fetching blocked users", e)
                onResult(emptyList())
            }
    }

    fun unBlockUser(
        currentUserId: String,
        otherUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the current blockedUserIds (ensure it's a list of Strings)
                    val userIds = document.get("blockedUserIds") as? List<String> ?: emptyList()
                    if (userIds.contains(otherUserId)) {
                        val newUserIds = userIds.filterNot { it == otherUserId }

                        firestore.collection("users").document(currentUserId)
                            .update("blockedUserIds", newUserIds)
                            .addOnSuccessListener {
                                onResult(true, null)
                                Log.d("BlockUser", "Successfully unblocked $otherUserId")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.localizedMessage)
                                Log.e("BlockUser", "Failed to unblock $otherUserId: ${e.message}")
                            }
                    } else {
                        onResult(false, "User is not blocked")
                        Log.e("BlockUser", "User $otherUserId is not blocked")
                    }
                } else {
                    onResult(false, "User not found")
                    Log.e("BlockUser", "User document not found for ID: $currentUserId")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("BlockUser", "Error retrieving user document: $currentUserId", e)
            }
    }

    fun logout() {
        val currentUserId = currentUserId() ?: return

        notificationRepository.getFCMToken { token ->
            token?.let {
                notificationRepository.removeFcmToken(currentUserId, it)
            } ?: Log.d("UserRepository", "FCM Token is null.")
        }

        FirebaseAuth.getInstance().signOut()
        Log.d("UserRepository", "User logged out.")
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        val currentUserId = currentUserId()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUserId == null || currentUser == null) {
            Log.e("UserRepository", "Cannot delete account: User is not authenticated.")
            onResult(false)
            return
        }

        // Step 1: Delete user's data from Firestore
        firestore.collection("users").document(currentUserId)
            .delete()
            .addOnSuccessListener {
                Log.d("UserRepository", "User data deleted successfully for $currentUserId.")

                // Step 2: Delete the user's authentication
                currentUser.delete()
                    .addOnSuccessListener {
                        Log.d("UserRepository", "User account deleted successfully.")

                        // Step 3: Remove FCM token
                        notificationRepository.getFCMToken { token ->
                            token?.let {
                                notificationRepository.removeFcmToken(currentUserId, it)
                                Log.d("UserRepository", "FCM token removed successfully.")
                            }
                            onResult(true)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserRepository", "Failed to delete user account: ${e.message}")
                        onResult(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to delete user data: ${e.message}")
                onResult(false)
            }
    }

}




package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.util.Crypto
import eu.tutorials.blinkchat.util.HashUtil
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
    private val appRepository: AppRepository
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

    fun addDisplayName(id: String, displayName: String) {
        firestore.collection("users").document(id)
            .update("displayName", displayName)
            .addOnSuccessListener {
                Log.d("UserRepo", "Display name updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("UserRepo", "Error updating display name", e)
            }
    }

    fun currentUserId(): String? {
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        Log.d("Nimish", "phoneNumber: $phoneNumber")
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

                if (activeUsers != null) {
                    if (activeUsers.isNotEmpty() && activeUsers.values.all { !it }) {
                        appRepository.deleteChatRoom(chatRoomId)
                    }
                }
            }
    }

    fun listenForMutedUsers(
        currentUserId: String,
        onResult: (List<String>) -> Unit
    ) {
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppRepo", "Error listening for muted users", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val mutedUserIds = snapshot.get("mutedUserIds") as? List<String> ?: emptyList()
                    onResult(mutedUserIds)
                } else {
                    onResult(emptyList())
                }
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
                    val blockedUserIds = document.get("blockedUserIds") as? List<String> ?: emptyList()
                    val newBlockedUserIds = blockedUserIds + otherUserId

                    val scheduledMeets = document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()
                    val meetingsToDelete = scheduledMeets.filter { meet ->
                        val createdWith = (meet["createdWith"] as? Map<String, Any>)?.get("id") == otherUserId
                        val createdBy = (meet["createdBy"] as? Map<String, Any>)?.get("id") == otherUserId
                        createdWith || createdBy
                    }

                    firestore.runBatch { batch ->
                        val userDocRef = firestore.collection("users").document(currentUserId)

                        batch.update(userDocRef, "blockedUserIds", newBlockedUserIds)

                        for (meeting in meetingsToDelete) {
                            batch.update(userDocRef, "scheduledMeets", FieldValue.arrayRemove(meeting))

                            val otherUserDocRef = firestore.collection("users").document(otherUserId)
                            batch.update(otherUserDocRef, "scheduledMeets", FieldValue.arrayRemove(meeting))
                        }
                    }.addOnSuccessListener {
                        onResult(true, null)
                        Log.d("BlockUser", "Successfully blocked $otherUserId and deleted related meetings.")
                    }.addOnFailureListener { e ->
                        onResult(false, e.localizedMessage)
                        Log.e("BlockUser", "Failed to block $otherUserId or delete meetings: ${e.message}")
                    }
                } else {
                    firestore.collection("users").document(currentUserId)
                        .set(mapOf("blockedUserIds" to listOf(otherUserId)))
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d("BlockUser", "Successfully blocked $otherUserId (user document created).")
                        }.addOnFailureListener { e ->
                            onResult(false, e.localizedMessage)
                            Log.e("BlockUser", "Failed to block $otherUserId: ${e.message}")
                        }
                }
            }.addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("BlockUser", "Failed to retrieve user document: $currentUserId", e)
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

    fun muteUser(
        currentUserId: String,
        otherUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIds =
                        document.get("mutedUserIds") as? List<Map<String, Any>> ?: emptyList()
                    val newUserIds = userIds + otherUserId

                    firestore.collection("users").document(currentUserId)
                        .update("mutedUserIds", newUserIds)
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d("MuteUser", "Successfully muted $otherUserId")
                        }.addOnFailureListener { e ->
                            onResult(false, e.localizedMessage)
                            Log.d("MuteUser", "Failed to mute $otherUserId")
                        }
                } else {
                    firestore.collection("users").document(currentUserId)
                        .set(mapOf("mutedUserIds" to listOf(otherUserId)))
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d("MuteUser", "Successfully muted $otherUserId")
                        }.addOnFailureListener { e ->
                            onResult(false, e.localizedMessage)
                            Log.d("MuteUser", "Failed to mute $otherUserId")
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("MuteUser", "Error retrieving user document: $currentUserId", e)
            }
    }

    fun unmuteUser(
        currentUserId: String,
        otherUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIds = document.get("mutedUserIds") as? List<String> ?: emptyList()
                    if (userIds.contains(otherUserId)) {
                        val newUserIds = userIds.filterNot { it == otherUserId }

                        firestore.collection("users").document(currentUserId)
                            .update("mutedUserIds", newUserIds)
                            .addOnSuccessListener {
                                onResult(true, null)
                                Log.d("MuteUser", "Successfully unmuted $otherUserId")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.localizedMessage)
                                Log.e("MuteUser", "Failed to unmute $otherUserId: ${e.message}")
                            }
                    } else {
                        onResult(false, "User is not muted")
                        Log.e("MuteUser", "User $otherUserId is not muted")
                    }
                } else {
                    onResult(false, "User not found")
                    Log.e("MuteUser", "User document not found for ID: $currentUserId")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("BlockUser", "Error retrieving user document: $currentUserId", e)
            }
    }

    fun fetchBlockedAndMutedData(
        currentUserId: String,
        otherUserId: String,
        onResult: (isBlocked: Boolean, isMuted: Boolean) -> Unit
    ) {
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val blockedUserIds = snapshot.get("blockedUserIds") as? List<String>
                    val mutedUserIds = snapshot.get("mutedUserIds") as? List<String>
                    val isBlocked = blockedUserIds?.contains(otherUserId) == true
                    val isMuted = mutedUserIds?.contains(otherUserId) == true
                    onResult(isBlocked, isMuted)
                } else {
                    onResult(false, false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FetchUserData", "Error fetching user data", e)
                onResult(false, false)
            }
    }

    fun logout() {
        val currentUserId = currentUserId() ?: return

        notificationRepository.getFCMToken { token ->
            Log.d("FCM-Token", "token: $token")
            token?.let {
                notificationRepository.removeFcmToken(currentUserId, it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FCM-Token", "FCM token removed successfully.")
                        } else {
                            Log.e(
                                "FCM-Token",
                                "Failed to remove FCM token: ${task.exception?.message}"
                            )
                        }
                        FirebaseAuth.getInstance().signOut()
                        Log.d("FCM-Token", "User logged out.")
                    }
            } ?: run {
                Log.d("FCM-Token", "FCM Token is null. Logging out user.")
                FirebaseAuth.getInstance().signOut()
                Log.d("FCM-Token", "User logged out.")
            }
        }
    }


    fun deleteAccount(onResult: (Boolean) -> Unit) {
        val currentUserId = currentUserId()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUserId == null || currentUser == null) {
            Log.e("UserRepository", "Cannot delete account: User is not authenticated.")
            onResult(false)
            return
        }

        firestore.collection("users").document(currentUserId)
            .delete()
            .addOnSuccessListener {
                Log.d("UserRepository", "User data deleted successfully for $currentUserId.")

                currentUser.delete()
                    .addOnSuccessListener {
                        Log.d("UserRepository", "User account deleted successfully.")

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
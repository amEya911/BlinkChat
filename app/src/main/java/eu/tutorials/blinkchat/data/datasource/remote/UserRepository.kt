package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.util.HashUtil
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun addUserDetails(contact: Contact) {
        firestore.collection("users").document(contact.id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("UserRepository", "User already exists: ${contact.displayName}")
                } else {
                    firestore.collection("users").document(contact.id)
                        .set(contact)
                        .addOnSuccessListener {
                            Log.d("UserRepository", "User details added for ${contact.displayName}")
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
        otherUserId: String
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
                            Log.d("BlockUser", "Successfully blocked $otherUserId")
                        }.addOnFailureListener {
                            Log.d("BlockUser", "Failed to block $otherUserId")
                        }
                } else {
                    firestore.collection("users").document(currentUserId)
                        .set(mapOf("blockedUserIds" to listOf(otherUserId)))
                        .addOnSuccessListener {
                            Log.d("BlockUser", "Successfully blocked $otherUserId")
                        }.addOnFailureListener {
                            Log.d("BlockUser", "Failed to block $otherUserId")
                        }
                }

            }.addOnFailureListener { e ->
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
        otherUserId: String
    ) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIds = document.get("blockedUserIds") as? List<String> ?: emptyList()
                    val newUserIds = userIds.filterNot { userId -> userId == otherUserId }

                    firestore.collection("users").document(currentUserId)
                        .update("blockedUserIds", newUserIds)
                        .addOnSuccessListener {
                            Log.d("BlockUser", "Successfully unblocked $otherUserId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BlockUser", "Failed to unblock $otherUserId: ${e.message}")
                        }
                } else {
                    Log.e("BlockUser", "User document not found for ID: $currentUserId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("BlockUser", "Error retrieving user document: $currentUserId", e)
            }
    }

}




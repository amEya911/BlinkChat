package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.ui.component.HashUtil
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

    fun listenToRecentChats(currentUserId: String, onResult: (List<Contact>) -> Unit) {
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    Log.e("listenToRecentChats", "Error listening for updates: ${exception.message}", exception)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val recentChats = document["recentChats"] as? List<Map<String, Any>> ?: emptyList()

                    val contacts = recentChats.mapNotNull { chat ->
                        val displayName = chat["displayName"] as? String
                        val lastUpdated = chat["lastUpdated"] as? Long
                        val photoUri = chat["photoUri"] as? String
                        val userId = chat["userId"] as? String

                        if (userId != null && displayName != null && lastUpdated != null) {
                            Contact(
                                id = userId,
                                displayName = displayName,
                                phoneNumber = "",
                                photoUri = photoUri,
                                photoThumbnailUri = null
                            )
                        } else {
                            null
                        }
                    }

                    onResult(contacts)
                } else {
                    Log.w("listenToRecentChats", "Document does not exist or has no recentChats field.")
                    onResult(emptyList())
                }
            }
    }

}




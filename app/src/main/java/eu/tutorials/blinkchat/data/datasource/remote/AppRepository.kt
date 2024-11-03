package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun createChatRoom(
        currentUserId: String,
        currentUserName: String,
        otherUserId: String,
        otherUserName: String,
        callback: (String?) -> Unit
    ) {
        val chatRoomId = UUID.randomUUID().toString()
        val chatRoomData = mapOf(
            "chatRoomId" to chatRoomId,
            "participantIds" to listOf(currentUserId, otherUserId) ,
            "participantUserNames" to listOf(currentUserName, otherUserName),
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
}


package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.android.gms.tasks.Task
import retrofit2.HttpException
import java.io.IOException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import eu.tutorials.blinkchat.data.datasource.local.notification.FcmApi
import eu.tutorials.blinkchat.util.NotificationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fcmApi: FcmApi
) {
    fun getFCMToken(onTokenReceived: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { task ->
            if (task.isNotEmpty()) {
                Log.d("FCM-Token", task)
                onTokenReceived(task)
            } else {
                Log.d("FCM-Token", "Error")
                onTokenReceived(null)
            }
        }
    }

    fun addFcmToken(userId: String, token: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener {
                Log.d("FCM-Token", "FCM token added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FCM-Token", "Failed to add FCM token: ${e.message}")
            }
    }

    fun removeFcmToken(userId: String, token: String): Task<Void> {
        val userDoc = firestore.collection("users").document(userId)
        return userDoc.update("fcmTokens", FieldValue.arrayRemove(token))
            .addOnSuccessListener {
                Log.d("FCM-Token", "FCM token removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FCM-Token", "Failed to remove FCM token: ${e.message}")
            }
    }


    fun getFcmTokens(userId: String, onResult: (List<String>) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val tokens = document.get("fcmTokens") as? List<String> ?: emptyList()
                onResult(tokens)
            }
            .addOnFailureListener { e ->
                Log.e("FCM-Token", "Failed to retrieve FCM tokens: ${e.message}")
                onResult(emptyList())
            }
    }

    fun updateFcmToken(userId: String, oldToken: String, newToken: String) {
        removeFcmToken(userId, oldToken)
        addFcmToken(userId, newToken)
    }

    fun notifyOtherUser(
        currentUserId: String,
        otherUserId: String,
        title: String,
        body: String
    ) {
        getFcmTokens(otherUserId) { tokens ->
            Log.d("NotificationSender1", "Tokens for user $otherUserId: $tokens")
            // Start a coroutine to send notifications
            CoroutineScope(Dispatchers.IO).launch {
                tokens.forEach { token ->
                    try {
                        sendNotification(
                            to = token,
                            title = title,
                            body = body
                        )
                    } catch (e: Exception) {
                        Log.e("NotificationSender1", "Failed to send notification: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun sendNotification(to: String, title: String, body: String) {
        try {
            // Directly call sendNotification with the necessary parameters
            NotificationSender.sendNotification(
                targetToken = to,
                title = title,
                body = body
            )
        } catch (e: HttpException) {
            Log.e("NotificationSender1", "HTTP Exception: ${e.message}")
        } catch (e: IOException) {
            Log.e("NotificationSender1", "IO Exception: ${e.message}")
        }
    }

}
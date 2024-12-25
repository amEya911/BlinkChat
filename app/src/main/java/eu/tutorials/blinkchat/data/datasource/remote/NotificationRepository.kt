package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import eu.tutorials.blinkchat.data.datasource.local.notification.FcmApi
import eu.tutorials.blinkchat.data.datasource.local.notification.NotificationBody
import eu.tutorials.blinkchat.data.datasource.local.notification.SendMessageDto
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fcmApi: FcmApi
) {
    fun getFCMToken(onTokenReceived: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { task ->
            if (task.isNotEmpty()) {
                Log.d("FCM Message", task)
                onTokenReceived(task)
            } else {
                Log.d("FCM Message", "Error")
                onTokenReceived(null)
            }
        }
    }

    fun addFcmToken(userId: String, token: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener {
                Log.d("NotificationRepo", "FCM token added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepo", "Failed to add FCM token: ${e.message}")
            }
    }

    fun removeFcmToken(userId: String, token: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.update("fcmTokens", FieldValue.arrayRemove(token))
            .addOnSuccessListener {
                Log.d("NotificationRepo", "FCM token removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationRepo", "Failed to remove FCM token: ${e.message}")
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
                Log.e("NotificationRepo", "Failed to retrieve FCM tokens: ${e.message}")
                onResult(emptyList())
            }
    }

    fun updateFcmToken(userId: String, oldToken: String, newToken: String) {
        removeFcmToken(userId, oldToken)
        addFcmToken(userId, newToken)
    }

    suspend fun sendNotification(to: String, title: String, body: String) {
        val messageDto = SendMessageDto(
            to = to,
            notification = NotificationBody(
                title = title,
                body = body
            )
        )

        try {
            fcmApi.sendMessage(messageDto)
            Log.d("NotificationRepo", "Notification sent successfully to $to")
        } catch (e: HttpException) {
            Log.e(
                "NotificationRepo",
                "HTTP exception while sending notification: ${e.response()?.errorBody()?.string()} | Code: ${e.code()}"
            )
        } catch (e: IOException) {
            Log.e("NotificationRepo", "Network issue while sending notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Unexpected error while sending notification: ${e.message}")
        }
    }


    suspend fun broadcastNotification(title: String, body: String) {
        val messageDto = SendMessageDto(
            to = null,
            notification = NotificationBody(
                title = title,
                body = body
            )
        )

        try {
            fcmApi.broadcast(messageDto)
            Log.d("NotificationRepo", "Broadcast notification sent successfully")
        } catch (e: HttpException) {
            Log.e("NotificationRepo", "Failed to broadcast notification: ${e.message()}")
        } catch (e: IOException) {
            Log.e("NotificationRepo", "Failed to broadcast notification due to network issue: ${e.message}")
        }
    }
}
package eu.tutorials.blinkchat.util

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject

object AccessToken {

    private const val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken(): String? {
        try {
            val jsonString = Ids.ACCESS_TOKEN

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))

            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        } catch (e : IOException) {
            return null
        }
    }
}

object NotificationSender {

    private const val FCM_URL = Ids.FCM_URL

    fun sendNotification(
        targetToken: String,
        title: String,
        body: String,
        deepLink: String?,
        data: Map<String, String> = emptyMap()
    ) {
        val accessToken = AccessToken.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            Log.e("NotificationSender1", "Access token is null or empty.")
            return
        }

//        val message = JSONObject().apply {
//            put("token", targetToken)
//            put("notification", JSONObject().apply {
//                put("title", title)
//                put("body", body)
//            })
//            if (data.isNotEmpty()) {
//                put("data", JSONObject(data))
//            }
//        }

        val message = JSONObject().apply {
            put("token", targetToken)
            val dataPayload = JSONObject().apply {
                put("title", title)
                put("body", body)
                put("deep_link", deepLink)
                Log.d("Notif1", "deepLink2: $deepLink")
                data.forEach { (key, value) ->
                    put(key, value)
                }
            }
            put("data", dataPayload)
        }

        Log.d("NotificationSender1", "message: $message")

        val payload = JSONObject().apply {
            put("message", message)
        }

        Log.d("NotificationSender1", "payload: $payload")

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            payload.toString()
        )

        val request = Request.Builder()
            .url(FCM_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationSender1", "Failed to send notification: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NotificationSender1", "Notification sent successfully.")
                } else {
                    Log.e("NotificationSender1", "Error sending notification: ${response.code}")
                }
            }
        })
    }
}

sealed class NotificationType(val type: String) {
    data object ScheduleMeet: NotificationType("schedule-meet")
    data object RescheduleMeet: NotificationType("reschedule-meet")
    data object DeleteMeet: NotificationType("delete-meet")
    data object CreateRoom: NotificationType("create-room")
}
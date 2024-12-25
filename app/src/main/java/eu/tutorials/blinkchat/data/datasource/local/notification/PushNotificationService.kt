package eu.tutorials.blinkchat.data.datasource.local.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import eu.tutorials.blinkchat.data.datasource.remote.NotificationRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService: FirebaseMessagingService() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var notificationRepository: NotificationRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            // Add the new token to Firestore
            notificationRepository.addFcmToken(currentUserId, token)
            Log.d("PushNotificationService", "New FCM Token added for user: $currentUserId")
        } else {
            Log.e("PushNotificationService", "User is not logged in, unable to update token")
        }

        //Update server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Respond to received messages
    }
}
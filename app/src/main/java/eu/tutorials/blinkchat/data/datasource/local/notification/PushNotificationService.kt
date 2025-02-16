package eu.tutorials.blinkchat.data.datasource.local.notification

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.NotificationsTypePreferences
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.NotificationRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.NotificationsType
import eu.tutorials.blinkchat.util.NotificationType
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class PushNotificationService: FirebaseMessagingService() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var localRepository: LocalRepository
    @Inject lateinit var appLifecycleObserver: AppLifecycleObserver
    @Inject lateinit var notificationsTypePreferences: NotificationsTypePreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            notificationRepository.addFcmToken(currentUserId, token)
            Log.d("PushNotificationService", "New FCM Token added for user: $currentUserId")
        } else {
            Log.e("PushNotificationService", "User is not logged in, unable to update token")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val userId = message.data["body"]
        val type = message.data["type"]
        val displayName = getDisplayNameFromLocalContact(userId)
        val modifiedBody = when (notificationsTypePreferences.loadNotificationsType()) {
            NotificationsType.PUBLIC -> displayName ?: "Someone"
            NotificationsType.PRIVATE -> "Someone"
        }

        val body = when(type) {
            NotificationType.ScheduleMeet.type -> "$modifiedBody has scheduled a meet with you"
            NotificationType.RescheduleMeet.type -> "$modifiedBody has rescheduled a meet with you"
            NotificationType.DeleteMeet.type -> "$modifiedBody has deleted a meet with you"
            NotificationType.CreateRoom.type -> "$modifiedBody has created a room with you"
            else -> "You have a new notification"
        }

        Log.d("NotificationSender1", "Modified message received: $modifiedBody")

        val deepLink = message.data["deep_link"] ?: "https://vanishtest.netlify.app"
        Log.d("ameyak", "deeplink: $deepLink")

        if (appLifecycleObserver.isAppInForeground) {
            sendNotification(body, deepLink)
        } else {
            sendNotification(body, deepLink)
        }
    }

    private fun sendNotification(messageBody: String, deepLink: String) {
        createNotificationChannel()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "your_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("New Message")
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt() + Random.nextInt(0, 1000)

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Channel for notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("your_channel_id", name, importance)
            channel.description = descriptionText

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getDisplayNameFromLocalContact(userId: String?): String? {
        if (userId == null) return null

        return try {
            val contact = runBlocking {
                localRepository.getContactById(userId)
            }
            contact?.displayName
        } catch (e: Exception) {
            Log.e("PushNotificationService", "Error retrieving display name for userId: $userId", e)
            null
        }
    }
}

class AppLifecycleObserver @Inject constructor(): Application.ActivityLifecycleCallbacks {
    var isAppInForeground = false

    override fun onActivityResumed(activity: Activity) {
        isAppInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStopped(activity: Activity) {}
}

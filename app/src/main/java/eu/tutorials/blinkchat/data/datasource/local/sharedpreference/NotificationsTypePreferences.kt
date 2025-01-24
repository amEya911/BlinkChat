package eu.tutorials.blinkchat.data.datasource.local.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import eu.tutorials.blinkchat.data.event.app.NotificationsType

class NotificationsTypePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "notificationsType_preferences"
        private const val KEY_SELECTED_NOTIFICATIONS_TYPE = "selected_notificationsType"
    }

    fun saveNotificationsType(notificationsType: NotificationsType) {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_NOTIFICATIONS_TYPE, notificationsType.name)
            .apply()
    }

    fun loadNotificationsType(): NotificationsType {
        val notificationsTypeName = sharedPreferences.getString(KEY_SELECTED_NOTIFICATIONS_TYPE, NotificationsType.PUBLIC.name)
        return NotificationsType.valueOf(notificationsTypeName ?: NotificationsType.PUBLIC.name)
    }
}
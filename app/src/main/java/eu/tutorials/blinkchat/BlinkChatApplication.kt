package eu.tutorials.blinkchat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import eu.tutorials.blinkchat.data.datasource.local.notification.AppLifecycleObserver

@HiltAndroidApp
class BlinkChatApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val appLifecycleObserver = AppLifecycleObserver()
        registerActivityLifecycleCallbacks(appLifecycleObserver)
    }
}
package eu.tutorials.blinkchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import eu.tutorials.blinkchat.navigation.RootNavGraph
import eu.tutorials.blinkchat.ui.theme.BlinkChatTheme
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.ThemePreferences
import eu.tutorials.blinkchat.data.event.app.AppTheme
import eu.tutorials.blinkchat.navigation.AppScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
        val deepLinkPath = appLinkData?.path

        val isFromDeepLink = deepLinkPath == "/${AppScreen.Meetings.route}/enter"
        Log.d("jatins", "deepLinkPath: $deepLinkPath")

        setContent {
            val themePreferences = ThemePreferences(applicationContext)
            val savedTheme = themePreferences.loadTheme()
            val darkTheme = when (savedTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            BlinkChatTheme(darkTheme = darkTheme) {
                App(isFromDeepLink)
            }
        }


    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(isFromDeepLink: Boolean) {
    RootNavGraph(isFromDeepLink)
}





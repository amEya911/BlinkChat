package eu.tutorials.blinkchat

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import eu.tutorials.blinkchat.navigation.RootNavGraph
import eu.tutorials.blinkchat.ui.theme.BlinkChatTheme
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlinkChatTheme {
                App()
            }
        }
        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    RootNavGraph()
}





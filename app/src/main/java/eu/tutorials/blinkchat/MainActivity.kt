package eu.tutorials.blinkchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import eu.tutorials.blinkchat.navigation.RootNavGraph
import eu.tutorials.blinkchat.ui.theme.BlinkChatTheme

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
    }
}

@Composable
fun App() {
    RootNavGraph()
}


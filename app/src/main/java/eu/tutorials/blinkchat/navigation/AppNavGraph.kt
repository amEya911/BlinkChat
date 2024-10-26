package eu.tutorials.blinkchat.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.blinkchat.ui.screen.Inbox
import eu.tutorials.blinkchat.ui.screen.Meetings
import eu.tutorials.blinkchat.ui.screen.Settings
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextColor
import androidx.compose.ui.unit.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = BackgroundColor,
        contentColor = TextColor,
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = AppScreen.Chats.route,
            route = Graph.APP
        ) {
            composable(AppScreen.Meetings.route) {
                Meetings()
            }

            composable(AppScreen.Chats.route) {
                Inbox()
            }

            composable(AppScreen.Settings.route) {
                Settings()
            }
        }
    }
}



sealed class AppScreen(val route: String) {
    object Meetings : AppScreen("meetings")
    object Chats : AppScreen("chats")
    object Settings : AppScreen("settings")
}

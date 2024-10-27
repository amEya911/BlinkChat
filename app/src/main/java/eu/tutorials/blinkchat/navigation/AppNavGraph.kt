package eu.tutorials.blinkchat.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.blinkchat.ui.screen.Inbox
import eu.tutorials.blinkchat.ui.screen.Meetings
import eu.tutorials.blinkchat.ui.screen.Settings
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextColor
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)

    val inboxViewModel: InboxViewModel = hiltViewModel()
    val inboxState = inboxViewModel.inboxState.collectAsState().value

    Scaffold(
        containerColor = BackgroundColor,
        contentColor = TextColor,
        topBar = {
            AppBar(
                title = when (currentRoute) {
                    AppScreen.Meetings.route -> "Meets"
                    AppScreen.Chats.route -> "Chats"
                    AppScreen.Settings.route -> "Settings"
                    else -> "Error"
                },
                showIcon = currentRoute != AppScreen.Settings.route,
                onIconClick = {
                    when (currentRoute) {
                        AppScreen.Meetings.route -> {}
                        AppScreen.Chats.route -> {
                            inboxViewModel.onEvent(InboxEvent.OnAllContactsIconClicked)
                        }

                        AppScreen.Settings.route -> {}
                    }
                },
                iconResId = if (currentRoute == AppScreen.Meetings.route) R.drawable.calendar else Icons.Default.AccountCircle
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Chats.route,
            route = Graph.APP
        ) {
            composable(AppScreen.Meetings.route) {
                Meetings(modifier = Modifier.padding(paddingValues))
            }

            composable(AppScreen.Chats.route) {
                Inbox(
                    inboxState = inboxState,
                    onEvent = inboxViewModel::onEvent,
                    modifier = Modifier.padding(paddingValues)
                )
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

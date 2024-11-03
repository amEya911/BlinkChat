package eu.tutorials.blinkchat.navigation

import android.content.Intent
import android.util.Log
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
import eu.tutorials.blinkchat.ui.screen.app.Inbox
import eu.tutorials.blinkchat.ui.screen.app.Meetings
import eu.tutorials.blinkchat.ui.screen.app.Settings
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.gson.Gson
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.ContactModel
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.screen.app.ChatRoom
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)

    Log.d("AppNavGraph", "current root: $currentRoute")

    val inboxViewModel: InboxViewModel = hiltViewModel()
    val inboxState = inboxViewModel.inboxState.collectAsState().value

    Scaffold(
        containerColor = BackgroundColor,
        contentColor = TextColor,
        topBar = {
            if (currentRoute != null && !currentRoute.startsWith(AppScreen.ChatRoom.route)) {
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
            }
        },
        bottomBar = {
            if (currentRoute != null && !currentRoute.startsWith(AppScreen.ChatRoom.route)) {
                BottomNavBar(navController = navController)
            }
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
                    modifier = Modifier.padding(paddingValues),
                    onStartChatWithContact = { chatRoomId ->
                        navController.navigate("${AppScreen.ChatRoom.route}/$chatRoomId")
                    }
                )
            }

            composable(AppScreen.Settings.route) {
                Settings()
            }

            composable(
                route = "${AppScreen.ChatRoom.route}/{chatRoomId}",
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://vanishtest.netlify.app/{chatRoomId}"
                        action = Intent.ACTION_VIEW
                    }
                ),
                arguments = listOf(
                    navArgument("chatRoomId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")
                ChatRoom(chatRoomId = chatRoomId!!, contact = inboxState.selectedContact!!)
            }
        }
    }
}


sealed class AppScreen(val route: String) {
    object Meetings : AppScreen("meetings")
    object Chats : AppScreen("chats")
    object Settings : AppScreen("settings")
    object ChatRoom : AppScreen("chat-room")
}

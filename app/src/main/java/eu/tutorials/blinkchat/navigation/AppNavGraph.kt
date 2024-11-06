package eu.tutorials.blinkchat.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.screen.app.ChatRoom
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = BackgroundColor)
    systemUiController.setNavigationBarColor(color = TextFieldColor)

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
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
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
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")

                if (chatRoomId != null) {
                    ChatRoom(chatRoomId = chatRoomId)
                    Log.e("AppNavGraph", "succesful $chatRoomId")
                } else {
                    Log.e("AppNavGraph", "chatRoomId is null")
                }
            }
        }
    }
}

sealed class AppScreen(val route: String) {
    data object Meetings : AppScreen("meetings")
    data object Chats : AppScreen("chats")
    data object Settings : AppScreen("settings")
    data object ChatRoom : AppScreen("chat-room")
}

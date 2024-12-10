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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.event.SettingsEvent
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.screen.app.AddBlockUsers
import eu.tutorials.blinkchat.ui.screen.app.BlockedUsers
import eu.tutorials.blinkchat.ui.screen.app.ChatRoom
import eu.tutorials.blinkchat.ui.theme.LightGray
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
import eu.tutorials.blinkchat.ui.viewmodel.ChatRoomViewModel
import eu.tutorials.blinkchat.ui.viewmodel.InboxViewModel
import eu.tutorials.blinkchat.ui.viewmodel.MeetingsViewModel
import eu.tutorials.blinkchat.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)
    Log.d("Timepass", "currentRoute: $currentRoute")

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(currentRoute) {
        currentRoute?.let {
            if (it.startsWith(AppScreen.ChatRoom.route)) {
                systemUiController.setSystemBarsColor(color = LightGray)
            } else {
                systemUiController.setSystemBarsColor(color = BackgroundColor)
                systemUiController.setNavigationBarColor(color = TextFieldColor)
            }
        }
    }

    Log.d("AppNavGraph", "current root: $currentRoute")

    val inboxViewModel: InboxViewModel = hiltViewModel()
    val inboxState = inboxViewModel.inboxState.collectAsState().value

    val meetingsViewModel: MeetingsViewModel = hiltViewModel()
    val meetingsState = meetingsViewModel.meetingsState.collectAsState().value

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState = settingsViewModel.settingsState.collectAsState().value

    val chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value

    Scaffold(
        containerColor = BackgroundColor,
        contentColor = TextColor,
        bottomBar = {
            if (currentRoute != null &&
                !currentRoute.startsWith(AppScreen.ChatRoom.route) &&
                !currentRoute.startsWith(AppScreen.BlockedUsers.route) &&
                !currentRoute.startsWith(AppScreen.AddBlockUsers.route)
            ) {
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
                Meetings(
                    modifier = Modifier.padding(paddingValues),
                    meetingsState = meetingsState,
                    onEvent = meetingsViewModel::onEvent
                )
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
                Settings(
                    modifier = Modifier.padding(paddingValues),
                    settingsState = settingsState,
                    onEvent = settingsViewModel::onEvent,
                    onBlockUserClicked = { navController.navigate(AppScreen.BlockedUsers.route) },
                    logout = {
                        navController.navigate(Graph.AUTH) {
                            popUpTo(Graph.APP) { inclusive = true }
                        }
                    }
                )
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
                    Log.d("AppNavGraph", "successful $chatRoomId")
                } else {
                    Log.e("AppNavGraph", "chatRoomId is null")
                }
            }

            composable(AppScreen.BlockedUsers.route) {
                BlockedUsers(
                    modifier = Modifier.padding(paddingValues),
                    blockedUsers = settingsState.blockedUsers,
                    onBackClicked = { navController.popBackStack() },
                    onUnblockClicked = { otherUserId ->
                        inboxViewModel.onEvent(InboxEvent.OnUnblockUser(otherUserId = otherUserId))
                        navController.popBackStack()
                    },
                    onAddBlockUsers = {
                        navController.popBackStack()
                        navController.navigate(AppScreen.AddBlockUsers.route)
                    }
                )
            }

            composable(AppScreen.AddBlockUsers.route) {
                AddBlockUsers(
                    modifier = Modifier.padding(paddingValues),
                    contacts = inboxState.contacts.filterNot { contact ->
                        settingsState.blockedUsers.any { blockedUser -> blockedUser.id == contact.id }
                    },
                    onBackClicked = {navController.popBackStack()},
                    onBlockUser = { userId ->
                        inboxViewModel.onEvent(InboxEvent.OnBlockUser(userId))
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class AppScreen(val route: String) {
    data object Meetings : AppScreen("meetings")
    data object Chats : AppScreen("chats")
    data object Settings : AppScreen("settings")
    data object ChatRoom : AppScreen("chat-room")
    data object BlockedUsers : AppScreen("blocked-users")
    data object AddBlockUsers: AppScreen("add-block-users")
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    return navBackStackEntry?.destination?.route
}



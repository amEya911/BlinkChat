package eu.tutorials.blinkchat.navigation

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.blinkchat.ui.screen.app.Inbox
import eu.tutorials.blinkchat.ui.screen.app.Meetings
import eu.tutorials.blinkchat.ui.screen.app.Settings
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.event.app.MeetingsEvent
import eu.tutorials.blinkchat.ui.screen.additional.AddBlockUsers
import eu.tutorials.blinkchat.ui.screen.additional.BlockedUsers
import eu.tutorials.blinkchat.ui.screen.additional.Profile
import eu.tutorials.blinkchat.ui.screen.additional.ScheduleAMeet
import eu.tutorials.blinkchat.ui.viewmodel.additional.AddBlockUsersViewModel
import eu.tutorials.blinkchat.ui.viewmodel.additional.BlockedUsersViewModel
import eu.tutorials.blinkchat.ui.viewmodel.additional.ProfileViewModel
import eu.tutorials.blinkchat.ui.viewmodel.app.ChatRoomViewModel
import eu.tutorials.blinkchat.ui.viewmodel.app.InboxViewModel
import eu.tutorials.blinkchat.ui.viewmodel.app.MeetingsViewModel
import eu.tutorials.blinkchat.ui.viewmodel.additional.ScheduleAMeetViewModel
import eu.tutorials.blinkchat.ui.viewmodel.app.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    isFromDeepLink: Boolean
) {
    Log.d("jatins", "isFromDeepLink: $isFromDeepLink")
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)
    val snackbarHostState = remember { SnackbarHostState() }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MaterialTheme.colorScheme.background)
    systemUiController.setNavigationBarColor(color = MaterialTheme.colorScheme.secondaryContainer)

    val inboxViewModel: InboxViewModel = hiltViewModel()
    val inboxState = inboxViewModel.inboxState.collectAsState().value

    val meetingsViewModel: MeetingsViewModel = hiltViewModel()
    val meetingsState = meetingsViewModel.meetingsState.collectAsState().value

    val scheduleAMeetViewModel: ScheduleAMeetViewModel = hiltViewModel()
    val scheduleAMeetState = scheduleAMeetViewModel.scheduleAMeetState.collectAsState().value

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState = settingsViewModel.settingsState.collectAsState().value

    val blockedUsersViewModel: BlockedUsersViewModel = hiltViewModel()
    val blockedUsersState = blockedUsersViewModel.blockedUsersState.collectAsState().value

    val addBlockUsersViewModel: AddBlockUsersViewModel = hiltViewModel()
    val addBlockUsersState = addBlockUsersViewModel.addBlockUsersState.collectAsState().value

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState = profileViewModel.profileState.collectAsState().value

    val chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
    val chatRoomState = chatRoomViewModel.chatRoomState.collectAsState().value

    LaunchedEffect(inboxState.snackbarMessage) {
        inboxState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            inboxViewModel.onEvent(InboxEvent.OnSnackbarDisplayed)
        }
    }

    LaunchedEffect(meetingsState.snackbarMessage) {
        Log.d("Snack", "${meetingsState.snackbarMessage}")
        meetingsState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            meetingsViewModel.onEvent(MeetingsEvent.OnSnackbarDisplayed)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (currentRoute != null &&
                !currentRoute.startsWith(AppScreen.ChatRoom.route) &&
                !currentRoute.startsWith(AppScreen.BlockedUsers.route) &&
                !currentRoute.startsWith(AppScreen.AddBlockUsers.route) &&
                !currentRoute.startsWith(AppScreen.ScheduleAMeet.route) &&
                !currentRoute.startsWith(AppScreen.Profile.route)
            ) {
                BottomNavBar(navController = navController)
            }
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isFromDeepLink) AppScreen.Meetings.route else AppScreen.Chats.route,
            route = Graph.APP
        ) {
            composable(
                AppScreen.Meetings.route,
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(1000)
                    )
                },
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(1000)
                    )
                }
            ) {
                Meetings(
                    modifier = Modifier.padding(paddingValues),
                    meetingsState = meetingsState,
                    onEvent = meetingsViewModel::onEvent,
                    onAddClicked = { navController.navigate(AppScreen.ScheduleAMeet.route) },
                    onBackPressed = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(AppScreen.Chats.route) { popUpTo(AppScreen.Chats.route) { inclusive = true } }
                        }
                    }
                )
            }

            composable(
                AppScreen.ScheduleAMeet.route,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        tween(1000)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        tween(1000)
                    )
                },
            ) {
                ScheduleAMeet(
                    contacts = inboxState.contacts,
                    onBackClicked = { navController.popBackStack() },
                    onScheduleConfirmed = { contact, date, time ->
                        inboxViewModel.onEvent(InboxEvent.OnScheduleConfirmed(contact, date, time))
                    },
                    scheduleAMeetState = scheduleAMeetState,
                    onEvent = scheduleAMeetViewModel::onEvent
                )
            }

            composable(
                AppScreen.Chats.route
            ) {
                Inbox(
                    inboxState = inboxState,
                    onEvent = inboxViewModel::onEvent,
                    modifier = Modifier.padding(paddingValues),
                    onStartChatWithContact = { chatRoomId ->
                        navHostController.navigate("${AppScreen.ChatRoom.route}/$chatRoomId")
                    },
                    inboxViewModel = inboxViewModel
                )
            }

            composable(
                AppScreen.Settings.route,
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(1000)
                    )
                },
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(1000)
                    )
                }
            ) {
                Settings(
                    modifier = Modifier.padding(paddingValues),
                    settingsState = settingsState,
                    onEvent = settingsViewModel::onEvent,
                    onBlockUserClicked = { navController.navigate(AppScreen.BlockedUsers.route) },
                    navigateToLoginScreen = {
                        navHostController.navigate(Graph.AUTH) {
                            popUpTo(Graph.APP) { inclusive = true }
                        }
                    },
                    onProfileClicked = { navController.navigate(AppScreen.Profile.route)}
                )
            }

            composable(
                AppScreen.Profile.route,
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(1000)
                    )
                },
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(1000)
                    )
                }
            ) {
                inboxState.currentUserContact?.let { contact ->
                    Profile(
                        modifier = Modifier.padding(paddingValues),
                        onBackClicked = { navController.popBackStack()},
                        currentUserId = contact.id,
                        profileState = profileState,
                        onEvent = profileViewModel::onEvent
                    )
                }
            }

            composable(
                AppScreen.BlockedUsers.route,
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(1000)
                    )
                },
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(1000)
                    )
                }
            ) {
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
                    },
                    blockedUsersState = blockedUsersState,
                    onEvent = blockedUsersViewModel::onEvent
                )
            }

            composable(
                AppScreen.AddBlockUsers.route,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        tween(1000)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        tween(1000)
                    )
                }
            ) {
                AddBlockUsers(
                    modifier = Modifier.padding(paddingValues),
                    contacts = inboxState.contacts.filterNot { contact ->
                        settingsState.blockedUsers.any { blockedUser -> blockedUser.id == contact.id }
                    },
                    onBackClicked = { navController.popBackStack() },
                    onBlockUser = { userId ->
                        inboxViewModel.onEvent(InboxEvent.OnBlockUser(userId))
                        navController.popBackStack()
                    },
                    addBlockUsersState = addBlockUsersState,
                    onEvent = addBlockUsersViewModel::onEvent
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
    data object AddBlockUsers : AppScreen("add-block-users")
    data object ScheduleAMeet : AppScreen("schedule-a-meet")
    data object Profile: AppScreen("profile")
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    return navBackStackEntry?.destination?.route
}



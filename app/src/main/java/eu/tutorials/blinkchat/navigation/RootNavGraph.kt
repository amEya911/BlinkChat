package eu.tutorials.blinkchat.navigation

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.blinkchat.ui.screen.app.ChatRoom
import eu.tutorials.blinkchat.ui.viewmodel.auth.LoginWithPhoneViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavGraph() {

    val navController = rememberNavController()
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Graph.APP
    } else {
        Graph.AUTH
    }
    val viewModel: LoginWithPhoneViewModel = hiltViewModel()
    val loginState = viewModel.loginWithPhoneState.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(
            navController,
            viewModel, loginState
        )
        composable(Graph.APP) {
            AppNavGraph(
                navController
            )
        }
        composable(
            route = "${AppScreen.ChatRoom.route}/{chatRoomId}?id={userId}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://vanishtest.netlify.app/{chatRoomId}?id={userId}"
                    action = Intent.ACTION_VIEW
                }
            ),
            arguments = listOf(
                navArgument("chatRoomId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")
            val userId = backStackEntry.arguments?.getString("userId")
            Log.d("Presence", "Root: $userId")

            if (chatRoomId != null) {
                ChatRoom(chatRoomId = chatRoomId, id = userId)
            } else {
                Log.e("AppNavGraph", "chatRoomId is null")
            }
        }
    }
}

object Graph {
    const val AUTH = "auth"
    const val APP = "app"
}


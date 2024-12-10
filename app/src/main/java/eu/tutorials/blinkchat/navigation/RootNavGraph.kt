package eu.tutorials.blinkchat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Graph.APP
    } else {
        Graph.AUTH
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(navController)
        composable(Graph.APP) {
            AppNavGraph()
        }
    }
}

object Graph {
    const val AUTH = "auth"
    const val APP = "app"
}
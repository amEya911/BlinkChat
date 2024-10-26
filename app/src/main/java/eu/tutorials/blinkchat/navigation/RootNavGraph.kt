package eu.tutorials.blinkchat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Graph.APP
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
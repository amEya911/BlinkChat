package eu.tutorials.blinkchat.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        val loda = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("loda", loda.toString())
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
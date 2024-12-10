package eu.tutorials.blinkchat.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eu.tutorials.blinkchat.ui.theme.TextFieldColor

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = currentRoute(navController)

    BottomNavigation(
        modifier = Modifier
            .wrapContentHeight(),
        backgroundColor = TextFieldColor
    ) {
        val items = listOf(
            BottomNavScreen.Meetings,
            BottomNavScreen.Chats,
            BottomNavScreen.Settings
        )

        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        if (currentRoute == screen.route) screen.selectedIcon else screen.unSelectedIcon,
                        contentDescription = null,
                        tint = if (currentRoute == screen.route) Color.Black else Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier.size(if (currentRoute == screen.route) 35.dp else 25.dp)
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                }
            )
        }
    }
}

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
) {
    data object Meetings: BottomNavScreen(
        route = AppScreen.Meetings.route,
        title = "Meets",
        selectedIcon = Icons.Filled.DateRange,
        unSelectedIcon = Icons.Outlined.DateRange
    )

    data object Chats: BottomNavScreen(
        route = AppScreen.Chats.route,
        title = "Chats",
        selectedIcon = Icons.Filled.Email,
        unSelectedIcon = Icons.Outlined.Email
    )

    data object Settings: BottomNavScreen(
        route = AppScreen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )
}




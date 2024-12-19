package eu.tutorials.blinkchat.ui.screen.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.data.event.SettingsEvent
import eu.tutorials.blinkchat.data.state.SettingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBlockUserClicked: () -> Unit,
    logout: () -> Unit
) {
    LaunchedEffect(key1 = settingsState.blockedUsers) {
        onEvent(SettingsEvent.OnLoadBlockedUsers)
    }
//    LaunchedEffect(key1 = settingsState.isLoggedIn) {
//        if (!settingsState.isLoggedIn) {
//            logout()
//        }
//    }
    Scaffold(
        topBar = {
            AppBar(
                title = "Settings",
                onIconClick = {}
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(
                onClick = {
                    onBlockUserClicked()
                },
                text = "Blocked",
                icon = Icons.Default.Block,
                count = settingsState.blockedUsers.size
            )

            ActionButton(
                onClick = {onEvent(SettingsEvent.Logout)},
                text = "Log Out",
                icon = Icons.Default.Logout
            )

            ActionButton(
                onClick = {},
                text = "Delete Account",
                icon = Icons.Default.Delete
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    onClick: () -> Unit = {},
    text: String = "Click Me",
    count: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = BackgroundColor,
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                count?.let { Text(text = "$count") }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Forward Arrow",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
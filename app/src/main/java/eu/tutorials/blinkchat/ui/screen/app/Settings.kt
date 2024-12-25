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
import eu.tutorials.blinkchat.data.event.app.SettingsEvent
import eu.tutorials.blinkchat.data.state.app.SettingsState
import eu.tutorials.blinkchat.ui.component.AppBar

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBlockUserClicked: () -> Unit,
    navigateToLoginScreen: () -> Unit
) {
    LaunchedEffect(key1 = settingsState.blockedUsers) {
        onEvent(SettingsEvent.OnLoadBlockedUsers)
    }

    LaunchedEffect(key1 = settingsState) {
        when {
            settingsState.isLoggedOut -> navigateToLoginScreen()
            settingsState.isAccountDeleted -> navigateToLoginScreen()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Settings",
                onIconClick = {}
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (settingsState.isLoading) {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            } else {
                ActionButton(
                    //modifier = modifier.padding(innerPadding),
                    onClick = {
                        onBlockUserClicked()
                    },
                    text = "Blocked",
                    icon = Icons.Default.Block,
                    count = settingsState.blockedUsers.size
                )

                ActionButton(
                    //modifier = modifier.padding(innerPadding),
                    onClick = {
                        onEvent(SettingsEvent.OnLogout)
                    },
                    text = "Log Out",
                    icon = Icons.Default.Logout
                )

                ActionButton(
                    //modifier = modifier.padding(innerPadding),
                    onClick = {
                        onEvent(SettingsEvent.OnDeleteAccount)
                    },
                    text = "Delete Account",
                    icon = Icons.Default.Delete
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit = {},
    text: String = "Click Me",
    count: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surfaceBright,
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
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                count?.let { Text(text = "$count" ,color = MaterialTheme.colorScheme.primary) }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Forward Arrow",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
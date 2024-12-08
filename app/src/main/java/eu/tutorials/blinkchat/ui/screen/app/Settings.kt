package eu.tutorials.blinkchat.ui.screen.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.event.SettingsEvent
import eu.tutorials.blinkchat.data.state.SettingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Settings",
                showIcon = false,
                onIconClick = {}
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->



        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(
                onClick = {
                    onEvent(SettingsEvent.OnDisplayBlockedUsers)
                }, buttonText = "Show Blocked Users"
            )

        }

        if (settingsState.isShowBlockedUsersClicked) {
            Show(settingsState = settingsState, onEvent = onEvent)
        }
    }
}

@Composable
fun ActionButton(onClick: () -> Unit = {}, buttonText: String = "Click Me") {
    OutlinedButton(modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary),
        onClick = { onClick() },
        content = {
            Text(text = buttonText, color = MaterialTheme.colorScheme.onPrimary)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Show(
    modifier: Modifier = Modifier,
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(SettingsEvent.OnDismissDisplayBlockedUsers) },
        content = {
            Box(
                modifier = Modifier.size(500.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn {
                    items(settingsState.blockedUsers) { user ->
                        Text(text = user.displayName)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    )
}
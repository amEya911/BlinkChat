package eu.tutorials.blinkchat.ui.screen.app

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.app.AppTheme
import eu.tutorials.blinkchat.data.event.app.NotificationsType
import eu.tutorials.blinkchat.data.event.app.SettingsEvent
import eu.tutorials.blinkchat.data.state.app.SettingsState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.rememberInternetConnectionState
import eu.tutorials.blinkchat.ui.component.settings.ActionButton
import eu.tutorials.blinkchat.ui.component.settings.AlertDialogSetting

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBlockUserClicked: () -> Unit,
    navigateToLoginScreen: () -> Unit
) {
    val activity = LocalContext.current as? Activity

    LaunchedEffect(key1 = settingsState.blockedUsers) {
        onEvent(SettingsEvent.OnLoadBlockedUsers)
    }

    LaunchedEffect(key1 = settingsState) {
        when {
            settingsState.isLoggedOut -> navigateToLoginScreen()
            settingsState.isAccountDeleted -> navigateToLoginScreen()
        }
    }

    HandleDialogEvents(
        settingsState = settingsState,
        onEvent = onEvent,
        activity = activity
    )

    Scaffold(
        topBar = {
            AppBar(
                title = "Settings",
                onIconClick = {},
                isOnline = rememberInternetConnectionState()
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
                    onClick = {
                        onBlockUserClicked()
                    },
                    text = "Blocked",
                    icon = Icons.Default.Block,
                    count = settingsState.blockedUsers.size
                )

                ActionButton(
                    onClick = {
                        onEvent(SettingsEvent.OnThemeClicked)
                    },
                    text = "Theme",
                    icon = painterResource(id = R.drawable.theme)
                )

                ActionButton(
                    onClick = {
                        onEvent(SettingsEvent.OnNotificationsClicked)
                    },
                    text = "Notifications",
                    icon = Icons.Default.Notifications
                )

                ActionButton(
                    onClick = {
                        onEvent(SettingsEvent.OnLogoutClicked)
                    },
                    text = "Log Out",
                    icon = Icons.Default.Logout
                )

                ActionButton(
                    onClick = {
                        onEvent(SettingsEvent.OnDeleteAccountClicked)
                    },
                    text = "Delete Account",
                    icon = Icons.Default.Delete
                )
            }
        }
    }
}

@Composable
fun HandleDialogEvents(
    settingsState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    activity: Activity?
) {
    if (settingsState.isLogoutClicked) {
        AlertDialogSetting(
            text = "Are you sure you want to LOGOUT?",
            onConfirm = {
                onEvent(SettingsEvent.OnLogoutClicked)
                onEvent(SettingsEvent.OnLogout)
            },
            onDismiss = {
                onEvent(SettingsEvent.OnLogoutClicked)
            }
        )
    }

    if (settingsState.isDeleteAccountClicked) {
        AlertDialogSetting(
            text = "Are you sure you want to DELETE account?",
            onConfirm = {
                onEvent(SettingsEvent.OnDeleteAccountClicked)
                onEvent(SettingsEvent.OnDeleteAccount)
            },
            onDismiss = {
                onEvent(SettingsEvent.OnDeleteAccountClicked)
            }
        )
    }

    if (settingsState.isThemeClicked) {
        ThemeSelectorBottomSheet(
            selectedTheme = settingsState.selectedTheme,
            onThemeSelected = { theme ->
                activity?.let { onEvent(SettingsEvent.OnThemeChanged(theme, it)) }
            },
            onDismiss = { onEvent(SettingsEvent.OnThemeClicked) }
        )
    }

    if (settingsState.isNotificationsClicked) {
        NotificationTypeDialog(
            selectedType = settingsState.selectedNotificationsType,
            onTypeSelected = { type -> onEvent(SettingsEvent.OnNotificationsTypeChanged(type)) },
            onDismiss = { onEvent(SettingsEvent.OnNotificationsClicked) }
        )
    }
}

@Composable
fun NotificationTypeDialog(
    selectedType: NotificationsType,
    onTypeSelected: (NotificationsType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Notification Type") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == NotificationsType.PUBLIC,
                        onClick = { onTypeSelected(NotificationsType.PUBLIC) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Public")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == NotificationsType.PRIVATE,
                        onClick = { onTypeSelected(NotificationsType.PRIVATE) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Private")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorBottomSheet(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeOption(
                    text = "System Default",
                    isSelected = selectedTheme == AppTheme.SYSTEM_DEFAULT,
                    onClick = { onThemeSelected(AppTheme.SYSTEM_DEFAULT) }
                )

                ThemeOption(
                    text = "Light",
                    isSelected = selectedTheme == AppTheme.LIGHT,
                    onClick = { onThemeSelected(AppTheme.LIGHT) }
                )

                ThemeOption(
                    text = "Dark",
                    isSelected = selectedTheme == AppTheme.DARK,
                    onClick = { onThemeSelected(AppTheme.DARK) }
                )

                Spacer(modifier = Modifier.padding(16.dp))
            }
        }

}

@Composable
fun ThemeOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style =MaterialTheme.typography.bodyLarge)
    }
}
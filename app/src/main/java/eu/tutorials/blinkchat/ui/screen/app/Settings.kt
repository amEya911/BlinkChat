package eu.tutorials.blinkchat.ui.screen.app

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.app.AppTheme
import eu.tutorials.blinkchat.data.event.app.SettingsEvent
import eu.tutorials.blinkchat.data.state.app.SettingsState
import eu.tutorials.blinkchat.ui.component.AppBar
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
    LaunchedEffect(key1 = settingsState.blockedUsers) {
        onEvent(SettingsEvent.OnLoadBlockedUsers)
    }

    LaunchedEffect(key1 = settingsState) {
        when {
            settingsState.isLoggedOut -> navigateToLoginScreen()
            settingsState.isAccountDeleted -> navigateToLoginScreen()
        }
    }

    if (settingsState.isLogoutClicked) {
        AlertDialogSetting(
            text = "Are you sure you want to LOGOUT?",
            onConfirm = {
                onEvent(SettingsEvent.OnLogoutDismissed)
                onEvent(SettingsEvent.OnLogout)
            },
            onDismiss = {
                onEvent(SettingsEvent.OnLogoutDismissed)
            }
        )
    }

    if (settingsState.isDeleteAccountClicked) {
        AlertDialogSetting(
            text = "Are you sure you want to DELETE account?",
            onConfirm = {
                onEvent(SettingsEvent.OnDeleteAccountDismissed)
                onEvent(SettingsEvent.OnDeleteAccount)
            },
            onDismiss = {
                onEvent(SettingsEvent.OnDeleteAccountDismissed)
            }
        )
    }

    if (settingsState.isThemeClicked) {
        ThemeSelectorBottomSheet(
            selectedTheme = settingsState.selectedTheme,
            onThemeSelected = { theme ->
                onEvent(SettingsEvent.OnThemeChanged(theme))
            },
            onDismiss = { onEvent(SettingsEvent.OnThemeClicked) }
        )
    }


//    if (settingsState.isThemeClicked) {
//        AlertDialog(
//            onDismissRequest = { onEvent(SettingsEvent.OnThemeClicked) },
//            title = { Text("Select Theme") },
//            text = {
//                Column {
//                    // RadioButton for System Default
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(
//                            selected = settingsState.selectedTheme == AppTheme.SYSTEM_DEFAULT,
//                            onClick = { onEvent(SettingsEvent.OnThemeChanged(AppTheme.SYSTEM_DEFAULT)) }
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("System Default")
//                    }
//
//                    // RadioButton for Light theme
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(
//                            selected = settingsState.selectedTheme == AppTheme.LIGHT,
//                            onClick = { onEvent(SettingsEvent.OnThemeChanged(AppTheme.LIGHT)) }
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Light")
//                    }
//
//                    // RadioButton for Dark theme
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(
//                            selected = settingsState.selectedTheme == AppTheme.DARK,
//                            onClick = { onEvent(SettingsEvent.OnThemeChanged(AppTheme.DARK)) }
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Dark")
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { onEvent(SettingsEvent.OnThemeClicked) }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }

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
                    onClick = {
                        onBlockUserClicked()
                    },
                    text = "Blocked",
                    icon = Icons.Default.Block,
                    count = settingsState.blockedUsers.size
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

                ActionButton(
                    onClick = {
                        onEvent(SettingsEvent.OnThemeClicked)
                    },
                    text = "Theme",
                    icon = painterResource(id = R.drawable.theme)
                )
            }
        }
    }
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
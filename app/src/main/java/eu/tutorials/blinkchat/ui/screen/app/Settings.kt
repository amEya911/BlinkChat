package eu.tutorials.blinkchat.ui.screen.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@Composable
fun Settings() {
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        }
    }
}
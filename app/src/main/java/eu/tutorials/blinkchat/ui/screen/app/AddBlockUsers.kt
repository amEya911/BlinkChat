package eu.tutorials.blinkchat.ui.screen.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@Composable
fun AddBlockUsers(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    onBackClicked: () -> Unit,
    onBlockUser: (String) -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Block Users",
                navigationIcon = Icons.Default.ArrowBackIosNew,
                onNavigationIconClicked = onBackClicked
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                LazyColumn {
                    items(contacts.sortedBy { it.displayName }) { user ->
                        BlockedUserItem(
                            blockedUser = user,
                            buttonName = "Block",
                            onClick = {
                                onBlockUser(user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


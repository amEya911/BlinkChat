package eu.tutorials.blinkchat.ui.component.chatroom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.tutorials.blinkchat.ui.component.inbox.MenuItem

@Composable
fun AddButtonClicked(
    modifier: Modifier = Modifier,
    onRoomLinkClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor =  MaterialTheme.colorScheme.secondary.copy(
                alpha = 0.5f
            )
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                text = "Copy Room Link",
                icon = Icons.Default.CopyAll,
                onClick = onRoomLinkClicked
            )
        }
    }
}
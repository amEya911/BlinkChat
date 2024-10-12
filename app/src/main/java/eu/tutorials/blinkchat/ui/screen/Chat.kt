package eu.tutorials.blinkchat.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import eu.tutorials.blinkchat.data.model.ContactModel

@Composable
fun Chat(
    contact: ContactModel?
) {
    Text(text = "Chatting with ${contact?.displayName}: ${contact?.phoneNumber}")
}
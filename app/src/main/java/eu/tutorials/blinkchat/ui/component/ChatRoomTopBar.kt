package eu.tutorials.blinkchat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomTopBar(contact: Contact) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = contact.displayName,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (contact.photoUri != null) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(45.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                    )
                }
            }
        },
        expandedHeight = 80.dp,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGray)
    )
}
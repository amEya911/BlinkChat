package eu.tutorials.blinkchat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.model.Contact

@Composable
fun BlockUnblockConfirmation(
    isBlock: Boolean,
    user: Contact,
    onConfirm: () -> Unit
) {
    val headText = if (isBlock) "Block" else "Unblock"
    val bodyText = if (isBlock) {
        "They cannot create a room with you nor schedule a meet.\n" +
                "They won't be notified that you blocked them.\n" +
                "You can unblock them anytime"
    } else {
        "They will be able to create room or schedule meet.\n" +
                "They won't be notified that you unblocked them."
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoUri != null) {
                    AsyncImage(
                        model = user.photoUri,
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(36.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(36.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$headText ${user.displayName}?",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Confirm", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

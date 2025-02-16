package eu.tutorials.blinkchat.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import eu.tutorials.blinkchat.R

@Composable
fun UserDisplay(
    modifier: Modifier = Modifier,
    photoUri: String?,
    size: Dp = 40.dp
) {
    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = "photoUri",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 2))
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.profile_image),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 2))
        )
    }
}
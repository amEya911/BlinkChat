package eu.tutorials.blinkchat.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.ui.theme.BackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    showIcon: Boolean = false,
    iconResId: Any? = null,
    onIconClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (showIcon) {
                    IconButton(onClick = { onIconClick?.invoke() }) {
                        when (iconResId) {
                            is Int -> {
                                Icon(
                                    painter = painterResource(id = iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            is ImageVector -> {
                                Icon(
                                    imageVector = iconResId,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        expandedHeight = 100.dp,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
    )
}

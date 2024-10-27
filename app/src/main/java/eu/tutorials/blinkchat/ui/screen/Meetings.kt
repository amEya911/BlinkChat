package eu.tutorials.blinkchat.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor

@Composable
fun Meetings(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Text(
                    "Search",
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.4f)
                )
            },
            singleLine = true,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(15.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldColor,
                unfocusedContainerColor = TextFieldColor,
                focusedTextColor = Color.Gray,
                unfocusedTextColor = Color.Gray,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }

}
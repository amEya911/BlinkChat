package eu.tutorials.blinkchat.ui.screen.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import eu.tutorials.blinkchat.data.datasource.local.LocalContact
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.LightGray

@Composable
fun BlockedUsers(
    modifier: Modifier = Modifier,
    blockedUsers: List<LocalContact> = emptyList(),
    onBackClicked: () -> Unit,
    onUnblockClicked: (String) -> Unit,
    onAddBlockUsers: () -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Blocked Accounts",
                iconResId = Icons.Default.Add,
                onIconClick = { onAddBlockUsers()},
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
                    items(blockedUsers) { user ->
                        BlockedUserItem(
                            blockedUser = user.toContact(),
                            buttonName = "Unblock",
                            onClick = {
                                onUnblockClicked(user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUserItem(
    modifier: Modifier = Modifier,
    blockedUser: Contact,
    buttonName: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        backgroundColor = BackgroundColor,
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (blockedUser.photoUri != null) {
                    AsyncImage(
                        model = blockedUser.photoUri,
                        contentDescription = "photoUri",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = blockedUser.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = { onClick(blockedUser.id) }
            ) {
                Text(text = buttonName)
            }
        }
    }
}

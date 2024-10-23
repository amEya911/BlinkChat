package eu.tutorials.blinkchat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.LoginOptionsEvent
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.DarkButtonColor
import eu.tutorials.blinkchat.ui.theme.LightGray
import eu.tutorials.blinkchat.ui.theme.TextColor
import eu.tutorials.blinkchat.ui.viewmodel.LoginOptionViewModel

@Composable
fun LoginOptions(
    onCLickLoginButton : () -> Unit,
    onClickGuestButton: () -> Unit,
    viewModel: LoginOptionViewModel = hiltViewModel()
) {
    val loginOptionsState by viewModel.loginOptionsState.collectAsState()

    LaunchedEffect(key1 = loginOptionsState) {
        if (loginOptionsState.isLoginClicked) {
            onCLickLoginButton()
            viewModel.onEvent(LoginOptionsEvent.Reset)
        }

        if (loginOptionsState.isGuestClicked) {
            onClickGuestButton()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .background(
                    color = LightGray,
                    shape = RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp)
                )
                .size(420.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Vanish",
                        color = TextColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                    Text(
                        text = "Share freely, chat privately,",
                        color = TextColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                    Text(
                        text = "and let it vanish",
                        color = TextColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            viewModel.onEvent(LoginOptionsEvent.onLoginClicked)
                        },
                        modifier = Modifier
                            .width(300.dp)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkButtonColor)
                    ) {
                        Text(text = "Login", color = TextColor, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.onEvent(LoginOptionsEvent.onGuestClicked)
                        },
                        modifier = Modifier
                            .width(300.dp)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkButtonColor)
                    ) {
                        Text(text = "Guest", color = TextColor, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LoginOptionsPreview() {
//    LoginOptions(
//        onCLickLoginButton = {},
//        onClickGuestButton = {}
//    )
//}

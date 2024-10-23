package eu.tutorials.blinkchat.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.LoginWithPhoneEvent
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.LightButtonColor
import eu.tutorials.blinkchat.ui.theme.TextColor
import eu.tutorials.blinkchat.ui.viewmodel.LoginWithPhoneViewModel

@Composable
fun LoginWithPhoneVerifyOTP(
    verificationId: String?,
    mobileNumber: String?,
    viewModel: LoginWithPhoneViewModel = hiltViewModel(),
    onOTPLoginSuccessful: () -> Unit
) {
    val loginState = viewModel.loginWithPhoneState.collectAsState().value

    val displayMobileNumber = mobileNumber ?: loginState.mobileNumber

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1.5f))
            Text(
                text = "Verification",
                fontSize = 54.sp,
                color = TextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enter OTP sent to $displayMobileNumber",
                fontSize = 16.sp,
                color = TextColor
            )
            Spacer(modifier = Modifier.weight(0.5f))
            TextField(
                value = loginState.verificationCode,
                onValueChange = {
                    viewModel.onEvent(
                        LoginWithPhoneEvent.EnterVerificationCode(it)
                    )
                },
                label = { Text("Verification Code") },
                placeholder = { Text("Enter code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color(0xFFF6FEDB),
                    unfocusedContainerColor = Color(0xFFF6FEDB),
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Button(
                onClick = {
                    if (verificationId != null) {
                        viewModel.onEvent(LoginWithPhoneEvent.VerifyCode(verificationId))
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightButtonColor)
            ) {
                Text(
                    text = "Verify Code"
                )
            }

            if (loginState.isLoggedIn) {
                onOTPLoginSuccessful()
            } else {
                loginState.verificationError?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

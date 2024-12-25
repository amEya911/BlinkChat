package eu.tutorials.blinkchat.ui.screen.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.ui.viewmodel.auth.LoginWithPhoneViewModel
import eu.tutorials.blinkchat.data.event.auth.LoginWithPhoneEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LoginWithPhoneVerifyPhone(
    viewModel: LoginWithPhoneViewModel = hiltViewModel(),
    onVerifyLoginSuccessful: (String, String) -> Unit
) {
    val loginState = viewModel.loginWithPhoneState.collectAsState().value
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(key1 = loginState.showVerificationField) {
        if (loginState.showVerificationField && !loginState.isLoggedIn) {
            loginState.verificationId?.let { verificationId ->
                onVerifyLoginSuccessful(verificationId, loginState.mobileNumber)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enter Phone Number",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(0.5f))
            TextField(
                value = loginState.mobileNumber,
                onValueChange = {
                    viewModel.onEvent(
                        LoginWithPhoneEvent.EnterMobileNumber(
                            it
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = "Mobile Number",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Button(
                onClick = {
                    if (activity != null) {
                        viewModel.onEvent(
                            LoginWithPhoneEvent.SendVerificationCode(
                                activity
                            )
                        )
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(
                    text = "Get OTP"
                )
            }

            if (loginState.verificationError != null) {
                Text(
                    text = loginState.verificationError,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
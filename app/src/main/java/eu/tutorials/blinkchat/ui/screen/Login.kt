package eu.tutorials.blinkchat.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.ui.viewmodel.LoginViewModel
import eu.tutorials.blinkchat.data.event.LoginEvent
import androidx.compose.ui.platform.LocalContext


@Composable
fun Login(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccessful: () -> Unit
) {
    val loginState = viewModel.loginState.collectAsState().value
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8D174)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enter your mobile number",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
                TextField(
                    value = loginState.mobileNumber,
                    onValueChange = { viewModel.onEvent(LoginEvent.EnterMobileNumber(it)) },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("Enter number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                        if (activity != null) {
                            viewModel.onEvent(LoginEvent.SendVerificationCode(activity))
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Send Verification Code")
                }

                if (loginState.showVerificationField) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter verification code",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    TextField(
                        value = loginState.verificationCode,
                        onValueChange = { viewModel.onEvent(LoginEvent.EnterVerificationCode(it)) },
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
                        onClick = { viewModel.onEvent(LoginEvent.VerifyCode) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "Verify Code")
                    }
                }
                if (loginState.isLoggedIn) {
                    onLoginSuccessful()
                } else {
                    loginState.verificationError?.let {
                        Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

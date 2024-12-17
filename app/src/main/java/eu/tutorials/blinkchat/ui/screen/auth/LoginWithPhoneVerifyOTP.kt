package eu.tutorials.blinkchat.ui.screen.auth

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import eu.tutorials.blinkchat.data.event.LoginWithPhoneEvent
import eu.tutorials.blinkchat.ui.theme.BackgroundColor
import eu.tutorials.blinkchat.ui.theme.LightButtonColor
import eu.tutorials.blinkchat.ui.theme.LightGray
import eu.tutorials.blinkchat.ui.theme.TextColor
import eu.tutorials.blinkchat.ui.theme.TextFieldColor
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
//            TextField(
//                value = loginState.verificationCode,
//                onValueChange = {
//                    viewModel.onEvent(
//                        LoginWithPhoneEvent.EnterVerificationCode(it)
//                    )
//                },
//                label = { Text("Verification Code") },
//                placeholder = { Text("Enter code") },
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth(0.8f),
//                colors = TextFieldDefaults.colors().copy(
//                    focusedContainerColor = Color(0xFFF6FEDB),
//                    unfocusedContainerColor = Color(0xFFF6FEDB),
//                    focusedTextColor = Color.Gray,
//                    unfocusedTextColor = Color.Gray,
//                    focusedPlaceholderColor = Color.Gray,
//                    unfocusedPlaceholderColor = Color.Gray,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent
//                )
//            )

            OTPVerificationBox(otpLength = 6) { otp ->
                viewModel.onEvent(LoginWithPhoneEvent.EnterVerificationCode(otp))
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                viewModel.onEvent(LoginWithPhoneEvent.OnDismiss)
            } else {
                loginState.verificationError?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OTPTextField(
    modifier: Modifier = Modifier,
    number: Int?,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onNumberChanged: (Int?) -> Unit,
    onKeyBoardBack: () -> Unit
) {
    var text by remember {
        mutableStateOf(
            TextFieldValue(
                text = number?.toString().orEmpty(),
                selection = TextRange(
                    index = if (number != null) 1 else 0
                )
            )
        )
    }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.Black
            )
            .background(TextFieldColor),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                val newNumber = newText.text
                if (newNumber.length <= 1 && newNumber.isDigitsOnly()) {
                    text = newText
                    onNumberChanged(newNumber.toIntOrNull())
                }
            },
            cursorBrush = SolidColor(Color.Black),
            singleLine = true,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .padding(10.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusChanged(it.isFocused)
                }
                .onKeyEvent { event ->
                    val didPressDelete = event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL
                    if (didPressDelete && text.text.isEmpty()) {
                        onKeyBoardBack()
                    }
                    false
                },
            decorationBox = { innerBox ->
                innerBox()
                if (!isFocused && text.text.isEmpty()) {
                    Text(
                        text = "-",
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }
            }
        )
    }
}

@Composable
fun OTPVerificationBox(
    otpLength: Int = 6,
    onOTPComplete: (String) -> Unit
) {
    val otpDigits = remember { mutableStateListOf(*Array(otpLength) { null as Int? }) }
    val focusRequesters = List(otpLength) { FocusRequester() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        otpDigits.forEachIndexed { index, _ ->
            OTPTextField(
                modifier = Modifier
                    .padding(4.dp)
                    .width(40.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                number = otpDigits[index],
                focusRequester = focusRequesters[index],
                onFocusChanged = { isFocused ->
                    if (isFocused && otpDigits[index] == null) {
                        otpDigits[index] = null
                    }
                },
                onNumberChanged = { newDigit ->
                    otpDigits[index] = newDigit
                    if (newDigit != null && index < otpLength - 1) {
                        focusRequesters[index + 1].requestFocus()
                    }
                    if (otpDigits.all { it != null }) {
                        onOTPComplete(otpDigits.joinToString(separator = "") { it.toString() })
                    }
                },
                onKeyBoardBack = {
                    if (index > 0) {
                        otpDigits[index - 1] = null
                        focusRequesters[index - 1].requestFocus()
                    }
                }
            )
        }
    }
}
package eu.tutorials.blinkchat.ui.screen.auth

import android.app.Activity
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.tutorials.blinkchat.data.event.auth.LoginWithPhoneEvent
import eu.tutorials.blinkchat.data.state.auth.LoginWithPhoneState
import eu.tutorials.blinkchat.ui.viewmodel.auth.LoginWithPhoneViewModel

@Composable
fun LoginWithPhoneVerifyOTP(
    verificationId: String?,
    mobileNumber: String?,
    onOTPLoginSuccessful: () -> Unit,
    onBackClicked: () -> Unit,
    viewModel: LoginWithPhoneViewModel,
    loginState: LoginWithPhoneState
) {
    val displayMobileNumber = mobileNumber ?: loginState.mobileNumber
    val systemUiController = rememberSystemUiController()
    val activity = LocalContext.current as? Activity
    systemUiController.setSystemBarsColor(color = MaterialTheme.colorScheme.background)

    LaunchedEffect(true) {
        viewModel.onEvent(LoginWithPhoneEvent.OnStartTimer)
    }

//    Log.d("Local123", "showDialog: ${loginState.showDialog}")
//    Log.d("Local123", "isLoggedIn: ${loginState.isLoggedIn}")

    if (loginState.showDialog) {
        var username by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Enter Username") },
            text = {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        label = { Text("Username") },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(LoginWithPhoneEvent.OnEnterDisplayName(username))
                        viewModel.onEvent(LoginWithPhoneEvent.DismissDialog)
                    }
                ) {
                    Text(text = "Enter")
                }
            }
        )
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
                text = "Enter OTP sent to $displayMobileNumber",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = {
                    viewModel.onEvent(LoginWithPhoneEvent.OnResetVerificationState)
                    onBackClicked()
                }
            ) {
                Text(text = "Wrong Number?")
            }
            Spacer(modifier = Modifier.weight(0.5f))

            OTPVerificationBox(otpLength = 6) { otp ->
                Log.d("OTPTextField", "Entered OTP: $otp")
                viewModel.onEvent(LoginWithPhoneEvent.EnterVerificationCode(otp))
            }

            if (loginState.verificationError != null) {
                Text(
                    text = loginState.verificationError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.onEvent(LoginWithPhoneEvent.OnClearError)
                    val enteredOTP = loginState.verificationCode
                    if (verificationId != null && enteredOTP.length == 6) {
                        viewModel.onEvent(LoginWithPhoneEvent.VerifyCode(verificationId))
                    } else {
                        viewModel.onEvent(LoginWithPhoneEvent.OnShowError("Please enter the complete OTP"))
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Verify Code")
            }
            if (loginState.isTimerRunning) {
                Text(
                    text = "Resend Code in ${loginState.timerSeconds}s",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                TextButton(
                    onClick = {
                        activity?.let {
                            viewModel.onEvent(
                                LoginWithPhoneEvent.OnResendVerificationCode(
                                    activity = activity,
                                    mobileNumber = mobileNumber ?: loginState.mobileNumber
                                )
                            )
                        }
                    }
                ) {
                    Text(
                        text = "Resend Verification Code",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (loginState.isLoggedIn) {
                Log.d("Local123", "inside isLoggedIn")
                //onOTPLoginSuccessful()
                viewModel.onEvent(LoginWithPhoneEvent.OnDismiss)
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
                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.4f
                )
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
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
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                    .height(50.dp),
                number = otpDigits[index],
                focusRequester = focusRequesters[index],
                onFocusChanged = { isFocused ->
                    Log.d("OTPTextField", "Field $index focused: $isFocused")
                    if (isFocused && otpDigits[index] == null) {
                        otpDigits[index] = null
                    }
                },
                onNumberChanged = { newDigit ->
                    otpDigits[index] = newDigit
                    Log.d("OTPTextField", "Field $index updated: $newDigit")
                    if (newDigit != null && index < otpLength - 1) {
                        focusRequesters[index + 1].requestFocus()
                    }
                    val currentOTP = otpDigits.joinToString("") { it?.toString() ?: "" }
                    Log.d("OTPTextField", "Current OTP: $currentOTP")
                    if (otpDigits.all { it != null }) {
                        onOTPComplete(currentOTP)
                    }
                },
                onKeyBoardBack = {
                    if (index > 0 && otpDigits[index] == null) {
                        focusRequesters[index - 1].requestFocus()
                        Log.d("OTPTextField", "Backspace pressed at field $index")
                    } else {
                        otpDigits[index] = null
                    }
                }
            )
        }
    }
}

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
//                    .fillMaxWidth(0.8f)
//                    .height(60.dp)
//                    .clip(RoundedCornerShape(30.dp)),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
//                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
//                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent
//                )
//            )

@Composable
fun DisplayNameDialog(modifier: Modifier = Modifier) {

}
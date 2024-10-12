package eu.tutorials.blinkchat.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.LoginEvent
import eu.tutorials.blinkchat.data.state.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    private var verificationId: String? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnterMobileNumber -> {
                _loginState.value = _loginState.value.copy(mobileNumber = event.mobileNumber)
            }
            is LoginEvent.EnterVerificationCode -> {
                _loginState.value = _loginState.value.copy(verificationCode = event.verificationCode)
            }
            is LoginEvent.SendVerificationCode -> {
                sendVerificationCode(_loginState.value.mobileNumber, event.activity)
            }
            is LoginEvent.VerifyCode -> {
                verifyVerificationCode(_loginState.value.verificationCode)
            }
        }
    }


    private fun sendVerificationCode(mobileNumber: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$mobileNumber") // Set the mobile number (ensure correct format)
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
            .setActivity(activity) // Pass the Activity here
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle the failure
                    _loginState.value = _loginState.value.copy(
                        verificationError = "Verification failed: ${e.message}"
                    )
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _loginState.value = _loginState.value.copy(
                        verificationError = null,
                        showVerificationField = true
                    )
                    this@LoginViewModel.verificationId = verificationId // Save the verification ID
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId ?: "", code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Login success
                _loginState.value = _loginState.value.copy(
                    isLoggedIn = true
                )
            } else {
                // Login failure
                _loginState.value = _loginState.value.copy(
                    showVerificationField = false,
                    verificationError = "Login failed: ${task.exception?.message}"
                )
            }
        }
    }
}

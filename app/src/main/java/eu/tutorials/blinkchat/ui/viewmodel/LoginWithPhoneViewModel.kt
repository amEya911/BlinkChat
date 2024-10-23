package eu.tutorials.blinkchat.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.LoginWithPhoneEvent
import eu.tutorials.blinkchat.data.state.LoginWithPhoneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginWithPhoneViewModel @Inject constructor() : ViewModel() {

    private val _loginWithPhoneState = MutableStateFlow(LoginWithPhoneState())
    val loginWithPhoneState: StateFlow<LoginWithPhoneState> = _loginWithPhoneState

    private var verificationId: String? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onEvent(event: LoginWithPhoneEvent) {
        when (event) {
            is LoginWithPhoneEvent.EnterMobileNumber -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(mobileNumber = event.mobileNumber)
            }
            is LoginWithPhoneEvent.EnterVerificationCode -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(verificationCode = event.verificationCode)
            }
            is LoginWithPhoneEvent.SendVerificationCode -> {
                sendVerificationCode(_loginWithPhoneState.value.mobileNumber, event.activity)
            }
            is LoginWithPhoneEvent.VerifyCode -> {
                verifyVerificationCode(_loginWithPhoneState.value.verificationCode, event.verificationId)
            }

        }
    }

    private fun verifyVerificationCode(code: String, verificationId: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
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
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        verificationError = "Verification failed: ${e.message}"
                    )
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    Log.d("LoginWithPhoneViewModel1", "Code sent successfully, verificationId: $verificationId")
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        verificationError = null,
                        showVerificationField = true,
                        verificationId = verificationId,
                        mobileNumber = _loginWithPhoneState.value.mobileNumber
                    )
                    this@LoginWithPhoneViewModel.verificationId = verificationId // Save the verification ID
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginWithPhoneViewModel1", "Login successful")
                // Login success
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    isLoggedIn = true
                )
            } else {
                Log.e("LoginWithPhoneViewModel1", "Login failed: ${task.exception?.message}")
                // Login failure
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    showVerificationField = false,
                    verificationError = "Login failed: ${task.exception?.message}"
                )
            }
        }
    }

}

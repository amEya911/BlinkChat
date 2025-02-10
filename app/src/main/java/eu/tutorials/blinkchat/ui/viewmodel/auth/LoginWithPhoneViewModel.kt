package eu.tutorials.blinkchat.ui.viewmodel.auth

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.auth.LoginWithPhoneEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.auth.LoginWithPhoneState
import eu.tutorials.blinkchat.util.HashUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginWithPhoneViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginWithPhoneState = MutableStateFlow(LoginWithPhoneState())
    val loginWithPhoneState: StateFlow<LoginWithPhoneState> = _loginWithPhoneState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TIMEOUT_DURATION = 60L
    }

    fun onEvent(event: LoginWithPhoneEvent) {
        when (event) {
            is LoginWithPhoneEvent.EnterMobileNumber -> {
                _loginWithPhoneState.value =
                    _loginWithPhoneState.value.copy(mobileNumber = event.mobileNumber)
            }

            is LoginWithPhoneEvent.EnterVerificationCode -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    verificationCode = event.verificationCode,
                    verificationError = null
                )
            }

            is LoginWithPhoneEvent.SendVerificationCode -> {
                if (isValidMobileNumber(_loginWithPhoneState.value.mobileNumber)) {
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        lastRequestedMobileNumber = _loginWithPhoneState.value.mobileNumber,
                    )
                    sendVerificationCode(_loginWithPhoneState.value.mobileNumber, event.activity)
                } else {
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        verificationError = "Please enter a valid 10-digit mobile number"
                    )
                }
            }

            is LoginWithPhoneEvent.VerifyCode -> {
                _loginWithPhoneState.value =
                    _loginWithPhoneState.value.copy(verificationError = null)
                verifyVerificationCode(
                    _loginWithPhoneState.value.verificationCode,
                    event.verificationId
                )
            }

            LoginWithPhoneEvent.OnDismiss -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    isLoggedIn = false
                )
            }

            LoginWithPhoneEvent.OnClearError -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    verificationError = null
                )
            }

            is LoginWithPhoneEvent.OnShowError -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    verificationError = event.error
                )
            }

            LoginWithPhoneEvent.OnResetVerificationState -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    verificationCode = "",
                    showVerificationField = false,
                    verificationId = null,
                    isLoggedIn = false
                )
            }

            is LoginWithPhoneEvent.OnResendVerificationCode -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    verificationError = null,
                    showVerificationField = false,
                    verificationId = null,
                    timerSeconds = 60
                )
                sendVerificationCode(mobileNumber = event.mobileNumber, event.activity)
                startTimer()
            }

            LoginWithPhoneEvent.OnStartTimer -> {
                startTimer()

            }

            LoginWithPhoneEvent.OnClearSnackbarMessage -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    snackbarMessage = null
                )
            }

            is LoginWithPhoneEvent.ShowSnackbar -> {
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    snackbarMessage = event.message
                )
            }
        }
    }

    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        val trimmedNumber = mobileNumber.replace(" ", "")
        return trimmedNumber.length == 10 && trimmedNumber.all { it.isDigit() }
    }

    private fun startTimer() {
        val lastRequestTime = _loginWithPhoneState.value.lastRequestTime ?: return
        val currentTime = System.currentTimeMillis()
        val timeElapsed = (currentTime - lastRequestTime) / 1000
        var remainingTime = (Companion.TIMEOUT_DURATION - timeElapsed).toInt()

        if (remainingTime <= 0) {
            _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                isTimerRunning = false,
                timerSeconds = 0
            )
            return
        }

        _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
            isTimerRunning = true,
            timerSeconds = remainingTime
        )

        viewModelScope.launch {
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime--

                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(timerSeconds = remainingTime)

                if (remainingTime <= 0) {
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(isTimerRunning = false)
                }
            }
        }
    }


    private fun verifyVerificationCode(code: String, verificationId: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun sendVerificationCode(mobileNumber: String, activity: Activity) {
        val currentTime = System.currentTimeMillis()
        val lastRequestTime = _loginWithPhoneState.value.lastRequestTime ?: 0
        val timeElapsed = (currentTime - lastRequestTime) / 1000

        if (timeElapsed < Companion.TIMEOUT_DURATION) {
            val remainingTime = Companion.TIMEOUT_DURATION - timeElapsed
            _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                verificationError = "Please wait $remainingTime seconds before requesting a new code.",
                timerSeconds = remainingTime.toInt()
            )
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$mobileNumber")
            .setTimeout(Companion.TIMEOUT_DURATION, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("LoginWithPhoneViewModel", "Verification failed: ${e.message}")
                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        verificationError = "Verification failed: ${e.message}"
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("LoginWithPhoneViewModel", "Code sent successfully, verificationId: $verificationId")

                    _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                        verificationError = null,
                        showVerificationField = true,
                        verificationId = verificationId,
                        mobileNumber = mobileNumber,
                        lastRequestTime = currentTime,
                        timerSeconds = Companion.TIMEOUT_DURATION.toInt(),
                        isTimerRunning = true
                    )

                    startTimer()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginWithPhoneViewModel", "Login successful")
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    isLoggedIn = true
                )

                val user = auth.currentUser
                user?.let {
                    val hashedId = HashUtil.hashPhoneNumber(it.phoneNumber ?: "Unknown Number")
                    val contact = Contact(
                        id = hashedId,
                        displayName = it.displayName ?: "Unknown User",
                        phoneNumber = it.phoneNumber ?: "Unknown Number",
                        photoThumbnailUri = it.photoUrl?.toString(),
                        photoUri = it.photoUrl?.toString()
                    )
                    userRepository.addUserDetails(contact)
                }
            } else {
                Log.e("LoginWithPhoneViewModel", "Login failed: ${task.exception?.message}")
                _loginWithPhoneState.value = _loginWithPhoneState.value.copy(
                    showVerificationField = false,
                    verificationError = "Login failed: ${task.exception?.message}"
                )
            }
        }
    }
}

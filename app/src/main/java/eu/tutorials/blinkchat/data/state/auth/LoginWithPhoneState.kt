package eu.tutorials.blinkchat.data.state.auth

data class LoginWithPhoneState(
    val mobileNumber: String = "",
    val verificationCode: String = "",
    val showVerificationField: Boolean = false,
    val verificationId: String? = null,
    val verificationError: String? = null,
    val isLoggedIn: Boolean = false,
    val timerSeconds: Int = 60,
    val isTimerRunning: Boolean = false,
    val isTimerPaused: Boolean = false,
    val snackbarMessage: String? = null
)


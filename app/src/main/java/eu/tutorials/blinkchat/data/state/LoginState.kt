package eu.tutorials.blinkchat.data.state

data class LoginState(
    val mobileNumber: String = "",
    val verificationCode: String = "",
    val showVerificationField: Boolean = false,
    val verificationError: String? = null,
    val isLoggedIn: Boolean = false
)

package eu.tutorials.blinkchat.data.state.auth

data class LoginWithPhoneState(
    val mobileNumber: String = "",
    val verificationCode: String = "",
    val showVerificationField: Boolean = false,
    val verificationId: String? = null,
    val verificationError: String? = null,
    val isLoggedIn: Boolean = false
)


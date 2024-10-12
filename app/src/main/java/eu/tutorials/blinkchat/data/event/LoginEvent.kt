package eu.tutorials.blinkchat.data.event

import android.app.Activity

sealed class LoginEvent {
    data class EnterMobileNumber(val mobileNumber: String) : LoginEvent()
    data class EnterVerificationCode(val verificationCode: String) : LoginEvent()
    data class SendVerificationCode(val activity: Activity) : LoginEvent()
    data object VerifyCode : LoginEvent()
}


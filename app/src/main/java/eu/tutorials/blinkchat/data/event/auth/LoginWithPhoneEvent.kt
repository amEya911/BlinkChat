package eu.tutorials.blinkchat.data.event.auth

import android.app.Activity

sealed class LoginWithPhoneEvent {
    data class EnterMobileNumber(val mobileNumber: String) : LoginWithPhoneEvent()
    data class EnterVerificationCode(val verificationCode: String) : LoginWithPhoneEvent()
    data class SendVerificationCode(val activity: Activity) : LoginWithPhoneEvent()
    data class VerifyCode(val verificationId: String) : LoginWithPhoneEvent()
    data object OnDismiss: LoginWithPhoneEvent()
}


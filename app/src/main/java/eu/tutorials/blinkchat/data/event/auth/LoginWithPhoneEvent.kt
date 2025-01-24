package eu.tutorials.blinkchat.data.event.auth

import android.app.Activity

sealed class LoginWithPhoneEvent {
    data class EnterMobileNumber(val mobileNumber: String) : LoginWithPhoneEvent()
    data class ShowSnackbar(val message: String) : LoginWithPhoneEvent()
    data object OnClearSnackbarMessage: LoginWithPhoneEvent()
    data class EnterVerificationCode(val verificationCode: String) : LoginWithPhoneEvent()
    data class SendVerificationCode(val activity: Activity) : LoginWithPhoneEvent()
    data class VerifyCode(val verificationId: String) : LoginWithPhoneEvent()
    data object OnDismiss: LoginWithPhoneEvent()
    data object OnClearError: LoginWithPhoneEvent()
    data class OnShowError(val error: String): LoginWithPhoneEvent()
    data object OnResetVerificationState: LoginWithPhoneEvent()
    data class OnResendVerificationCode(val mobileNumber: String, val activity: Activity): LoginWithPhoneEvent()
    data object OnStartTimer: LoginWithPhoneEvent()
}


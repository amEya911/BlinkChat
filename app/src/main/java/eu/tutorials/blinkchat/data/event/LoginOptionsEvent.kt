package eu.tutorials.blinkchat.data.event

sealed class LoginOptionsEvent {

    data object onLoginClicked: LoginOptionsEvent()
    data object onGuestClicked: LoginOptionsEvent()
    object Reset : LoginOptionsEvent()
}
package eu.tutorials.blinkchat.data.event

sealed class LoginOptionsEvent {
    data object OnLoginClicked: LoginOptionsEvent()
    data object OnGuestClicked: LoginOptionsEvent()
    data object Reset : LoginOptionsEvent()
}
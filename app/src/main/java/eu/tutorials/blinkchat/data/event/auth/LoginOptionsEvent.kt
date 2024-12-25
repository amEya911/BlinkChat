package eu.tutorials.blinkchat.data.event.auth

sealed class LoginOptionsEvent {
    data object OnLoginClicked: LoginOptionsEvent()
    data object OnGuestClicked: LoginOptionsEvent()
    data object Reset : LoginOptionsEvent()
}
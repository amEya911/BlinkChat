package eu.tutorials.blinkchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.LoginOptionsEvent
import eu.tutorials.blinkchat.data.state.LoginOptionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginOptionViewModel @Inject constructor(): ViewModel() {

    private val _loginOptionsState = MutableStateFlow(LoginOptionsState())
    val loginOptionsState: StateFlow<LoginOptionsState> = _loginOptionsState

    fun onEvent(event: LoginOptionsEvent) {
        when (event) {
            LoginOptionsEvent.OnGuestClicked -> {
                _loginOptionsState.value = _loginOptionsState.value.copy(
                    isGuestClicked = true
                )
            }
            LoginOptionsEvent.OnLoginClicked -> {
                _loginOptionsState.value = _loginOptionsState.value.copy(
                    isLoginClicked = true
                )
            }
            LoginOptionsEvent.Reset -> {
                _loginOptionsState.value = LoginOptionsState()
            }
        }
    }

}
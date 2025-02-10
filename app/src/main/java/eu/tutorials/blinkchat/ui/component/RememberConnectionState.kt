package eu.tutorials.blinkchat.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import eu.tutorials.blinkchat.util.ConnectivityObserver
import kotlinx.coroutines.flow.collectLatest

@Composable
fun rememberInternetConnectionState(): Boolean {
    val context = LocalContext.current
    val isOnlineState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ConnectivityObserver.startObserving(context)

        ConnectivityObserver.isOnline.collectLatest { isConnected ->
            isOnlineState.value = isConnected
        }
    }

    return isOnlineState.value
}

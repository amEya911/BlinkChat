package eu.tutorials.blinkchat.ui.component.chatroom

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import eu.tutorials.blinkchat.R
import eu.tutorials.blinkchat.data.event.ChatRoomEvent
import eu.tutorials.blinkchat.data.state.ChatRoomState
import eu.tutorials.blinkchat.ui.component.CameraPermissionTextProvider
import eu.tutorials.blinkchat.ui.component.PermissionDialog
import eu.tutorials.blinkchat.ui.component.RecordAudioPermissionTextProvider
import eu.tutorials.blinkchat.ui.screen.app.openAppSettings
import eu.tutorials.blinkchat.ui.theme.LightGray
import eu.tutorials.blinkchat.ui.viewmodel.ChatRoomViewModel

@Composable
fun ChatBottomBar(
    onRoomLinkClicked: () -> Unit,
    onEvent: (ChatRoomEvent) -> Unit,
    chatRoomState: ChatRoomState,
    context: Context,
    activity: Activity,
    viewModel: ChatRoomViewModel
) {
    val visiblePermissionDialogQueue = viewModel.visiblePermissionDialogQueue.collectAsState().value

    val permissionsToRequest = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    )

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                val isGranted = perms[permission] == true
                Log.d("Permissions1", "Permission: $permission, Granted: $isGranted")

                if (isGranted) {
                    onEvent(
                        ChatRoomEvent.OnPermissionResult(
                            permission = permission,
                            isGranted = true
                        )
                    )
                } else {
                    onEvent(
                        ChatRoomEvent.OnPermissionResult(
                            permission = permission,
                            isGranted = false
                        )
                    )
                }
            }
        }
    )

    val permissionsNotGranted = permissionsToRequest.filter { permission ->
        ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    Log.d("Permissions1", "Permissions not granted: $permissionsNotGranted")
    Log.d("Permissions2", "hi: $visiblePermissionDialogQueue")


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!chatRoomState.isOtherUserInChatRoom || !chatRoomState.isCurrentUserInChatRoom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for the other user to join the chat room.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            IconButton(onClick = { onRoomLinkClicked() }) {
                Icon(
                    imageVector = Icons.Default.CopyAll,
                    contentDescription = "Copy",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (permissionsNotGranted.isNotEmpty()) {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                } else {
                    onEvent(ChatRoomEvent.OnLaunchCamera(context = context, activity = activity))
                }
            }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onEvent(ChatRoomEvent.OnMessageTyping(""))
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.eraser),
                    contentDescription = "Erase",
                    tint = Color.Black
                )
            }
        }

        visiblePermissionDialogQueue
            .forEach { permission ->
                PermissionDialog(
                    permissionTextProvider = when (permission) {
                        Manifest.permission.CAMERA -> {
                            CameraPermissionTextProvider()
                        }

                        Manifest.permission.RECORD_AUDIO -> {
                            RecordAudioPermissionTextProvider()
                        }

                        else -> return@forEach
                    },
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        activity, permission
                    ),
                    onDismiss = { onEvent(ChatRoomEvent.OnDismissPermissionDialog) },
                    onOkClick = {
                        onEvent(ChatRoomEvent.OnDismissPermissionDialog)
                        multiplePermissionResultLauncher.launch(
                            arrayOf(permission)
                        )
                    },
                    onGoToAppSettingsClick = {
                        activity.openAppSettings()
                    }
                )
            }
    }
}



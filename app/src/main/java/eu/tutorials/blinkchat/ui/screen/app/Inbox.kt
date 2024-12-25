package eu.tutorials.blinkchat.ui.screen.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.state.app.InboxState
import eu.tutorials.blinkchat.ui.component.AppBar
import eu.tutorials.blinkchat.ui.component.ContactPermissionTextProvider
import eu.tutorials.blinkchat.ui.component.CustomTextField
import eu.tutorials.blinkchat.ui.component.PermissionDialog
import eu.tutorials.blinkchat.ui.component.inbox.InboxContactPress
import eu.tutorials.blinkchat.ui.component.ScheduleMeetDialog
import eu.tutorials.blinkchat.ui.component.inbox.AllChatItem
import eu.tutorials.blinkchat.ui.component.inbox.RecentChatItem
import eu.tutorials.blinkchat.ui.viewmodel.app.InboxViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Inbox(
    inboxState: InboxState,
    onEvent: (InboxEvent) -> Unit,
    modifier: Modifier = Modifier,
    onStartChatWithContact: (String) -> Unit,
    inboxViewModel: InboxViewModel
) {
    val activity = LocalContext.current as? Activity
    var searchQuery = inboxState.searchQuery
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val visiblePermissionDialogQueue =
        inboxViewModel.visiblePermissionDialogQueue.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val contactsPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEvent(
                InboxEvent.OnPermissionResult(
                    permission = Manifest.permission.READ_CONTACTS,
                    isGranted = isGranted
                )
            )
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    visiblePermissionDialogQueue?.let { permission ->
        val permissionTextProvider = when (permission) {
            Manifest.permission.READ_CONTACTS -> ContactPermissionTextProvider()
            else -> throw IllegalArgumentException("Unsupported permission: $permission")
        }

        PermissionDialog(
            permissionTextProvider = permissionTextProvider,
            isPermanentlyDeclined = activity?.let {
                !shouldShowRequestPermissionRationale(it, permission)
            } ?: true,
            onDismiss = { onEvent(InboxEvent.OnDismissPermissionDialog) },
            onOkClick = {
                onEvent(InboxEvent.OnDismissPermissionDialog)
            },
            onGoToAppSettingsClick = {
                activity?.openAppSettings()
            }
        )
    }

    LaunchedEffect(key1 = inboxState.isAllContactsClicked) {
        if (inboxState.isAllContactsClicked) {
            contactsPermissionResultLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }
    }

    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("Permissions3", "Notification permission already granted")
            }
        } else {
            Log.d("Permissions3", "Notification permission not required on this Android version")
        }
    }

    LaunchedEffect(key1 = true) {
        onEvent(InboxEvent.LoadRecentChats)
    }

    LaunchedEffect(inboxState.contacts) {
        if (inboxState.contacts.isEmpty()) {
            activity?.let { onEvent(InboxEvent.OnLoadContacts) }
        }
    }

    LaunchedEffect(key1 = inboxState.isEnterChatRoom) {
        if (inboxState.isEnterChatRoom) {
            onStartChatWithContact(inboxState.navigateToChatId!!)
            onEvent(InboxEvent.ResetEnterChatRoom)
        }
    }

    val contentModifier =
        if (inboxState.isContactClicked || inboxState.isScheduleAMeetClicked) Modifier.blur(
            80.dp,
            edgeTreatment = BlurredEdgeTreatment.Unbounded
        ) else Modifier

    Scaffold(
        topBar = {
            if (!(inboxState.isContactClicked || inboxState.isScheduleAMeetClicked)) {
                AppBar(
                    title = "Chats",
                    onIconClick = { onEvent(InboxEvent.OnAllContactsIconClicked) },
                    iconResId = Icons.Default.AccountCircle
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(contentModifier)
        ) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                CustomTextField(
                    value = searchQuery ?: "",
                    onValueChange = {
                        searchQuery = it
                        onEvent(InboxEvent.SearchUsers(it))
                    },
                    placeholderText = "Search",
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )

                val displayedContacts = when {
                    inboxState.isAllContactsClicked -> {
                        if (inboxState.searchQuery.isNullOrBlank()) {
                            inboxState.contacts
                        } else {
                            inboxState.contacts.filter {
                                it.displayName.contains(
                                    inboxState.searchQuery,
                                    ignoreCase = true
                                ) ||
                                        it.phoneNumber.contains(inboxState.searchQuery)
                            }
                        }
                    }

                    else -> {
                        if (inboxState.searchQuery.isNullOrBlank()) {
                            inboxState.recentContacts.map { it.contact }
                        } else {
                            inboxState.recentContacts.map { it.contact }.filter {
                                it.displayName.contains(
                                    inboxState.searchQuery,
                                    ignoreCase = true
                                ) ||
                                        it.phoneNumber.contains(inboxState.searchQuery)
                            }
                        }
                    }
                }
                if (inboxState.isAllContactsClicked) {
                    LazyColumn {
                        val groupedContacts = displayedContacts
                            .sortedBy { it.displayName }
                            .groupBy { it.displayName.first().uppercaseChar() }

                        groupedContacts.forEach { (initial, contacts) ->
                            stickyHeader {
                                Text(
                                    text = initial.toString(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(contacts) { contact ->
                                AllChatItem(contact = contact, onEvent = onEvent)
                            }
                        }
                    }
                } else {
                    LazyColumn {
                        items(displayedContacts.reversed()) { contact ->
                            RecentChatItem(
                                inboxState = inboxState,
                                contact = contact,
                                onEvent = onEvent
                            )
                        }
                    }
                }
            }
        }
        if (inboxState.isContactClicked) {
            keyboardController?.hide()
            InboxContactPress(
                state = inboxState,
                onEvent = onEvent
            ) { contact ->
                RecentChatItem(
                    inboxState = inboxState,
                    contact = contact,
                    onEvent = {}
                )
            }
        }
        if (inboxState.isScheduleAMeetClicked) {
            ScheduleMeetDialog(
                onDismiss = { onEvent(InboxEvent.OnScheduleDismissed) },
                onConfirm = { date, time ->
                    onEvent(InboxEvent.OnScheduleConfirmed(inboxState.selectedContact, date, time))
                }
            )
        }
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

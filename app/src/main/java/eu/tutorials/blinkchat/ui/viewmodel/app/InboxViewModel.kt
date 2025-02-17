package eu.tutorials.blinkchat.ui.viewmodel.app

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContact
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.RecentChatRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.app.InboxEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.RecentChatContact
import eu.tutorials.blinkchat.data.model.toContact
import eu.tutorials.blinkchat.data.state.app.InboxState
import eu.tutorials.blinkchat.util.ConnectivityObserver
import eu.tutorials.blinkchat.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val appContext: Application,
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val meetRepository: MeetRepository,
    private val localRepository: LocalRepository,
    private val recentChatRepository: RecentChatRepository
) : ViewModel() {

    private val _inboxState = MutableStateFlow(InboxState())
    val inboxState: StateFlow<InboxState> = _inboxState

    private val _visiblePermissionDialogQueue = MutableStateFlow<String?>(null)
    val visiblePermissionDialogQueue: StateFlow<String?> = _visiblePermissionDialogQueue

    init {
        loadCurrentUser()
        loadRecentChats()
    }

    fun onEvent(event: InboxEvent) {
        when (event) {
            InboxEvent.OnLoadContacts -> {
                loadContacts()
            }

            is InboxEvent.OnContactClicked -> {
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = true,
                    selectedContact = event.contact
                )

                fetchBlockedAndMutedData(event.contact.id)
            }

            InboxEvent.OnContactDismissed -> {
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            InboxEvent.OnAllContactsIconClicked -> {
                _inboxState.value = _inboxState.value.copy(
                    isAllContactsClicked = !_inboxState.value.isAllContactsClicked
                )
            }

            is InboxEvent.SearchUsers -> {
                searchUsers(event.searchQuery)
            }

            InboxEvent.OnEnterChatRoom -> {
                createChatRoom()
            }

            InboxEvent.ResetEnterChatRoom -> {
                _inboxState.value = _inboxState.value.copy(
                    isEnterChatRoom = false,
                    searchQuery = null
                )
            }

            InboxEvent.OnScheduleAMeetClick -> {
                _inboxState.value = _inboxState.value.copy(
                    isScheduleAMeetClicked = true
                )
            }

            is InboxEvent.OnScheduleConfirmed -> {
                _inboxState.value = _inboxState.value.copy(
                    scheduleDate = event.date,
                    scheduleTime = event.time,
                    isScheduleAMeetClicked = false
                )
                addScheduleMeets(
                    otherUserContact = event.otherUserContact,
                    date = event.date,
                    time = event.time
                )
            }

            InboxEvent.OnScheduleDismissed -> {
                _inboxState.value = _inboxState.value.copy(
                    isScheduleAMeetClicked = false
                )
            }

            is InboxEvent.OnDeleteRecentChat -> {
                deleteRecentChat(event.recentChatContact.recentChatId)
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            is InboxEvent.OnBlockUser -> {
                blockUser(otherUserId = event.otherUserId)
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            is InboxEvent.OnUnblockUser -> {
                unblockUser(otherUserId = event.otherUserId)
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            is InboxEvent.OnMuteUser -> {
                muteUser(event.otherUserId)
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }
            is InboxEvent.OnUnmuteUser -> {
                unmuteUser(event.otherUserId)
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            InboxEvent.OnDismissPermissionDialog -> {
                _visiblePermissionDialogQueue.value = null
            }

            is InboxEvent.OnPermissionResult -> {
                if (!event.isGranted && _visiblePermissionDialogQueue.value != event.permission) {
                    _visiblePermissionDialogQueue.value = event.permission
                }
            }

            InboxEvent.OnSnackbarDisplayed -> {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = null
                )
            }

            InboxEvent.OnNoInternetConnection -> {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Cannot create room, no internet connection"
                )
            }

            InboxEvent.OnStartRefresh -> {
                _inboxState.value = _inboxState.value.copy(
                    isRefreshing = true
                )
                refreshData()
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            delay(500)

            while (!ConnectivityObserver.isOnline.value) {
                delay(1000)
            }

            try {
                loadCurrentUser()
                loadRecentChats()
                loadContacts()
            } finally {
                _inboxState.value = _inboxState.value.copy(
                    isRefreshing = false
                )
            }
        }
    }

    private fun loadCurrentUser() {
        val currentUserId = userRepository.currentUserId()
        Log.d("Nimish", "currentUserId: $currentUserId")
        if (currentUserId != null) {
            userRepository.getUserDetails(currentUserId) { currentUserDetails ->
                if (currentUserDetails != null) {
                    _inboxState.value = _inboxState.value.copy(
                        currentUserContact = Contact(
                            id = currentUserId,
                            displayName = currentUserDetails.displayName,
                            phoneNumber = currentUserDetails.phoneNumber,
                            photoUri = currentUserDetails.photoUri,
                            photoThumbnailUri = currentUserDetails.photoThumbnailUri
                        )
                    )
                    Log.d(
                        "InboxViewModel",
                        "currentUserContact: ${_inboxState.value.currentUserContact}"
                    )
                } else {
                    Log.e("InboxViewModel", "Failed to retrieve current user details.")
                }
            }
        } else {
            Log.e("InboxViewModel", "Current user is not logged in.")
        }
    }

    private fun loadRecentChats() {
        val currentUserId = userRepository.currentUserId()
        if (currentUserId != null) {
            recentChatRepository.listenToRecentChats(currentUserId) { recentChatUserIds ->
                val matchedRecentContacts = recentChatUserIds.map { (contact, recentChatId) ->
                    val matchedContact = runBlocking {
                        localRepository.getContactById(contact.id)
                    }
                    if (matchedContact != null) {
                        RecentChatContact(recentChatId = recentChatId, contact = matchedContact.toContact())
                    } else {
                        RecentChatContact(
                            recentChatId = recentChatId,
                            contact = Contact(
                                id = contact.id,
                                displayName = contact.phoneNumber,
                                phoneNumber = contact.phoneNumber,
                                photoThumbnailUri = contact.photoThumbnailUri,
                                photoUri = contact.photoUri
                            )
                        )
                    }
                }

                _inboxState.value = _inboxState.value.copy(
                    recentContacts = matchedRecentContacts
                )
            }

            recentChatRepository.listenForPresence(currentUserId) { activeUserNames ->
                _inboxState.value = _inboxState.value.copy(
                    usersInChatRoom = activeUserNames.filterNotNull()
                )
            }

            userRepository.listenForMutedUsers(currentUserId) { mutedUsers ->
                _inboxState.value = _inboxState.value.copy(
                    mutedUsers = mutedUsers
                )
            }
        } else {
            Log.e("saala", "Current user is not logged in.")
        }
    }

    private fun deleteRecentChat(recentChatId: String) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("RecentChats", "Current user details not loaded.")
            return
        }
        recentChatRepository.deleteRecentChat(
            currentUserId = currentUserId,
            recentChatId = recentChatId
        )
    }

    private fun muteUser(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "Failed to mute user: Invalid user data."
            )
            Log.e("MuteUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "Failed to mute user: Invalid user data."
            )
            Log.e("MuteUser", "No contact Selected")
            return
        }
        userRepository.muteUser(currentUserId, otherUserId) { result, message ->
            if (result) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Successfully muted user."
                )
            } else {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Failed to mute user: $message"
                )
            }
        }
    }

    private fun fetchBlockedAndMutedData(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("MuteUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            Log.e("MuteUser", "No contact Selected")
            return
        }

        _inboxState.value.currentUserContact?.id?.let {
            userRepository.fetchBlockedAndMutedData(
                currentUserId = it,
                otherUserId = otherUserId,
            ) { isBlocked, isMuted ->
                _inboxState.value = _inboxState.value.copy(
                    isSelectedContactBlocked = isBlocked,
                    isSelectedContactMuted = isMuted
                )
            }
        }
    }

    private fun unmuteUser(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("MuteUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            Log.e("MuteUser", "No contact Selected")
            return
        }
        userRepository.unmuteUser(currentUserId, otherUserId) { result, message ->
            if (result) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Successfully unmuted user."
                )
            } else {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Failed to unmute user: $message"
                )
            }
        }
    }



    private fun blockUser(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "Failed to block user: Invalid user data."
            )
            Log.e("BlockUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "Failed to block user: Invalid user data."
            )
            Log.e("BlockUser", "No contact Selected")
            return
        }
        userRepository.blockUser(currentUserId, otherUserId) { result, message ->
            if (result) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Successfully blocked user."
                )
            } else {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Failed to block user: $message"
                )
            }
        }
    }

    private fun unblockUser(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("BlockUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            Log.e("BlockUser", "No contact Selected")
            return
        }
        userRepository.unBlockUser(currentUserId, otherUserId) { result, message ->
            if (result) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Successfully unblocked user."
                )
            } else {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Failed to unblock user: $message"
                )
            }
        }
    }

    private fun addScheduleMeets(
        otherUserContact: Contact?,
        date: String?,
        time: String?
    ) {
        val currentUserContact = _inboxState.value.currentUserContact

        if (currentUserContact == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "CurrentUserId not loaded"
            )
            Log.e("ScheduledMeets", "CurrentUserId not loaded")
            return
        }
        if (otherUserContact == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "No contact selected"
            )
            Log.e("ScheduledMeets", "No contact selected")
            return
        }
        if (date == null || time == null) {
            _inboxState.value = _inboxState.value.copy(
                snackbarMessage = "Date or Time not selected"
            )
            Log.e("ScheduledMeets", "Date or Time not selected")
            return
        }
        userRepository.fetchBlockedAndMutedData(otherUserContact.id, currentUserContact.id) { blocked, muted ->
            if (blocked) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Error Scheduling Meet"
                )
            } else {
                checkRecipientExists(otherUserContact.id) { exists ->
                    meetRepository.addSchedule(
                        currentUserContact = currentUserContact,
                        otherUserContact = otherUserContact,
                        ifOtherUserExists = exists,
                        date = date,
                        time = time,
                        isUserMuted = muted
                    ) { result, message ->
                        if (result) {
                            _inboxState.value = _inboxState.value.copy(
                                snackbarMessage = "Meet successfully scheduled"
                            )
                        } else {
                            _inboxState.value = _inboxState.value.copy(
                                snackbarMessage = "Error Scheduling Meet: $message"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            val filteredContacts = _inboxState.value.contacts.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query)
            }
            _inboxState.value = _inboxState.value.copy(
                searchQuery = query,
                searchResults = filteredContacts
            )
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _inboxState.value = _inboxState.value.copy(isLoadingContacts = true)
            try {
                val contactsList = mutableListOf<LocalContact>()
                val cursor = appContext.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                    ),
                    null,
                    null,
                    null
                )

                cursor?.use { contactsCursor ->
                    val nameIndex =
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex =
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoUriIndex =
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val photoThumbnailUriIndex =
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                    val uniqueContacts = mutableSetOf<String>()
                    while (contactsCursor.moveToNext()) {
                        val name = contactsCursor.getString(nameIndex)
                        val number = contactsCursor.getString(numberIndex)
                        val uniqueId = HashUtil.hashPhoneNumber(number)

                        if (uniqueContacts.add(uniqueId)) {
                            val photoUri = contactsCursor.getString(photoUriIndex)
                            val photoThumbnailUri = contactsCursor.getString(photoThumbnailUriIndex)

                            contactsList.add(
                                LocalContact(
                                    id = uniqueId,
                                    displayName = name,
                                    phoneNumber = number,
                                    photoUri = photoUri,
                                    photoThumbnailUri = photoThumbnailUri
                                )
                            )
                        }
                    }
                }
                val dbContacts = localRepository.getContacts()

                val contactsToDelete = dbContacts.filter { dbContact ->
                    contactsList.none { it.id == dbContact.id }
                }

                if (contactsToDelete.isNotEmpty()) {
                    localRepository.deleteContacts(contactsToDelete)
                }

                localRepository.insertContacts(contactsList)

                val contactList = contactsList.map { localContact ->
                    Contact(
                        id = localContact.id,
                        displayName = localContact.displayName,
                        phoneNumber = localContact.phoneNumber,
                        photoUri = localContact.photoUri,
                        photoThumbnailUri = localContact.photoThumbnailUri
                    )
                }

                withContext(Dispatchers.Main) {
                    _inboxState.value = _inboxState.value.copy(contacts = contactList, isLoadingContacts = false)
                }

                loadRecentChats()
            } catch (e: Exception) {
                Log.e("noooo", "Error loading contacts", e)
                _inboxState.value = _inboxState.value.copy(snackbarMessage = "Error loading contacts")
            }
        }
    }

    private fun createChatRoom() {
        val initiatorUser = _inboxState.value.currentUserContact
        val recipientUser = _inboxState.value.selectedContact

        if (initiatorUser == null) {
            Log.e("InboxViewModel", "Current user details not loaded.")
            Log.d("InboxViewModel", "${_inboxState.value}")
            return
        }

        if (recipientUser == null) {
            Log.e("InboxViewModel", "No contact selected.")
            return
        }

        userRepository.fetchBlockedAndMutedData(recipientUser.id, initiatorUser.id) { blocked, muted ->
            if (blocked) {
                _inboxState.value = _inboxState.value.copy(
                    snackbarMessage = "Error Creating room"
                )
            } else {
                checkRecipientExists(recipientUser.id) { exists ->
                    appRepository.createChatRoom(
                        initiatorUser = initiatorUser,
                        recipientUser = recipientUser,
                        recipientUserExists = exists,
                        notifyOtherUser = !muted,
                        context = appContext,
                        isGuest = false
                    ) { chatRoomId ->
                        if (chatRoomId != null) {
                            _inboxState.value = _inboxState.value.copy(
                                isEnterChatRoom = true,
                                navigateToChatId = chatRoomId
                            )
                        } else {
                            Log.e("InboxViewModel", "Failed to create chat room.")
                        }
                    }
                }
            }
        }
    }

    private fun checkRecipientExists(userId: String, onResult: (Boolean) -> Unit) {
        userRepository.getUserDetails(userId) { contact ->
            if (contact != null) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}

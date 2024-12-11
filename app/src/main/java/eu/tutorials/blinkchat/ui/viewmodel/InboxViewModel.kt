package eu.tutorials.blinkchat.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.local.LocalContact
import eu.tutorials.blinkchat.data.datasource.local.LocalContactDao
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.LocalRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.RecentChatRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.RecentChatContact
import eu.tutorials.blinkchat.data.state.InboxState
import eu.tutorials.blinkchat.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    private companion object {
        const val REQUEST_CODE_READ_CONTACTS = 1001
    }

    private val _inboxState = MutableStateFlow(InboxState())
    val inboxState: StateFlow<InboxState> = _inboxState

    init {
        loadCurrentUser()
    }

    fun onEvent(event: InboxEvent) {
        when (event) {
            is InboxEvent.LoadContacts -> {
                loadContacts(event.activity)
            }

            is InboxEvent.OnContactClicked -> {
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = true,
                    selectedContact = event.contact
                )
                checkIfUserIsBlocked(event.contact.id) { isBlocked ->
                    _inboxState.value = _inboxState.value.copy(
                        isSelectedContactBlocked = isBlocked
                    )
                }
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
                    isEnterChatRoom = false
                )
            }

            InboxEvent.LoadRecentChats -> {
                loadRecentChats()
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

            InboxEvent.OnClearError -> {
                _inboxState.value = _inboxState.value.copy(error = null)
            }
        }
    }

    private fun loadCurrentUser() {
        val currentUserId = userRepository.currentUserId()
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
                    Log.d("InboxViewModel", "currentUserContact: ${_inboxState.value.currentUserContact}")
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
                val allContacts = _inboxState.value.contacts
                Log.d("saala", "allContacts: ${_inboxState.value.contacts}")

                val matchedRecentContacts = recentChatUserIds.mapNotNull { (userId, recentChatId) ->
                    val contact = allContacts.find { it.id == userId }
                    if (contact != null) {
                        RecentChatContact(recentChatId = recentChatId, contact = contact)
                    } else {
                        null
                    }
                }

                Log.d("saala", "matchedRecentContacts: $matchedRecentContacts")

                _inboxState.value = _inboxState.value.copy(
                    recentContacts = matchedRecentContacts
                )
            }

            Log.d("saala", "recentContacts: ${_inboxState.value.recentContacts}")

            recentChatRepository.listenForPresence(currentUserId) { activeUserNames ->
                _inboxState.value = _inboxState.value.copy(
                    usersInChatRoom = activeUserNames.filterNotNull()
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

    private fun blockUser(otherUserId: String?) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("BlockUser", "Current user details not loaded.")
            return
        }
        if (otherUserId == null) {
            Log.e("BlockUser", "No contact Selected")
            return
        }
        userRepository.blockUser(currentUserId, otherUserId)
    }

    private fun checkIfUserIsBlocked(
        otherUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUserId = _inboxState.value.currentUserContact?.id
        if (currentUserId == null) {
            Log.e("BlockUser", "Current user details not loaded.")
            onResult(false)
            return
        }
        userRepository.checkIfUserIsBlocked(currentUserId, otherUserId) { result ->
            onResult(result)
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
        userRepository.unBlockUser(currentUserId, otherUserId)
    }

    private fun addScheduleMeets(
        otherUserContact: Contact?,
        date: String?,
        time: String?
    ) {
        val currentUserContact = _inboxState.value.currentUserContact

        if (currentUserContact == null) {
            Log.e("ScheduledMeets", "CurrentUserId not loaded")
            return
        }
        if (otherUserContact == null) {
            Log.e("ScheduledMeets", "No contact selected")
            return
        }
        if (date == null || time == null) {
            Log.e("ScheduledMeets", "Date or Time not selected")
            return
        }
        userRepository.checkIfUserIsBlocked(otherUserContact.id, currentUserContact.id) { result ->
            if (result) {
                _inboxState.value = _inboxState.value.copy(
                    error = "Error Scheduling Meet"
                )
            } else {
                checkRecipientExists(otherUserContact.id) { exists ->
                    meetRepository.addSchedule(
                        currentUserContact = currentUserContact,
                        otherUserContact = otherUserContact,
                        ifOtherUserExists = exists,
                        date = date,
                        time = time
                    )
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

    private fun loadContacts(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
            return
        }
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch {
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
                    val nameIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoUriIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val photoThumbnailUriIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

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
                    _inboxState.value = _inboxState.value.copy(contacts = contactList)
                    Log.d("saala", "contacts: ${_inboxState.value.contacts}")
                }

                loadRecentChats()

                Log.d("noooo", "Contacts loaded and saved: ${contactsList.size}")
                Log.d("noooo", "size: ${localRepository.getContacts().size}")
            } catch (e: Exception) {
                Log.e("noooo", "Error loading contacts", e)
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

        userRepository.checkIfUserIsBlocked(recipientUser.id, initiatorUser.id) { result ->
           if (result) {
               _inboxState.value  = _inboxState.value.copy(
                   error = "Error Creating room"
               )
           } else {
               checkRecipientExists(recipientUser.id) { exists ->
                   appRepository.createChatRoom(
                       initiatorUser = initiatorUser,
                       recipientUser = recipientUser,
                       recipientUserExists = exists,
                       context = appContext
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

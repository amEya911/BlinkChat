package eu.tutorials.blinkchat.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.state.InboxState
import eu.tutorials.blinkchat.ui.component.HashUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val context: Context,
    private val appRepository: AppRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private companion object {
        const val REQUEST_CODE_READ_CONTACTS = 1001
    }

    private val _inboxState = MutableStateFlow(InboxState())
    val inboxState: StateFlow<InboxState> = _inboxState

    init {
        loadCurrentUser()
        loadRecentChats()
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
            userRepository.listenToRecentChats(currentUserId) { recentChats ->
                _inboxState.value = _inboxState.value.copy(
                    recentContacts = recentChats
                )
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
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
            return
        }

        viewModelScope.launch {
            val contactsList = mutableListOf<Contact>()
            val cursor = context.contentResolver.query(
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
                            Contact(
                                id = uniqueId,
                                displayName = name,
                                phoneNumber = number,
                                photoThumbnailUri = photoThumbnailUri,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
            _inboxState.value = InboxState(contacts = contactsList)
        }
    }

    private fun createChatRoom() {
        val initiatorUser = _inboxState.value.currentUserContact
        val recipientUser = _inboxState.value.selectedContact

        if (initiatorUser == null) {
            Log.e("ChatRoom", "Current user details not loaded.")
            Log.d("ChatRoom", "${_inboxState.value}")
            return
        }

        if (recipientUser == null) {
            Log.e("ChatRoom", "No contact selected.")
            return
        }

        checkRecipientExists(recipientUser.id) { exists ->
            appRepository.createChatRoom(
                initiatorUser = initiatorUser,
                recipientUser = recipientUser,
                recipientUserExists = exists,
                context = context
            ) { chatRoomId ->
                if (chatRoomId != null) {
                    _inboxState.value = _inboxState.value.copy(
                        isEnterChatRoom = true,
                        navigateToChatId = chatRoomId
                    )
                } else {
                    Log.e("ChatRoom", "Failed to create chat room.")
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

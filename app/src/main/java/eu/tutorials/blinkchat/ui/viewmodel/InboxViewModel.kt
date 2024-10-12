package eu.tutorials.blinkchat.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.blinkchat.data.event.InboxEvent
import eu.tutorials.blinkchat.data.model.ContactModel
import eu.tutorials.blinkchat.data.state.InboxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(private val context: Context) : ViewModel() {

    private companion object {
        const val REQUEST_CODE_READ_CONTACTS = 1001
    }


    private val _inboxState = MutableStateFlow(InboxState())
    val inboxState: StateFlow<InboxState> = _inboxState

    fun onEvent(event: InboxEvent) {
        when (event) {
            is InboxEvent.LoadContacts -> {
                loadContacts(event.activity)
            }

            is InboxEvent.SearchUsers -> {
                searchUsers(event.searchQuery)
            }

            is InboxEvent.OnContactClicked -> {
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = true,
                    selectedContact = event.contact
                )
            }

            InboxEvent.OnContactDismiss -> {
                _inboxState.value = _inboxState.value.copy(
                    isContactClicked = false
                )
            }

            is InboxEvent.StartChatWithContact -> {

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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
        }

        viewModelScope.launch {
            val contactsList = mutableListOf<ContactModel>()
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
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
                val idIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val photoThumbnailUriIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                val uniqueContacts = mutableSetOf<String>()
                while (contactsCursor.moveToNext()) {
                    val id = contactsCursor.getString(idIndex)
                    if (uniqueContacts.add(id)) {
                        val name = contactsCursor.getString(nameIndex)
                        val number = contactsCursor.getString(numberIndex)
                        val photoUri = contactsCursor.getString(photoUriIndex)
                        val photoThumbnailUri = contactsCursor.getString(photoThumbnailUriIndex)

                        contactsList.add(
                            ContactModel(
                                id = id,
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


}




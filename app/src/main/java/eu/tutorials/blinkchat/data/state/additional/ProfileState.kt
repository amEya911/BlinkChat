package eu.tutorials.blinkchat.data.state.additional

import eu.tutorials.blinkchat.data.model.Contact

data class ProfileState(
    val currentUserContact: Contact = Contact("", "", "", null, null),
    val isNameClicked: Boolean = false
)
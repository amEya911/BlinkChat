package eu.tutorials.blinkchat.data.state

data class GuestState(
    val initiatorId: String = "",
    val recipientId: String = "",
    val chatRoomId: String = "",
    val roomLink: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
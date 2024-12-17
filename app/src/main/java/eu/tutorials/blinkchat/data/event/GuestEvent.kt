package eu.tutorials.blinkchat.data.event

import android.content.Context

sealed class GuestEvent {
    data class OnCreateRoom(val context: Context) : GuestEvent()
}
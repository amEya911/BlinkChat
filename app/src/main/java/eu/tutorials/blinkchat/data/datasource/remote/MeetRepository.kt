package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.util.Crypto
import eu.tutorials.blinkchat.util.NotificationType
import java.util.UUID
import javax.inject.Inject

class MeetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) {
    private val secretKey = Crypto.stringToKey("YourSharedKeyString")

    fun addSchedule(
        currentUserContact: Contact,
        otherUserContact: Contact,
        ifOtherUserExists: Boolean,
        date: String,
        time: String,
        isUserMuted: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        val meetingId = UUID.randomUUID().toString()
        val scheduledMeetEntry = mapOf(
            "meetingId" to meetingId,
            "createdBy" to currentUserContact,
            "createdWith" to otherUserContact,
            "date" to date,
            "time" to time,
            "createdAt" to System.currentTimeMillis()
        )

        addScheduleToUser(
            currentUserContact.id,
            scheduledMeetEntry,
            otherUserContact.displayName,
            date,
            time,
            onResult
        )

        if (ifOtherUserExists) {
            addScheduleToUser(
                otherUserContact.id,
                scheduledMeetEntry,
                currentUserContact.displayName,
                date,
                time,
                onResult
            )
            if (!isUserMuted) {
                notificationRepository.notifyOtherUser(
                    currentUserId = currentUserContact.id,
                    otherUserId = otherUserContact.id,
                    title = "New Schedule Created",
                    body = currentUserContact.id,
                    type = NotificationType.ScheduleMeet.type,
                    deepLink = "https://vanishtest.netlify.app"
                )
            }
        }
    }

    private fun addScheduleToUser(
        userId: String,
        scheduledMeetEntry: Map<String, Any>,
        otherUserDisplayName: String,
        date: String,
        time: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            val encryptedMeetEntry = scheduledMeetEntry.mapValues { (key, value) ->
                when (key) {
                    "meetingId" -> value
                    "createdBy", "createdWith" -> Crypto.encryptContact(value as Contact, secretKey)
                    else -> if (value is String) Crypto.encrypt(value, secretKey) else value
                }
            }

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val scheduledMeets =
                            document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()
                        val newScheduledMeets = scheduledMeets + encryptedMeetEntry

                        firestore.collection("users").document(userId)
                            .update("scheduledMeets", newScheduledMeets)
                            .addOnSuccessListener {
                                Log.d(
                                    "ScheduledMeets",
                                    "ScheduledMeet successfully added for $otherUserDisplayName " +
                                            "on $date " +
                                            "at $time"
                                )
                                onResult(true, null)
                            }.addOnFailureListener { e ->
                                onResult(false, e.localizedMessage)
                                Log.e("ScheduledMeets", "Failed to add Scheduled Meet for $userId")
                            }
                    } else {
                        firestore.collection("users").document(userId)
                            .set(mapOf("scheduledMeets" to listOf(encryptedMeetEntry)))
                            .addOnSuccessListener {
                                Log.d(
                                    "ScheduledMeets",
                                    "ScheduledMeet successfully added for $otherUserDisplayName " +
                                            "on $date " +
                                            "at $time"
                                )
                                onResult(true, null)
                            }.addOnFailureListener { e ->
                                onResult(false, e.localizedMessage)
                                Log.e("ScheduledMeets", "Failed to add Scheduled Meet for $userId")
                            }
                    }
                }.addOnFailureListener { e ->
                    onResult(false, e.localizedMessage)
                    Log.e("ScheduledMeets", "Failed to retrieve user document: $userId", e)
                }
        } catch (e: Exception) {
            onResult(false, e.message)
            Log.e("ScheduledMeets", "Encryption failed", e)
        }
    }

    fun listenForMeetings(
        currentUserId: String,
        onResult: (List<Meeting>) -> Unit
    ) {
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    Log.e(
                        "ScheduledMeets",
                        "Error listening for updates: ${exception.message}",
                        exception
                    )
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val meets = document["scheduledMeets"] as? List<Map<String, Any>> ?: emptyList()

                    val meetingDetails = meets.mapNotNull { meet ->
                        try {
                            val meetingId = meet["meetingId"] as? String
                            val createdByEncrypted = meet["createdBy"] as? String
                            val createdWithEncrypted = meet["createdWith"] as? String
                            val date = Crypto.decrypt(meet["date"] as? String ?: "", secretKey)
                            val time = Crypto.decrypt(meet["time"] as? String ?: "", secretKey)
                            val createdAt = meet["createdAt"] as? Long ?: 0L

                            val createdBy = createdByEncrypted?.let {
                                Crypto.decryptContact(it, secretKey)
                            }

                            val createdWith = createdWithEncrypted?.let {
                                Crypto.decryptContact(it, secretKey)
                            }

                            Log.d(
                                "ScheduledMeets",
                                "Decrypted createdBy: $createdBy" +
                                        " Decrypted createdWith: $createdWith" +
                                        " Decrypted date: $date" +
                                        " Decrypted time: $time"
                            )

                            if (meetingId != null) {
                                if (createdBy?.id != currentUserId) {
                                    createdBy?.let {
                                        Meeting(meetingId, it, it, date, time, createdAt)
                                    }
                                } else if (createdWith?.id != currentUserId) {
                                    createdWith?.let {
                                        Meeting(meetingId, createdBy, it, date, time, createdAt)
                                    }
                                } else {
                                    null
                                }
                            } else {
                                Log.e("ScheduledMeets", "Decrypted meetingId is empty")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("ScheduledMeets", "Decryption failed", e)
                            null
                        }
                    }

                    onResult(meetingDetails)
                } else {
                    Log.w(
                        "ScheduledMeets",
                        "Document does not exist or has no scheduledMeets field."
                    )
                    onResult(emptyList())
                }
            }
    }

    fun rescheduleMeet(
        meetingId: String,
        currentUserId: String,
        otherUserId: String,
        newDate: String,
        newTime: String,
        isUserMuted: Boolean,
        isBlocked: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        var completed = 0
        var success = true
        var errorMessage: String? = null

        // Function to handle the result of rescheduling for each user
        val handleResult: (Boolean, String?) -> Unit = { result, message ->
            completed++
            if (!result) {
                success = false
                errorMessage = message ?: "Unknown error"
            }

            // If both operations are completed, update the final result
            if (completed == 2) {
                if (success) {
                    onResult(true, null) // Both users succeeded
                } else {
                    onResult(false, errorMessage) // At least one failed
                }
            }
        }
        rescheduleMeetForUser(meetingId, currentUserId, newDate, newTime, handleResult)
        if (!isBlocked) {
            rescheduleMeetForUser(meetingId, otherUserId, newDate, newTime, handleResult)
        }
        if (!isUserMuted) {
            notificationRepository.notifyOtherUser(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                title = "Meet Rescheduled",
                body = currentUserId,
                type = NotificationType.RescheduleMeet.type,
                deepLink = "https://vanishtest.netlify.app"
            )
        }
    }

    private fun rescheduleMeetForUser(
        meetingId: String,
        userId: String,
        newDate: String,
        newTime: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val encryptedNewDate = Crypto.encrypt(newDate, secretKey)
        val encryptedNewTime = Crypto.encrypt(newTime, secretKey)

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val scheduledMeets =
                        document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()

                    val updatedMeets = scheduledMeets.map { meet ->
                        if (meet["meetingId"] == meetingId) {
                            meet.toMutableMap().apply {
                                this["date"] = encryptedNewDate
                                this["time"] = encryptedNewTime
                                this["createdAt"] = System.currentTimeMillis()
                            }
                        } else {
                            meet
                        }
                    }

                    firestore.collection("users").document(userId)
                        .update("scheduledMeets", updatedMeets)
                        .addOnSuccessListener {
                            onResult(true, null)
                            Log.d(
                                "ScheduledMeets",
                                "Meeting successfully rescheduled for $userId to $newDate at $newTime"
                            )
                        }.addOnFailureListener { e ->
                            onResult(false, e.localizedMessage)
                            Log.e("ScheduledMeets", "Failed to reschedule meeting for $userId", e)
                        }
                } else {
                    onResult(false, "User does not exist")
                    Log.e("ScheduledMeets", "User document does not exist: $userId")
                }
            }.addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("ScheduledMeets", "Error retrieving user document: $userId", e)
            }
    }

    fun deleteMeet(
        meetingId: String,
        currentUserId: String,
        otherUserId: String,
        isUserMuted: Boolean,
        isBlocked: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        var completed = 0
        var success = true
        var errorMessage: String? = null

        val handleResult: (Boolean, String?) -> Unit = { result, message ->
            completed++
            if (!result) {
                success = false
                errorMessage = message ?: "Unknown error"
            }

            if (completed == 2) {
                if (success) {
                    onResult(true, null)
                } else {
                    onResult(false, errorMessage)
                }
            }
        }
        deleteMeetForUser(meetingId, currentUserId, handleResult)
        if (!isBlocked) {
            deleteMeetForUser(meetingId, otherUserId, handleResult)
        }
        if (!isUserMuted) {
            notificationRepository.notifyOtherUser(
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                title = "Meet Deleted",
                body = currentUserId,
                type = NotificationType.DeleteMeet.type,
                deepLink = "https://vanishtest.netlify.app"
            )
        }
    }

    private fun deleteMeetForUser(
        meetingId: String,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val scheduledMeets =
                        document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()

                    val meetToDelete = scheduledMeets.find { it["meetingId"] == meetingId }

                    if (meetToDelete != null) {
                        firestore.collection("users").document(userId)
                            .update("scheduledMeets", FieldValue.arrayRemove(meetToDelete))
                            .addOnSuccessListener {
                                onResult(true, null)
                                Log.d(
                                    "ScheduledMeets",
                                    "Meeting successfully deleted for $userId"
                                )
                            }.addOnFailureListener { e ->
                                onResult(false, e.localizedMessage)
                                Log.e("ScheduledMeets", "Failed to delete meeting for $userId", e)
                            }
                    } else {
                        onResult(false, "Meeting not found")
                        Log.e("ScheduledMeets", "Meeting not found for $meetingId in $userId")
                    }

                } else {
                    onResult(false, "User does not exist")
                    Log.e("ScheduledMeets", "User document does not exist: $userId")
                }

            }.addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
                Log.e("ScheduledMeets", "Error retrieving user document: $userId", e)
            }
    }
}

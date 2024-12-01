package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting
import java.util.UUID
import javax.inject.Inject

class MeetRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun addSchedule(
        currentUserContact: Contact,
        otherUserContact: Contact,
        ifOtherUserExists: Boolean,
        date: String,
        time: String
    ) {
        val meetingId = UUID.randomUUID().toString()
        val scheduledMeetEntry = mapOf(
            "meetingId" to meetingId,
            "createdBy" to currentUserContact,
            "createdWith" to otherUserContact,
            "date" to date,
            "time" to time,
            "createAt" to System.currentTimeMillis()
        )

        addScheduleToUser(
            currentUserContact.id,
            scheduledMeetEntry,
            otherUserContact.displayName,
            date,
            time
        )

        if (ifOtherUserExists) {
            addScheduleToUser(
                otherUserContact.id,
                scheduledMeetEntry,
                currentUserContact.displayName,
                date,
                time
            )
        }
    }

    private fun addScheduleToUser(
        userId: String,
        scheduledMeetEntry: Map<String, Any>,
        otherUserDisplayName: String,
        date: String,
        time: String
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val scheduledMeets =
                        document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()
                    val newScheduledMeets = scheduledMeets + scheduledMeetEntry

                    firestore.collection("users").document(userId)
                        .update("scheduledMeets", newScheduledMeets)
                        .addOnSuccessListener {
                            Log.d(
                                "ScheduledMeets",
                                "ScheduledMeet successfully added for $otherUserDisplayName " +
                                        "on $date " +
                                        "at $time"
                            )
                        }.addOnFailureListener {
                            Log.e("ScheduledMeets", "Failed to add Scheduled Meet for $userId")
                        }
                } else {
                    firestore.collection("users").document(userId)
                        .set(mapOf("scheduledMeets" to listOf(scheduledMeetEntry)))
                        .addOnSuccessListener {
                            Log.d(
                                "ScheduledMeets",
                                "ScheduledMeet successfully added for $otherUserDisplayName " +
                                        "on $date " +
                                        "at $time"
                            )
                        }.addOnFailureListener {
                            Log.e("ScheduledMeets", "Failed to add Scheduled Meet for $userId")
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("ScheduledMeets", "Failed to retrieve user document: $userId", e)
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
                        val meetingId = meet["meetingId"] as? String
                        val createdByMap = meet["createdBy"] as? Map<String, Any>
                        val createdWithMap = meet["createdWith"] as? Map<String, Any>
                        val date = meet["date"] as? String ?: ""
                        val time = meet["time"] as? String ?: ""


                        val createdBy = createdByMap?.let { map ->
                            Contact(
                                id = map["id"] as? String ?: "",
                                displayName = map["displayName"] as? String ?: "",
                                phoneNumber = map["phoneNumber"] as? String ?: "",
                                photoThumbnailUri = map["photoThumbnailUri"] as? String,
                                photoUri = map["photoUri"] as? String
                            )
                        }

                        val createdWith = createdWithMap?.let { map ->
                            Contact(
                                id = map["id"] as? String ?: "",
                                displayName = map["displayName"] as? String ?: "",
                                phoneNumber = map["phoneNumber"] as? String ?: "",
                                photoThumbnailUri = map["photoThumbnailUri"] as? String,
                                photoUri = map["photoUri"] as? String
                            )
                        }

                        Log.d(
                            "ScheduledMeets",
                            "createdBy: $createdBy" +
                                    "createdWith: $createdWith" +
                                    "date: $date" +
                                    "time: $time"
                        )
                        if (meetingId != null) {
                            if (createdBy?.id != currentUserId) {
                                createdBy?.let {
                                    Meeting(meetingId, it, it, date, time)
                                }
                            } else if (createdWith?.id != currentUserId) {
                                createdWith?.let {
                                    Meeting(meetingId, createdBy, it, date, time)
                                }
                            } else {
                                null
                            }
                        } else {
                            Log.e("ScheduledMeets", "meetingId is null")
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
        newTime: String
    ) {
        rescheduleMeetForUser(meetingId, currentUserId, newDate, newTime)
        rescheduleMeetForUser(meetingId, otherUserId, newDate, newTime)
    }

    private fun rescheduleMeetForUser(
        meetingId: String,
        userId: String,
        newDate: String,
        newTime: String
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val scheduledMeets =
                        document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()

                    val updatedMeets = scheduledMeets.map { meet ->
                        if (meet["meetingId"] == meetingId) {
                            meet.toMutableMap().apply {
                                this["date"] = newDate
                                this["time"] = newTime
                            }
                        } else {
                            meet
                        }
                    }

                    firestore.collection("users").document(userId)
                        .update("scheduledMeets", updatedMeets)
                        .addOnSuccessListener {
                            Log.d(
                                "ScheduledMeets",
                                "Meeting successfully rescheduled for $userId to $newDate at $newTime"
                            )
                        }.addOnFailureListener { e ->
                            Log.e("ScheduledMeets", "Failed to reschedule meeting for $userId", e)
                        }
                } else {
                    Log.e("ScheduledMeets", "User document does not exist: $userId")
                }
            }.addOnFailureListener { e ->
                Log.e("ScheduledMeets", "Error retrieving user document: $userId", e)
            }
    }

    fun deleteMeet(
        meetingId: String,
        currentUserId: String,
        otherUserId: String
    ) {
        deleteMeetForUser(meetingId, currentUserId)
        deleteMeetForUser(meetingId, otherUserId)
    }

    private fun deleteMeetForUser(
        meetingId: String,
        userId: String
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val scheduledMeets =
                        document.get("scheduledMeets") as? List<Map<String, Any>> ?: emptyList()

                    val updatedMeets = scheduledMeets.filterNot { meet ->
                        meet["meetingId"] == meetingId
                    }

                    firestore.collection("users").document(userId)
                        .update("scheduledMeets", updatedMeets)
                        .addOnSuccessListener {
                            Log.d(
                                "ScheduledMeets",
                                "Meeting successfully deleted for $userId"
                            )
                        }.addOnFailureListener { e ->
                            Log.e("ScheduledMeets", "Failed to delete meeting for $userId", e)
                        }

                } else {
                    Log.e("ScheduledMeets", "User document does not exist: $userId")
                }

            }.addOnFailureListener { e ->
                Log.e("ScheduledMeets", "Error retrieving user document: $userId", e)
            }
    }
}

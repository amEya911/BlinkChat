package eu.tutorials.blinkchat.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting
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
        val scheduledMeetEntry = mapOf(
            "createdBy" to currentUserContact,
            "createdWith" to otherUserContact,
            "date" to date,
            "time" to time,
            "createAt" to System.currentTimeMillis()
        )

        addScheduleToUser(currentUserContact.id, scheduledMeetEntry, otherUserContact.displayName, date, time)

        if (ifOtherUserExists) {
            addScheduleToUser(otherUserContact.id, scheduledMeetEntry, currentUserContact.displayName, date, time)
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
                    Log.e("ScheduledMeets", "Error listening for updates: ${exception.message}", exception)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val meets = document["scheduledMeets"] as? List<Map<String, Any>> ?: emptyList()
                    Log.d("ScheduledMeets", "meets: $meets")

                    val meetingDetails = meets.mapNotNull { meet ->
                        val createdByMap = meet["createdBy"] as? Map<String, Any>
                        val createdWithMap = meet["createdWith"] as? Map<String, Any>
                        val date = meet["date"] as? String ?: ""
                        val time = meet["time"] as? String ?: ""

                        Log.d("ScheduledMeets", "createdByMap: $createdByMap createdWithMap: $createdWithMap")

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

                        if (createdBy?.id != currentUserId) {
                            createdBy?.let {
                                Meeting(it, it, date, time)
                            }
                        } else if (createdWith?.id != currentUserId) {
                            createdWith?.let {
                                Meeting(createdBy, it, date, time)
                            }
                        } else {
                            null
                        }
                    }
                    Log.d("ScheduledMeets", "meetingDetails: $meetingDetails")

                    onResult(meetingDetails)
                } else {
                    Log.w("ScheduledMeets", "Document does not exist or has no scheduledMeets field.")
                    onResult(emptyList())
                }
            }
    }
}

package eu.tutorials.blinkchat.data.datasource.local.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_contact")
data class LocalContact(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    @ColumnInfo(name = "photo_thumbnail_uri") val photoThumbnailUri: String?
)

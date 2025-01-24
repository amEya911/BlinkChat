package eu.tutorials.blinkchat.data.datasource.local.contact

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<LocalContact>)

    @Query("SELECT * FROM local_contact")
    suspend fun getAllContacts(): List<LocalContact>

    @Query("DELETE FROM local_contact")
    suspend fun clearContacts()

    @Delete
    suspend fun deleteContacts(contacts: List<LocalContact>)

    @Query("SELECT * FROM local_contact WHERE id = :id")
    suspend fun getContactById(id: String): LocalContact?

}


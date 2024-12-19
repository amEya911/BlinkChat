package eu.tutorials.blinkchat.data.datasource.local.contact

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocalContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): LocalContactDao
}

package eu.tutorials.blinkchat.data.datasource.remote

import eu.tutorials.blinkchat.data.datasource.local.contact.AppDatabase
import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContact
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    suspend fun getContacts(): List<LocalContact> {
        return appDatabase.contactDao().getAllContacts()
    }

    suspend fun insertContacts(contacts: List<LocalContact>) {
        appDatabase.contactDao().insertContacts(contacts)
    }

    suspend fun clearContacts() {
        appDatabase.contactDao().clearContacts()
    }

    suspend fun deleteContacts(contacts: List<LocalContact>) {
        appDatabase.contactDao().deleteContacts(contacts)
    }
}
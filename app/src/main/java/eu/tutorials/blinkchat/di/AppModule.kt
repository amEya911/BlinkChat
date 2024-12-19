package eu.tutorials.blinkchat.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.tutorials.blinkchat.data.datasource.local.contact.AppDatabase
import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContactDao
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.RecentChatRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUsersRepository(): UserRepository {
        return UserRepository(FirebaseFirestore.getInstance())
    }

    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    fun provideAppRepository(
        firestore: FirebaseFirestore,
        recentChatRepository: RecentChatRepository
    ): AppRepository {
        return AppRepository(firestore, recentChatRepository)
    }

    @Provides
    @Singleton
    fun provideMeetRepository(
        firestore: FirebaseFirestore
    ): MeetRepository {
        return MeetRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "contacts-database"
        ).build()
    }

    @Provides
    fun provideContactDao(appDatabase: AppDatabase): LocalContactDao {
        return appDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideRecentChatRepository(
        firestore: FirebaseFirestore
    ): RecentChatRepository {
        return RecentChatRepository(firestore)
    }
}

package eu.tutorials.blinkchat.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.tutorials.blinkchat.data.datasource.local.contact.AppDatabase
import eu.tutorials.blinkchat.data.datasource.local.contact.LocalContactDao
import eu.tutorials.blinkchat.data.datasource.local.notification.AppLifecycleObserver
import eu.tutorials.blinkchat.data.datasource.local.notification.FcmApi
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.NotificationsTypePreferences
import eu.tutorials.blinkchat.data.datasource.local.sharedpreference.ThemePreferences
import eu.tutorials.blinkchat.data.datasource.remote.AppRepository
import eu.tutorials.blinkchat.data.datasource.remote.MeetRepository
import eu.tutorials.blinkchat.data.datasource.remote.NotificationRepository
import eu.tutorials.blinkchat.data.datasource.remote.RecentChatRepository
import eu.tutorials.blinkchat.data.datasource.remote.UserRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi {
        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }

    @Provides
    fun provideAppLifecycleObserver(): AppLifecycleObserver {
        return AppLifecycleObserver()
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideNotificationsTypePreferences(@ApplicationContext context: Context): NotificationsTypePreferences {
        return NotificationsTypePreferences(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        fcmApi: FcmApi
    ): NotificationRepository {
        return NotificationRepository(firestore, fcmApi)
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
        recentChatRepository: RecentChatRepository,
        notificationRepository: NotificationRepository
    ): AppRepository {
        return AppRepository(firestore, recentChatRepository, notificationRepository)
    }

    @Provides
    @Singleton
    fun provideUsersRepository(
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository,
        appRepository: AppRepository
    ): UserRepository {
        return UserRepository(firestore, notificationRepository, appRepository)
    }

    @Provides
    @Singleton
    fun provideMeetRepository(
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository
    ): MeetRepository {
        return MeetRepository(firestore, notificationRepository)
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

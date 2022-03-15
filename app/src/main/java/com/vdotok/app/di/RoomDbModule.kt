package com.vdotok.app.di

import android.content.Context
import androidx.room.Room
import com.vdotok.app.dao.CallHistoryDao
import com.vdotok.app.dao.ChatDao
import com.vdotok.app.dao.GroupsDao
import com.vdotok.app.dao.UserDao
import com.vdotok.app.roomDB.AppDatabase
import com.vdotok.app.roomDB.AppDatabase.Companion.migration1_2
import com.vdotok.app.roomDB.AppDatabase.Companion.migration2_3
import com.vdotok.app.roomDB.AppDatabase.Companion.migration3_4
import com.vdotok.app.roomDB.AppDatabase.Companion.migration5_6
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RoomDbModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "vdotokCombineDatabase"
        )
            .addMigrations(migration1_2)
            .addMigrations(migration2_3)
            .addMigrations(migration3_4)
            .addMigrations(migration5_6)
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.usersDao()
    }

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupsDao {
        return database.groupsDao()
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideCallHistoryDao(database: AppDatabase): CallHistoryDao {
        return database.callHistoryDao()
    }


}
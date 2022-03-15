package com.vdotok.app.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vdotok.app.dao.CallHistoryDao
import com.vdotok.app.dao.ChatDao
import com.vdotok.app.dao.GroupsDao
import com.vdotok.app.dao.UserDao
import com.vdotok.app.models.CallHistoryDetails
import com.vdotok.app.models.ChatModel
import com.vdotok.network.converters.DataTypeConverter
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.UserModel

@Database(entities = [UserModel::class, GroupModel::class, ChatModel::class, CallHistoryDetails::class], version = 6, exportSchema = false)
@TypeConverters(DataTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun usersDao(): UserDao
    abstract fun groupsDao(): GroupsDao
    abstract fun chatDao(): ChatDao
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        val migration1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            //database.execSQL("ALTER TABLE 'CallHistory' ADD COLUMN 'groupAutoCreated' TEXT")
            }
        }
        val migration2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE 'CallHistory' ADD COLUMN 'profilePic' TEXT")
            }
        }
        val migration3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE 'CallHistory' ADD COLUMN 'participantRefId' TEXT")
            }
        }
        val migration5_6 = object : Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'CallHistory' ADD COLUMN 'groupAutoCreatedValue' TEXT")
            }
        }

    }

}
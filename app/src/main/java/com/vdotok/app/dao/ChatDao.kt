package com.vdotok.app.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import com.vdotok.app.models.ChatModel

@Dao
interface ChatDao {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChat(chatModel: ChatModel)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChat(chatModel: ArrayList<ChatModel>)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT * FROM ChatData Where groupId = :groupId ORDER BY createdAt ASC")
    fun getChat(groupId: Int): LiveData<List<ChatModel>>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT * FROM ChatData Where groupId = :groupId ORDER BY createdAt ASC")
    fun getChatList(groupId: Int): List<ChatModel>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("DELETE from ChatData Where groupId = :groupId")
    suspend fun deleteChat(groupId: Int)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Update
    suspend fun updateChat(chatModel: ChatModel)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("UPDATE ChatData SET status = :messageStatus Where messageId = :messageId")
    suspend fun updateChatStatus(messageStatus: Int, messageId: String)

}
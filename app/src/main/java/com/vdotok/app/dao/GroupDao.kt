package com.vdotok.app.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import com.vdotok.network.models.GroupModel

@Dao
interface GroupsDao {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroups(groupModel: GroupModel)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groupModelList: ArrayList<GroupModel>)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT * FROM Groups ORDER BY groupId")
    fun getGroups(): LiveData<List<GroupModel>>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT * FROM Groups ORDER BY groupId")
    fun getGroupList(): List<GroupModel>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT groupId FROM Groups WHERE channelKey = :channelKey LIMIT 1")
    fun getGroupId(channelKey: String): Int

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("DELETE from Groups Where groupId = :groupId")
    suspend fun deleteGroup(groupId: Int)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("DELETE from Groups")
    suspend fun deleteAllGroups()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Update
    suspend fun updateGroup(groupModel: GroupModel)

}
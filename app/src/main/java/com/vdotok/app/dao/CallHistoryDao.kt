package com.vdotok.app.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vdotok.app.models.CallHistoryData
import com.vdotok.app.models.CallHistoryDetails

@Dao
interface CallHistoryDao {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallHistory(callHistoryDetails: CallHistoryDetails)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallHistory(callHistoryList: ArrayList<CallHistoryDetails>)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("DELETE from CallHistory Where id = :historyId")
    suspend fun deleteCallHistoryItem(historyId: Int)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("DELETE from CallHistory")
    suspend fun deleteCallHistory()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("UPDATE CallHistory SET callStatus = :callStatus Where sessionId = :sessionId")
    suspend fun updateCallStatus(callStatus: String, sessionId: String)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT CallHistory.*,Users.profilePic FROM CallHistory LEFT JOIN Users On Users.refId = CallHistory.participantRefId ORDER BY CallHistory.id DESC")
    fun getImageUpdated(): LiveData<List<CallHistoryData>>

}
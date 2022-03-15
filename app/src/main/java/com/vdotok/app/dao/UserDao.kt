package com.vdotok.app.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import com.vdotok.network.models.UserModel

@Dao
interface UserDao {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(userModel: UserModel)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(userModelList: ArrayList<UserModel>)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Query("SELECT * FROM Users ORDER BY userId")
    fun getUsersList(): LiveData<List<UserModel>>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Delete
    suspend fun deleteUserModel(userModel: UserModel)

}
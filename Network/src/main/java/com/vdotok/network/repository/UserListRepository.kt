package com.vdotok.network.repository

import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.models.CreateGroupResponse
import com.vdotok.network.models.GetAllUsersResponseModel
import com.vdotok.network.network.Result
import com.vdotok.network.network.api.APIService
import com.vdotok.network.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 11/5/21 At 5:45 PM in 2021
 */
class UserListRepository  @Inject constructor(
    private val apiService: APIService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllUsers(token: String): Result<GetAllUsersResponseModel> {
        return safeApiCall(dispatcher) {
            apiService.getAllUsers(token)
        }
    }

    suspend fun createGroup(token: String, model: CreateGroupModel): Result<CreateGroupResponse> {
        return safeApiCall(dispatcher) {
            apiService.createGroup(token, model)
        }
    }
}
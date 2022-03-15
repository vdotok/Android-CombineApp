package com.vdotok.network.repository

import com.vdotok.network.models.*
import com.vdotok.network.network.Result
import com.vdotok.network.network.api.ApiService
import com.vdotok.network.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ProfileRepository @Inject constructor(
        private val apiService: ApiService,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun uploadImage(file: MultipartBody.Part?, type: RequestBody, auth_token: RequestBody): Result<ProfileImageResponse> {
        return safeApiCall(dispatcher) {
            apiService.uploadImage(file,type,auth_token)
        }
    }

}
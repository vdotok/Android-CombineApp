package com.vdotok.app.feature.profile.viewmodel

import androidx.lifecycle.liveData
import com.vdotok.app.base.BaseViewModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * Created By: VdoTok
 * Date & Time: On 22/11/2021 At 7:01 PM in 2021
 */

@HiltViewModel
class ProfileViewModel @Inject constructor(var repo: ProfileRepository,
) : BaseViewModel() {
    fun profileImage(file: MultipartBody.Part, type: RequestBody, auth_token: RequestBody) = liveData {
        emit(Result.Loading)
        emit(repo.uploadImage(file,type,auth_token))
    }

}
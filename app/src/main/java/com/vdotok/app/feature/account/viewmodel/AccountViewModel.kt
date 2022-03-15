package com.vdotok.app.feature.account.viewmodel

import androidx.lifecycle.liveData
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.utils.SDK_PROJECT_ID
import com.vdotok.network.models.CheckUserModel
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:25 PM in 2021
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    var repo: AccountRepository,
) : BaseViewModel() {

    fun loginUser(email: String, password: String) = liveData {
        emit(Result.Loading)
        emit(repo.login(LoginUserModel(email, password, SDK_PROJECT_ID)))
    }

    fun checkEmailAlreadyExist(email: String) = liveData {
        emit(Result.Loading)
        emit(repo.emailAlreadyExist(CheckUserModel(email)))
    }

    fun signUp(signup: SignUpModel) = liveData {
        emit(Result.Loading)
        emit(repo.signUp(signup))
    }
}
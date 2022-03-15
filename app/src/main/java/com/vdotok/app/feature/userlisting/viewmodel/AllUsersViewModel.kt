package com.vdotok.app.feature.userlisting.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.dao.UserDao
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.models.UserModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.UserListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created By: VdoTok
 * Date & Time: On 22/11/2021 At 7:01 PM in 2021
 */

@HiltViewModel
class AllUsersViewModel @Inject constructor(
    var userRepo: UserListRepository,
    override var userDao: UserDao
) : BaseViewModel() {

//    API Methods
    fun getAllUsers(token: String) = liveData {
        emit(Result.Loading)
        emit(userRepo.getAllUsers(token))
    }

    fun createGroup(token: String, model: CreateGroupModel) = liveData {
        emit(Result.Loading)
        emit(userRepo.createGroup(token, model))
    }

//    DAO Methods

    fun updateUsersList(usersList: ArrayList<UserModel>) {
        viewModelScope.launch {
            userDao.insertUsers(usersList)
        }
    }

    fun getUsersData(): LiveData<List<UserModel>> {
        return userDao.getUsersList()
    }

}
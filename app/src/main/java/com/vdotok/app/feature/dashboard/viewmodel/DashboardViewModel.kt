package com.vdotok.app.feature.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.base.UserPreferences.userData
import com.vdotok.app.dao.ChatDao
import com.vdotok.app.dao.GroupsDao
import com.vdotok.app.models.ChatModel.Companion.toChatModel
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.Presence
import com.vdotok.network.models.*
import com.vdotok.network.network.Result
import com.vdotok.network.repository.GroupRepository
import com.vdotok.network.repository.UserListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:41 AM in 2021
 */

@HiltViewModel
class DashboardViewModel @Inject constructor(
    var groupRepo: GroupRepository,
    var groupsDao: GroupsDao,
    var chatDao: ChatDao,
    var userRepo: UserListRepository,
) : BaseViewModel() {

    var savedPresenceList: ArrayList<Presence> = ArrayList()

    /**
     * method to get data related to login user
     */
    fun getUserData(): LoginResponse {
        return (userData as LoginResponse)
    }

    fun getUserName(): String {
        return (userData as LoginResponse).fullName ?: kotlin.run {
            "Anonymous"
        }
    }

    //    API Methods
    fun getAllGroups(token: String) = liveData {
        emit(Result.Loading)
        emit(groupRepo.getAllGroups(token))
    }

    fun updateGroupName(token: String, updateGroupNameModel: UpdateGroupNameModel) = liveData {
        emit(Result.Loading)
        emit(groupRepo.updateGroupName(token, updateGroupNameModel))
    }

    fun deleteGroup(token: String, deleteGroupModel: DeleteGroupModel) = liveData {
        emit(Result.Loading)
        emit(groupRepo.deleteGroup(token, deleteGroupModel))
    }

    fun getAllUsers(token: String) = liveData {
        emit(Result.Loading)
        emit(userRepo.getAllUsers(token))
    }

    //    DAO Methods

    fun updateGroupListing(groupsList: ArrayList<GroupModel>) {
        viewModelScope.launch {
            groupsDao.insertGroups(groupsList)
        }
    }

    fun updateGroupData(groupModel: GroupModel) {
        viewModelScope.launch {
            groupsDao.updateGroup(groupModel)
        }
    }

    fun getGroupList(): List<GroupModel> {
        return groupsDao.getGroupList()
    }

    fun deleteGroupData(groupId: Int) {
        viewModelScope.launch {
            groupsDao.deleteGroup(groupId)
        }
    }

    fun getGroupsData(): LiveData<List<GroupModel>> {
        return groupsDao.getGroups()
    }

    fun getGroupId(channelKey: String): Int {
        return groupsDao.getGroupId(channelKey)
    }

    fun insertChatModel(message: Message, groupId: Int, fileUri: String) {
        viewModelScope.launch {
            chatDao.insertChat(message.toChatModel(groupId, fileUri))
        }
    }

    fun updateChatStatus(messageId: String, messageStatus: Int) {
        viewModelScope.launch {
            chatDao.updateChatStatus(messageStatus, messageId)
        }
    }

    fun deleteChat(groupId: Int) {
        viewModelScope.launch {
            chatDao.deleteChat(groupId)
        }
    }

    fun updateUsersList(usersList: ArrayList<UserModel>) {
        viewModelScope.launch {
            userDao.insertUsers(usersList)
        }
    }

    fun getUsersData(): LiveData<List<UserModel>> {
        return userDao.getUsersList()
    }

}
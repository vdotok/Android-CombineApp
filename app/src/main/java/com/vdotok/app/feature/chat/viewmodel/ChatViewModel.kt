package com.vdotok.app.feature.chat.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.dao.ChatDao
import com.vdotok.app.dao.GroupsDao
import com.vdotok.app.models.ChatModel.Companion.toChatModel
import com.vdotok.app.models.ChatModel.Companion.toMessageModel
import com.vdotok.connect.models.HeaderModel
import com.vdotok.connect.models.Message
import com.vdotok.network.models.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created By: VdoTok
 * Date & Time: On 24/11/2021 At 3:38 PM in 2021
 */

@HiltViewModel
class ChatViewModel @Inject constructor(
    var chatDao: ChatDao
) : BaseViewModel() {

    var groupTitle: ObservableField<String> = ObservableField<String>()
    var messageText: ObservableField<String> = ObservableField<String>()
    var typingText: ObservableField<String> = ObservableField<String>()
    var showTypingText: ObservableBoolean = ObservableBoolean(false)
    var disableButton: ObservableBoolean = ObservableBoolean(true)
    var name: ObservableField<String> = ObservableField<String>()

    val usersList = ArrayList<String>()


    /**
     * method to get data related to login user
     */
    fun getUserData(): LoginResponse {
        return (UserPreferences.userData as LoginResponse)
    }


    /**
     * this method is used to get the group name respective of the group created as single/multi users
     */
    fun getGroupName() {
        if (groupModel.autoCreated == 1) {
            groupModel.participants.forEach { userName ->
                if (!userName.fullname?.equals(getUserData().fullName)!!) {
                    groupTitle.set(userName.fullname)
                }
            }
        } else {
            groupTitle.set(groupModel.groupTitle)
        }
    }

    /**
     * method to know whether group is many to many or one to one
     */
    fun getGroupStatus(): Boolean {
        return groupModel.autoCreated == 1
    }

    /**
     * this method is used to get get typing status according to the users in a respective group
     */
    fun getNameOfUsers(message: Message): String {
        // users list using to get participant messages typing status  other then login user
        if (usersList.contains(message.from).not()) {
            usersList.add(message.from)
        }
        return when (usersList.size) {
            0 -> ""
            1 -> "${usersList[0]} is typing..."
            2 -> "${usersList[usersList.size - 1]} and ${usersList[usersList.size - 2]} are typing..."
            else -> {
                val size = usersList.size
                "${usersList[size - 1]},${usersList[size - 2]} and ${size.minus(2)} others are typing..."
            }
        }
    }

    /**
     * method to save chat locally
     */
    fun getSaveChat(): List<Message> {
        return appManager.mapGroupMessages[groupModel.channelName]
            ?: return ArrayList()
    }

    /**
     * method to get other username
     */
    fun getUserName(model: Message): String {
        val participants = groupModel.participants.find { it.refID == model.from }
        return participants?.fullname.toString()
    }

    /**
     * dummyHeader for file creation
     */
    fun getDummyHeader(fileType: Int): HeaderModel {
        return HeaderModel("", 0, 0, "", "", "", getUserData().refId.toString(), fileType, 0)
    }

//    DAO Methods
    fun insertChatModel(message: Message, groupId: Int, fileUri: String) {
        CoroutineScope(Dispatchers.IO).launch {
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

    fun getChatData(groupId: Int): List<Message> {
        val chatDataList = chatDao.getChatList(groupId)
        val arrayOfMessages = ArrayList<Message>()
        chatDataList.forEach { chatModel ->
            arrayOfMessages.add(chatModel.toMessageModel())
        }
        return arrayOfMessages
    }

}

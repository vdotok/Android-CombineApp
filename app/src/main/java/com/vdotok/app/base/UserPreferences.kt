package com.vdotok.app.base

import com.vdotok.app.models.CallHistoryDetails
import com.vdotok.connect.models.Connection
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.LoginResponse


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:59 AM in 2021
 */
object UserPreferences : BasePreferences() {

    var userData by objectPref(LoginResponse::class.java, defaultValue = null)
    var messagingConnection by objectPref(Connection::class.java, defaultValue = null)
    var mcToken by stringPref(defaultValue = null)
    var bytesInterval by intPref(defaultValue = 0)
    var groupList by arrayListPref(ArrayList(), defaultValue = null, arrayListDataType = GroupModel::class.java)
    var callLogList by arrayListPref(ArrayList(), defaultValue = null, arrayListDataType = CallHistoryDetails::class.java)

    fun clearUserData() {
        clearApplicationPrefs()
    }
}
package com.vdotok.app.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vdotok.app.R
import com.vdotok.app.dao.CallHistoryDao
import com.vdotok.app.dao.UserDao
import com.vdotok.app.di.ResourcesProvider
import com.vdotok.app.feature.call.CallActivity
import com.vdotok.app.manager.AppManager
import com.vdotok.app.models.CallHistoryDetails
import com.vdotok.app.models.CallNameModel
import com.vdotok.app.roomDB.AppDatabase
import com.vdotok.app.services.ProjectionService
import com.vdotok.app.utils.Utils
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.ReadReceiptModel
import com.vdotok.connect.models.ReceiptType
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.LoginResponse
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created By: VdoTok
 * Date & Time: On 11/11/2021 At 12:26 PM in 2021
 */

@HiltViewModel
open class
BaseViewModel @Inject constructor() : ViewModel() {

    lateinit var groupModel: GroupModel

    lateinit var appManager: AppManager
        @Inject set

    @Inject
    lateinit var callHistoryDao: CallHistoryDao

    @Inject
    open lateinit var userDao: UserDao

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    fun getOwnRefID(): String {
        val userData = UserPreferences.userData as LoginResponse
        return userData.refId ?: kotlin.run {
            "Not Available"
        }
    }

    fun getOwnUsername(): String {
        val userData = UserPreferences.userData as LoginResponse
        return userData.fullName ?: kotlin.run {
            "anonymous"
        }
    }


    // this fun will end the specific session e.g end screen share in broadcast
    fun endCall(sessionType: SessionType) {
        appManager.activeSession[sessionType]?.sessionUUID?.let {
            appManager.removeSession(sessionType)
            appManager.getCallClient()?.endCallSession(arrayListOf(it))
        }
        appManager.isTimerRunning.set(false)
        appManager.videoViews.clear()
    }

    // this fun will close all the active sessions e.g you hit the red button on call screen
    fun endCall() {
        val sessionIDsList = ArrayList<String>()
        for (session in appManager.activeSession.values) {
            sessionIDsList.add(session.sessionUUID)
        }
        appManager.activeSession.clear()
        appManager.getCallClient()?.endCallSession(sessionIDsList)
        appManager.isTimerRunning.set(false)
        appManager.videoViews.clear()
    }

    fun rejectCall(sessionId: String) {
        appManager.getCallClient()?.rejectIncomingCall(getOwnRefID(), sessionId)
        appManager.tempHoldCallParams = null
    }

    fun registerCallClient(reconnectStatus: Int) {
        if (appManager.getCallClient()?.isConnected() == true)
            (UserPreferences.userData as LoginResponse).authorizationToken?.let {
                appManager.getCallClient()?.register(
                    authToken = it,
                    refId = getOwnRefID(),
                    reconnectStatus = reconnectStatus
                )
            }
    }


    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    fun sendAcknowledgeMsgToGroup(myMessage: Message) {
        if (myMessage.status != ReceiptType.SEEN.value) myMessage.status =
            ReceiptType.DELIVERED.value
        if (myMessage.from != getOwnRefID()) {
            val receipt = ReadReceiptModel(
                myMessage.status,
                myMessage.key,
                System.currentTimeMillis(),
                myMessage.id,
                getOwnRefID(),
                myMessage.to
            )

            appManager.getChatClient()?.publishPacketMessage(receipt, receipt.key, receipt.to)
        }
    }


    private fun getIsBroadCastInteger(isGroupBroadcast: Boolean): Int {
        return if (isGroupBroadcast)
            0
        else
            1
    }

    private var participantsID: String? = null
    private var callParams: CallParams? = null
    fun startPublicBroadCast(
        mediaProjection: MediaProjection?,
        activity: Activity?,
        isGroupBroadcast: Boolean,
        toRefIDs: ArrayList<String>,
        callTitle: String,
        autoCreated: Int?
    ) {
        if (appManager.isCamEnableInMultiCast)
            if (appManager.isSSEnableInMultiCast) {
                appManager.getCallClient()?.startMultiSessionV2(
                    getMultiSessionParams(
                        SessionType.CALL,
                        true,
                        getIsBroadCastInteger(isGroupBroadcast),
                        toRefIDs, callTitle, autoCreated.toString()
                    ),
                    mediaProjection,
                    isGroupBroadcast
                )
            } else {
                callParams = getSingleSessionParams(
                    SessionType.CALL,
                    false,
                    getIsBroadCastInteger(isGroupBroadcast),
                    toRefIDs,
                    callTitle, autoCreated.toString()
                )
                val session = appManager.getCallClient()?.dialOne2ManyCall(callParams!!)
                session?.let { it1 ->
                    callParams?.sessionUUID = it1
                    appManager.setSession(SessionType.CALL, callParams!!)
                }
                insertCallHistory(
                    callParams,
                    participantsID,
                    resourcesProvider.getString(R.string.status_outgoing_call),
                    true,
                    callTitle
                )
            }
        else {
            callParams = getSingleSessionParams(
                SessionType.SCREEN,
                appManager.isAppAudioEnableInMultiCast,
                getIsBroadCastInteger(isGroupBroadcast),
                toRefIDs, callTitle, autoCreated.toString()
            )
            val session = appManager.getCallClient()?.startSession(callParams!!, mediaProjection)
            session?.let { it1 ->
                callParams?.sessionUUID = it1
                appManager.setSession(SessionType.SCREEN, callParams!!)
            }
            insertCallHistory(
                callParams,
                participantsID,
                resourcesProvider.getString(R.string.status_outgoing_call),
                true,
                callTitle
            )
        }
        if (toRefIDs.size > 1 || callParams?.isBroadcast == 1) {
            participantsID = null
        } else {
            toRefIDs.forEach {
                participantsID = it
            }
        }
        if (appManager.isCamEnableInMultiCast && appManager.isSSEnableInMultiCast) {
            return
        }else{
            if (this::groupModel.isInitialized) {
                activity?.startActivity(CallActivity.createCallActivityV2(activity, groupModel))
            } else {
                activity?.startActivity(CallActivity.createCallActivity(activity))
            }

        }
    }

    fun setupMultiSessionData(
        sessionIds: Pair<String, String>,
        isGroupBroadcast: Boolean,
        toRefIDs: ArrayList<String>,
        callTitle: String,
        autoCreated: Int?,
        activity: Activity?
    ) {
        callParams = getMultiSessionParams(
            SessionType.CALL,
            false,
            getIsBroadCastInteger(isGroupBroadcast),
            toRefIDs, callTitle, autoCreated.toString()
        )
        callParams?.sessionUUID = sessionIds.first
        callParams?.associatedSessionUUID = sessionIds.second
        appManager.setSession(SessionType.CALL, callParams!!)
        insertCallHistory(
            callParams,
            participantsID,
            resourcesProvider.getString(R.string.status_outgoing_call),
            true,
            callTitle
        )
        val screenParams =
            getMultiSessionParams(
                SessionType.SCREEN,
                appManager.isAppAudioEnableInMultiCast,
                getIsBroadCastInteger(isGroupBroadcast),
                toRefIDs,
                callTitle,
                autoCreated.toString()
            )
        screenParams.sessionUUID = sessionIds.second
        screenParams.associatedSessionUUID = sessionIds.first
        appManager.setSession(SessionType.SCREEN, screenParams)
        insertCallHistory(
            screenParams,
            participantsID,
            resourcesProvider.getString(R.string.status_outgoing_call),
            true,
            callTitle
        )
        if (this::groupModel.isInitialized) {
            activity?.startActivity(CallActivity.createCallActivityV2(activity, groupModel))
        } else {
            activity?.startActivity(CallActivity.createCallActivity(activity))
        }

    }

    fun insertCallHistory(
        callParams: CallParams?,
        participantsID: String?,
        callStatus: String,
        isInitiator: Boolean,
        callTitle: String
    ) {

        val data: CallNameModel? = Utils.getCallTitle(callParams?.customDataPacket.toString())
        val title = if (isInitiator) {
            callTitle
        } else {
            if (callParams?.callType == CallType.ONE_TO_ONE) {
                data?.calleName
            } else {
                data?.groupName
            }
        }

        viewModelScope.launch {
            callHistoryDao.insertCallHistory(
                CallHistoryDetails(
                    callParams?.sessionUUID,
                    title,
                    participantsID,
                    callStatus,
                    System.currentTimeMillis(),
                    callParams?.associatedSessionUUID,
                    callParams?.mediaType!!,
                    callParams.sessionType,
                    data?.groupAutoCreatedValue
                )
            )
        }
    }

    fun updateCallHistory(sessionId: String, callStatus: String) {
        viewModelScope.launch {
            callHistoryDao.updateCallStatus(callStatus, sessionId)
        }
    }

    private fun getMultiSessionParams(
        sessionType: SessionType,
        isAppAudioIncluded: Boolean,
        isGroupBroadcast: Int,
        toRefIDs: ArrayList<String>,
        callTitle: String,
        autoCreated: String
    ): CallParams {
        return CallParams(
            refId = getOwnRefID(),
            toRefIds = toRefIDs,
            callType = CallType.ONE_TO_MANY,
            sessionType = sessionType,
            mediaType = MediaType.VIDEO,
            isInitiator = true,
            isAppAudio = isAppAudioIncluded,
            isBroadcast = isGroupBroadcast,
            customDataPacket = Utils.setCallTitleCustomObject(null, callTitle, autoCreated)
        )
    }

    private fun getSingleSessionParams(
        sessionType: SessionType,
        isAppAudioIncluded: Boolean,
        isGroupBroadcast: Int,
        toRefIDs: ArrayList<String>,
        callTitle: String,
        autoCreated: String
    ): CallParams {
        return CallParams(
            refId = getOwnRefID(),
            toRefIds = toRefIDs,
            mediaType = MediaType.VIDEO,
            callType = CallType.ONE_TO_MANY,
            sessionType = sessionType,
            isAppAudio = isAppAudioIncluded,
            isBroadcast = isGroupBroadcast,
            isInitiator = true,
            customDataPacket = Utils.setCallTitleCustomObject(null, callTitle, autoCreated)
        )
    }

    @TargetApi(21)
    fun startScreenCapture(activity: Activity): Intent? {
        appManager.serviceIntent = Intent(activity, ProjectionService::class.java)
        activity.bindService(
            appManager.serviceIntent,
            appManager.mConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
        val mediaProjectionManager =
            activity.application?.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    fun logout() {
        clearDBTables()
        (UserPreferences.userData as LoginResponse).refId?.let {
            appManager.getCallClient()?.unRegister(it)
            appManager.getChatClient()?.disconnect()
            appManager.userPresenceList = ArrayList()
        }
    }

    private fun clearDBTables() {
        CoroutineScope(Dispatchers.IO).launch {
            appDatabase.clearAllTables()
        }
    }

    fun getRefIDs(): java.util.ArrayList<String> {
        val refIdList = java.util.ArrayList<String>()
        groupModel.participants?.forEach { participant ->
            if (participant.refID != getOwnRefID())
                participant.refID?.let { refIdList.add(it) }
        }
        return refIdList
    }


}



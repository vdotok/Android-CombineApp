package com.vdotok.app.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.utils.SDK_PROJECT_ID
import com.vdotok.app.interfaces.CallBackManager
import com.vdotok.app.models.ActiveSession
import com.vdotok.app.models.CallHistoryDetails
import com.vdotok.app.services.ProjectionService
import com.vdotok.connect.manager.ChatManager
import com.vdotok.connect.manager.ChatManagerCallback
import com.vdotok.connect.models.*
import com.vdotok.network.models.LoginResponse
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.commands.RegisterResponse
import com.vdotok.streaming.enums.CallStatus
import com.vdotok.streaming.enums.EnumConnectionStatus
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.interfaces.CallSDKListener
import com.vdotok.streaming.models.CallParams
import com.vdotok.streaming.models.SessionDataModel
import com.vdotok.streaming.models.SessionStateInfo
import com.vdotok.streaming.models.Usage
import org.webrtc.VideoTrack
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:02 PM in 2021
 */
class AppManager(val context: Context) {

    private var callClient: CallClient? = null
    private var chatClient: ChatManager? = null

    var isSSEnableInMultiCast = false
    var isCamEnableInMultiCast = false
    var isAppAudioEnableInMultiCast = false

    val callSDKStatus: ObservableBoolean = ObservableBoolean(false)
    val callSDKRegistrationStatus: ObservableBoolean = ObservableBoolean(false)
    val chatSDKStatus: ObservableBoolean = ObservableBoolean(false)
    val isTimerRunning: ObservableBoolean = ObservableBoolean(false)
    var countParticipant : ObservableInt = ObservableInt(0)

    val listeners: ArrayList<CallBackManager> = ArrayList()

    val activeSession: HashMap<SessionType, CallParams> = HashMap()
    var videoViews = ArrayList<ActiveSession>()

    val handler = Handler(Looper.getMainLooper())
    lateinit var runnable: Runnable
    var isCallActivityOpened = MutableLiveData<Boolean>(false)
    var callList: ArrayList<CallHistoryDetails> = ArrayList()
    var prefList : ArrayList<CallHistoryDetails> = ArrayList()

    var mapGroupMessages: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var mapLastMessage: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var mapUnreadCount: MutableMap<String, Int> = mutableMapOf()
    var messageUpdateLiveData = MutableLiveData<Message>()
    var lastMessageGroupKey = ""
    var serviceIntent: Intent? = null

    var mService: ProjectionService? = null
    var mBound = false

    var broadCastURL: String? = null

    var tempHoldCallParams: CallParams? = null
    var speakerState = false

    //    this is made to keep track of group presence as doSubscribe is not called until new group is formed
//    or existing are not subscribed so due to viewModel lifecycle we need additional storing of data
    var userPresenceList: ArrayList<Presence> = ArrayList()

    var isCallSDKsReconnect = false

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: ProjectionService.LocalBinder = service as ProjectionService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    init {
        CallClient.getInstance(context)?.let {
            it.setConstants(SDK_PROJECT_ID)
            callClient = it
        }
        ChatManager.getInstance(context).let {
            it.setConstants(SDK_PROJECT_ID)
            it.setIsSenderReceiveFilePackets(true)
            chatClient = it
        }
        setSDKsListener()
    }

    fun acceptSecondCallIfAny() {
        tempHoldCallParams?.let {
            Handler(Looper.getMainLooper()).postDelayed({
                val userData = UserPreferences.userData as LoginResponse
                userData.refId?.let { it1 ->
                    getCallClient()
                        ?.acceptIncomingCall(it1, it)
                }
                setSession(it.sessionType, it)
                tempHoldCallParams = null
            }, 5000)
        }
    }

    fun notifyCallConnectionStatus(enumConnectionStatus: EnumConnectionStatus) {
        for (listener in listeners) {
            listener.callConnectionStatus(enumConnectionStatus)
        }
    }

    fun notifyChatConnectionStatus(message: String) {
        for (listener in listeners) {
            listener.chatConnectionStatus(message)
        }
    }

    fun notifyCallStatus(callInfoResponse: CallInfoResponse) {
        for (listener in listeners) {
            listener.callStatus(callInfoResponse)
        }
    }

    fun notifyNewMessage(message: Message) {
        for (listener in listeners) {
            listener.onMessageArrive(message)
        }
    }

    fun notifyAttachmentProgress(fileHeaderId: String, progress: Int) {
        for (listener in listeners) {
            listener.onAttachmentProgressSend(fileHeaderId, progress)
        }
    }

    fun notifyFileReceivingFailed() {
        for (listener in listeners) {
            listener.onFileReceiveFailed()
        }
    }

    fun notifyFileSendStart(fileHeaderId: String, fileType: Int) {
        for (listener in listeners) {
            listener.onFileSend(fileHeaderId, fileType)
        }
    }

    fun notifyTypingMessage(message: Message) {
        for (listener in listeners) {
            listener.onTypingMessage(message)
        }
    }

    fun notifyCameraStream(stream: VideoTrack) {
        for (listener in listeners) {
            listener.onLocalCamera(stream)
        }
    }

    fun notifyAudioStream(refId: String, sessionID: String) {
        for (listener in listeners) {
            listener.onAudioTrack(refId, sessionID)
        }
    }

    fun notifyRemoteViewStream(stream: VideoTrack, refId: String, sessionID: String) {
        for (listener in listeners) {
            listener.onVideoTrack(stream, refId, sessionID)
        }
    }

    fun notifyIncomingCall(callParams: CallParams) {
        for (listener in listeners) {
            listener.incomingCall(callParams)
        }
    }

    private fun removeVideoViews(refID: String, sessionID: String) {
        videoViews.forEach {
            it.viewRenderer?.apply {
                it.videoTrack.removeSink(preview)
                release()
                it.viewRenderer = null
            }

        }
        videoViews.clear()
    }

    private fun setSDKsListener() {
        callClient?.setListener(object : CallSDKListener {
            override fun audioVideoState(sessionStateInfo: SessionStateInfo) {
                for (listener in listeners) {
                    listener.audioVideoState(sessionStateInfo)
                }
            }

            override fun callStatus(callInfoResponse: CallInfoResponse) {
                Log.e("CallStatus", "appManager" + callInfoResponse.callStatus.value)
                notifyCallStatus(callInfoResponse)
                when (callInfoResponse.callStatus) {
                    CallStatus.OUTGOING_CALL_ENDED -> {
                        callInfoResponse.callParams?.apply {
                            for (value in activeSession.values) {
                                if (sessionUUID == value.sessionUUID) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        removeSession(sessionType)
                                        removeVideoViews(refId, sessionUUID)
                                    }, 500)
                                    isTimerRunning.set(false)
                                    speakerState = false
                                    countParticipant.set(0)
                                }
                            }
                        }
                    }
                    CallStatus.CALL_REJECTED -> {
                        callInfoResponse.callParams?.let {
                            removeSession(it.sessionType)
                            removeVideoViews(it.refId, it.sessionUUID)
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun connectionStatus(enumConnectionStatus: EnumConnectionStatus) {
                notifyCallConnectionStatus(enumConnectionStatus)
            }

            override fun incomingCall(callParams: CallParams) {
                var isMultiSessionIncomingCall = false
                tempHoldCallParams = callParams
                //Means already a session is active
                if (activeSession.size != 0) {
                    for (entry in activeSession.values) {
                        //last active session is part of multiSession so simply accept the call
                        if (entry.sessionUUID == callParams.associatedSessionUUID) {
                            isMultiSessionIncomingCall = true
                            acceptSecondCallIfAny()
                            tempHoldCallParams = null
                        }
                    }
                    if (!isMultiSessionIncomingCall) {
                        val userData = UserPreferences.userData as LoginResponse
                        userData.refId?.let {
                            getCallClient()?.sessionBusy(
                                refId = it,
                                sessionUUID = callParams.sessionUUID
                            )
                        }
                    }
                } else {
                    if (isCallActivityOpened.value == false) {
                        tempHoldCallParams = null
                        notifyIncomingCall(callParams)
                    }
                }
            }

            override fun onCameraStream(stream: VideoTrack) {
                Handler(Looper.getMainLooper()).postDelayed({
                    notifyCameraStream(stream)
                }, 500)
            }

            override fun onClose(reason: String) {
                notifyCallConnectionStatus(EnumConnectionStatus.CLOSED)
            }

            override fun onError(cause: String) {
                notifyCallConnectionStatus(EnumConnectionStatus.ERROR)
            }

            override fun onPublicURL(publicURL: String) {
                broadCastURL = publicURL
//                for (listener in listeners) {
//                    listener.onPublicURL(url = publicURL)
//                }
            }

            override fun onRemoteStream(refId: String, sessionID: String) {
                notifyAudioStream(refId, sessionID)
            }

            override fun onRemoteStream(stream: VideoTrack, refId: String, sessionID: String) {
                var isNewView = true
                videoViews.forEach { action ->
                    if (action.sessionID == sessionID && action.refID == refId) {
                        isNewView = false
                        action.videoTrack.removeSink(action.viewRenderer?.preview)
                        action.videoTrack.dispose()
                        stream.addSink(action.viewRenderer?.preview)
                    }
                }
                if (isNewView)
                    Handler(Looper.getMainLooper()).postDelayed({
                        notifyRemoteViewStream(stream, refId, sessionID)
                    }, 500)
            }

            override fun onSessionReady(
                mediaProjection: MediaProjection?
            ) {
                Handler(Looper.getMainLooper()).postDelayed({
                    for (listener in listeners) {
                        listener.onSSSessionReady(mediaProjection)
                    }
                }, 1000)
            }

            override fun participantCount(participantCount: Int, participantRefIdList: ArrayList<String>) {
                Handler(Looper.getMainLooper()).postDelayed({
                    for (listener in listeners) {
                        listener.countParticipant(participantCount, participantRefIdList)
                    }
                }, 1000)
            }

            override fun registrationStatus(registerResponse: RegisterResponse) {
                for (listener in listeners) {
                    listener.callRegistrationStatus(registerResponse)
                }
            }

            override fun sendCurrentDataUsage(sessionKey: String, usage: Usage) {

            }

            override fun sendEndDataUsage(sessionKey: String, sessionDataModel: SessionDataModel) {

            }

            override fun sessionHold(sessionUUID: String) {
            }

        })
        chatClient?.listener = object : ChatManagerCallback {
            override fun connectionError() {
                notifyChatConnectionStatus("Error")
            }

            override fun reconnectAction(connectionState: Boolean) {
                if (connectionState || chatClient?.isConnected() == true) {
                    notifyChatConnectionStatus("Connected")
                }
            }

            override fun onConnect() {
                notifyChatConnectionStatus("Connected")
            }

            override fun onConnectionFailed(cause: Throwable) {
                notifyChatConnectionStatus(cause.toString())
            }

            override fun onConnectionLost(cause: Throwable) {
                notifyChatConnectionStatus(cause.toString())
            }

            override fun onBytesReceived(payload: ByteArray) {
                for (listener in listeners) {
                    listener.onByteReceived(payload)
                }
            }

            override fun onFileReceivingCompleted(
                headerModel: HeaderModel,
                byteArray: ByteArray,
                msgId: String
            ) {
                for (listener in listeners) {
                    listener.onFileReceivedCompleted(headerModel, byteArray, msgId)
                }
            }

            override fun onFileReceivingFailed() {
                notifyFileReceivingFailed()
            }

            override fun onFileReceivingProgressChanged(fileHeaderId: String, progress: Int) {

            }

            override fun onFileReceivingStarted(fileHeaderId: String) {

            }

            override fun onFileSendingComplete(fileHeaderId: String, fileType: Int) {

            }

            override fun onFileSendingFailed(headerId: String) {

            }

            override fun onFileSendingProgressChanged(fileHeaderId: String, progress: Int) {
                notifyAttachmentProgress(fileHeaderId, progress)
            }

            override fun onFileSendingStarted(fileHeaderId: String, fileType: Int) {
                notifyFileSendStart(fileHeaderId, fileType)


            }

            override fun onMessageArrived(myMessage: Message) {
                notifyNewMessage(myMessage)
            }

            override fun onPresenceReceived(who: ArrayList<Presence>) {
                for (listener in listeners) {
                    listener.onPresenceReceived(who)
                }

            }

            override fun onReceiptReceived(model: ReadReceiptModel) {
                for (listener in listeners) {
                    listener.onReceiptReceive(model)
                }
            }

            override fun onSubscribe(topic: String) {

            }

            override fun onSubscribeFailed(topic: String, cause: Throwable?) {

            }

            override fun onTypingMessage(myMessage: Message) {
                notifyTypingMessage(myMessage)

            }


        }
    }

    fun addListener(callManagerListener: CallBackManager) {
        if (!listeners.contains(callManagerListener)) listeners.add(callManagerListener)
    }

    fun removeListener(callManagerListener: CallBackManager) {
        listeners.remove(callManagerListener)
    }

    fun getCallClient() = callClient
    fun getChatClient() = chatClient


    private fun timerClick(time: String) {
        for (listener in listeners) {
            listener.onTimeTicks(time)
        }
    }

    fun startTimer() {

        var seconds = 0
        runnable = object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                val time = when {
                    minutes <= 10 -> {
                        String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
                    }
                    hours == 0 -> {
                        String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
                    }
                    else -> {
                        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                    }
                }
                seconds++
                timerClick(time)
                if (isTimerRunning.get())
                    handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    fun connect() {
        val loginData = UserPreferences.userData as LoginResponse
        loginData.mediaServer?.let { mediaServerMap ->
            callClient?.connect(getMediaServerAddress(mediaServerMap), mediaServerMap.endPoint)
        }
        chatClient?.setIsSenderReceiveFilePackets(true)
        chatClient?.connect(UserPreferences.messagingConnection as Connection)

    }

    fun reconnectCallSDKs() {
        val loginData = UserPreferences.userData as LoginResponse
        loginData.mediaServer?.let { mediaServerMap ->
            callClient?.connect(getMediaServerAddress(mediaServerMap), mediaServerMap.endPoint)
            isCallSDKsReconnect = true
        }
    }

    private fun getMediaServerAddress(mediaServer: LoginResponse.MediaServerMap): String {
        return "https://${mediaServer.host}:${mediaServer.port}"
    }

    fun setSession(session: SessionType, callParams: CallParams) {
        activeSession[session] = callParams
    }

    fun removeSession(session: SessionType) {
        activeSession.remove(session)
    }


    /**
     * Function to help in persisting local chat by updating local data till the user is connected to the socket
     * @param message message object we will be sending to the server
     * */
    fun updateMessageMapData(message: Message) {
        if (mapGroupMessages.containsKey(message.to)) {
            val messageValue: ArrayList<Message> =
                mapGroupMessages[message.to] as ArrayList<Message>
            val check = messageValue.any { it.id == message.id }
            if (!check) {
                messageValue.add(message)
                mapGroupMessages[message.to] = messageValue
                mapLastMessage[message.to] = messageValue
            } else {
                val list = mapGroupMessages[message.to] as ArrayList<Message>
                val oldValue = list.first { it.id == message.id }
                list[list.indexOf(oldValue)] = message
                mapGroupMessages[message.to] = list
            }

        } else {
            val messageValue: ArrayList<Message> = ArrayList()
            messageValue.add(message)
            mapGroupMessages[message.to] = messageValue
            mapLastMessage[message.to] = messageValue
        }
    }

}
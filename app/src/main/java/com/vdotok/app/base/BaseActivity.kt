package com.vdotok.app.base

import android.app.AlertDialog
import android.content.Context
import android.media.projection.MediaProjection
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.norgic.spotsdialog.SpotsDialog
import com.vdotok.app.R
import com.vdotok.app.databinding.ActivityBaseBinding
import com.vdotok.app.feature.call.CallActivity
import com.vdotok.app.interfaces.CallBackManager
import com.vdotok.app.models.ActiveSession
import com.vdotok.app.uielements.CustomCallView
import com.vdotok.app.utils.Utils
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.connect.models.Presence
import com.vdotok.network.network.NetworkStatusLiveData
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.commands.RegisterResponse
import com.vdotok.streaming.enums.*
import com.vdotok.streaming.models.CallParams
import com.vdotok.streaming.models.SessionStateInfo
import org.webrtc.VideoTrack
import java.util.*


abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity(),
    CallBackManager {


    @get:LayoutRes
    protected abstract val getLayoutRes: Int
    protected abstract val getViewModel: Class<VM>
    lateinit var binding: DB
    lateinit var viewModel: VM
    private lateinit var spotsDialog: AlertDialog

    private val TAG: String = "BaseActivity"
    private lateinit var rootBinding: ActivityBaseBinding

    private lateinit var proximityWakeLock: PowerManager.WakeLock
    private var proximitySensorEnabled = false
    private lateinit var mLiveDataNetwork: NetworkStatusLiveData
    private var isInternetConnectionRestored = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[getViewModel]

        rootBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_base, null, false)
        binding =
            DataBindingUtil.inflate(layoutInflater, getLayoutRes, rootBinding.layoutContainer, true)
        super.setContentView(rootBinding.root)
        rootBinding.isActiveSession = viewModel.appManager.isTimerRunning
        rootBinding.notificationLayout.timerLayout.performSingleClick {
            startActivity(CallActivity.createCallActivityForCallConnectedFragment(this))
        }
        viewModel.appManager.isCallActivityOpened.observeForever {
            rootBinding.isCallActivityOpen = ObservableBoolean(it)
        }


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            Log.w(TAG, "PROXIMITY_SCREEN_OFF_WAKE_LOCK isn't supported on this device!")
        }

        proximityWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName;proximity_sensor"
        )

        addInternetConnectionObserver()
    }

    private fun addInternetConnectionObserver() {
        mLiveDataNetwork = NetworkStatusLiveData(this)
        mLiveDataNetwork.observe(this) { isInternetConnected ->
            when {
                isInternetConnected == true && isInternetConnectionRestored -> {
                    Log.i("Internet", "internet connection restored!")
                    viewModel.appManager.getCallClient()?.apply {
                        if (!isConnected()) {
                            viewModel.appManager.reconnectCallSDKs()
                        }
                    }
                }
                isInternetConnected == false -> {
                    isInternetConnectionRestored = true
                    Log.i("Internet", "internet connection lost!")
                }
                else -> {
                }
            }
        }
    }

    fun showProgress(message: String) {
        spotsDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage(message)
            .setCancelable(false)
            .setTheme(R.style.LoadingStyleTransparent)
            .build()
        spotsDialog.show()
    }

    fun hideProgress() {
        spotsDialog.dismiss()
    }

    override fun callConnectionStatus(enumConnectionStatus: EnumConnectionStatus) {
        when (enumConnectionStatus) {
            EnumConnectionStatus.CONNECTED -> {
                viewModel.appManager.callSDKStatus.set(true)
                runOnUiThread {
                    viewModel.registerCallClient(if (viewModel.appManager.isCallSDKsReconnect) 1 else 0)
                }
            }
            EnumConnectionStatus.CLOSED ->
                viewModel.appManager.callSDKStatus.set(false)
            EnumConnectionStatus.ERROR ->
                viewModel.appManager.callSDKStatus.set(false)
            EnumConnectionStatus.OPEN ->
                viewModel.appManager.callSDKStatus.set(true)
            else -> {
                Log.i(TAG, "Call Connection Status")
            }
        }
        if (!viewModel.appManager.callSDKStatus.get())
            viewModel.appManager.callSDKRegistrationStatus.set(false)
    }

    override fun chatConnectionStatus(message: String) {
        if (message == "Connected") {
            viewModel.appManager.chatSDKStatus.set(true)
        } else
            viewModel.appManager.chatSDKStatus.set(false)
    }


    override fun onPresenceReceived(who: ArrayList<Presence>) {
    }


    override fun callRegistrationStatus(registerResponse: RegisterResponse) {
        when (registerResponse.registrationStatus) {
            RegistrationStatus.REGISTER_SUCCESS -> {
                viewModel.appManager.callSDKRegistrationStatus.set(true)
                if (registerResponse.reConnectStatus == 1) {
                    viewModel.appManager.getCallClient()?.initiateReInviteProcess()
                }
            }
            RegistrationStatus.UN_REGISTER,
            RegistrationStatus.REGISTER_FAILURE,
            RegistrationStatus.INVALID_REGISTRATION -> {
                viewModel.appManager.callSDKRegistrationStatus.set(false)
            }
        }
    }

    override fun callStatus(callInfoResponse: CallInfoResponse) {
        Log.e("CallStatus", "BaseActivity" + callInfoResponse.callStatus.value)
        when (callInfoResponse.callStatus) {
            CallStatus.PARTICIPANT_LEFT_CALL -> {
                callInfoResponse.callParams?.let {
                    removeView(it.sessionUUID, it.toRefIds)
                }
            }
        }
    }

    private fun removeView(sessionID: String, toRefIds: ArrayList<String>) {
        runOnUiThread {
            viewModel.appManager.videoViews.forEach {
                toRefIds.forEach { id ->
                    if (it.sessionID == sessionID && it.refID == id) {
                        it.videoTrack.removeSink(it.viewRenderer?.proxyVideoSink)
                        it.videoTrack.dispose()
                        it.viewRenderer?.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun incomingCall(callParams: CallParams) {
            viewModel.insertCallHistory(callParams,
              callParams.refId, viewModel.resourcesProvider.getString(R.string.status_incoming_call), false, "",)
        startActivity(CallActivity.createCallActivity(this, callParams))
    }

    override fun onLocalCamera(stream: VideoTrack) {
        viewModel.appManager.videoViews.add(
            ActiveSession(
                refID = viewModel.getOwnRefID(),
                sessionID = "ownRefID",
                videoTrack = stream,
                null
            )
        )
    }

    override fun onAudioTrack(refId: String, sessionID: String) {

    }

    override fun onVideoTrack(stream: VideoTrack, refId: String, sessionID: String) {
//        var matchedIndex = -1
//        viewModel.appManager.videoViews.forEachIndexed { index, action ->
//            if (action.sessionID == sessionID && action.refID == refId) {
//                matchedIndex = index
//                action.videoTrack.removeSink(action.viewRenderer?.preview)
//                action.videoTrack.dispose()
//                stream.addSink(action.viewRenderer?.preview)
//            }
//        }
//        if (matchedIndex == -1) {
            viewModel.appManager.videoViews.add(
                ActiveSession(
                    refID = refId,
                    sessionID = sessionID,
                    videoTrack = stream,
                    null
                )
            )
            if (viewModel.appManager.isCallActivityOpened.value == false)
                addCallViews()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.appManager.removeListener(this)
        enableProximitySensor(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.appManager.addListener(this)
        viewModel.appManager.activeSession[SessionType.CALL]?.apply {
            if (mediaType == MediaType.AUDIO)
                enableProximitySensor(true)
        }
        if (viewModel.appManager.isCallActivityOpened.value == false && viewModel.appManager.isTimerRunning.get())
            if (viewModel.appManager.activeSession.size != 0 && viewModel.appManager.videoViews.size != 0) {
                addCallViews()
            }
    }

    private fun addCallViews() {
        viewModel.appManager.videoViews.forEach {
            addView(it)
        }
    }

    private fun addView(view: ActiveSession) {
        runOnUiThread {
            if (view.viewRenderer == null) {
//            val mainHandler = Handler(this.mainLooper)
//            val myRunnable = Runnable {
                val rowView = this.layoutInflater.inflate(R.layout.peer_video, null)
                val lp = LinearLayout.LayoutParams(
                    230,
                    290
                )
                lp.setMargins(5, 0, 5, 20)
                rowView.layoutParams = lp
                val rowId = View.generateViewId()
                rowView.id = rowId

                rootBinding.notificationLayout.notificationCallView.addView(rowView)

                view.viewRenderer = rowView as CustomCallView?
                view.viewRenderer?.refID = view.refID
                view.viewRenderer?.sessionID = view.sessionID
                view.videoTrack.addSink(view.viewRenderer?.setView())
                view.viewRenderer?.showHideMuteIcon(view.isMuted)
                view.viewRenderer?.showHideAvatar(view.isCamPaused)
//            }
//            mainHandler.post(myRunnable)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        enableProximitySensor(false)
        viewModel.appManager.removeListener(this)
        viewModel.appManager.videoViews.forEach {
            it.videoTrack.removeSink(it.viewRenderer?.proxyVideoSink)
            rootBinding.notificationLayout.notificationCallView.removeView(it.viewRenderer)
            it.viewRenderer = null
        }
    }


    override fun audioVideoState(sessionStateInfo: SessionStateInfo) {
        viewModel.appManager.videoViews.forEach {
            if (it.refID == sessionStateInfo.refID && it.sessionID == sessionStateInfo.sessionKey) {
                runOnUiThread {
                    it.isMuted = sessionStateInfo.audioState == 0
                    it.isCamPaused = sessionStateInfo.videoState == 0
                    it.viewRenderer?.showHideAvatar(it.isCamPaused)
                    it.viewRenderer?.showHideMuteIcon(it.isMuted)
                }
            }
        }
    }

    override fun sendCurrentDataUsage(sessionKey: String, usage: Usage) {
        viewModel.getOwnRefID().let { refId ->
            Log.e(
                "StatsLogger",
                "currentSentUsage: ${usage.currentSentBytes}, currentReceivedUsage: ${usage.currentReceivedBytes}"
            )
            viewModel.appManager.getCallClient()?.sendEndCallLogs(
                refId = refId,
                sessionKey = sessionKey,
                stats = PartialCallLogs(
                    upload_bytes = usage.currentSentBytes.toString(),
                    download_bytes = usage.currentReceivedBytes.toString()
                )
            )
        }
    }

    override fun sendEndDataUsage(sessionKey: String, sessionDataModel: SessionDataModel) {
        viewModel.getOwnRefID().let { refId ->
            Log.e("StatsLogger", "sessionData: $sessionDataModel")
            viewModel.appManager.getCallClient()?.sendEndCallLogs(
                refId = refId,
                sessionKey = sessionKey,
                stats = sessionDataModel
            )
        }
    }

    fun enableProximitySensor(enable: Boolean) {
        if (enable) {
            if (!proximitySensorEnabled) {
                Log.i(TAG, "Enabling proximity sensor turning off screen")
                if (!proximityWakeLock.isHeld) {
                    Log.i(TAG, "Acquiring PROXIMITY_SCREEN_OFF_WAKE_LOCK")
                    proximityWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
                }
                proximitySensorEnabled = true
            }
        } else {
            if (proximitySensorEnabled) {
                Log.i(TAG, "Disabling proximity sensor turning off screen")
                if (proximityWakeLock.isHeld) {
                    Log.i(TAG, "Releasing PROXIMITY_SCREEN_OFF_WAKE_LOCK")
                    proximityWakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                }
                proximitySensorEnabled = false
            }
        }
    }


    override fun onTimeTicks(timer: String) {
        rootBinding.notificationLayout.callTime.text = timer
    }

    override fun onPublicURL(url: String) {

    }

    override fun onSSSessionReady(
        mediaProjection: MediaProjection?
    ) {
    }
}

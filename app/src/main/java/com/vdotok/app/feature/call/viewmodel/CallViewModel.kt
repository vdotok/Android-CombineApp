package com.vdotok.app.feature.call.viewmodel

import androidx.databinding.ObservableBoolean
import com.vdotok.app.base.BaseViewModel
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:25 PM in 2021
 */
@HiltViewModel
class CallViewModel @Inject constructor() : BaseViewModel() {

    val isCamEnable = ObservableBoolean(true)
    val isMicEnable = ObservableBoolean(true)
    val isAppAudioEnable = ObservableBoolean(true)
    val isScreenEnable = ObservableBoolean(true)
    var isSpeakerEnable = ObservableBoolean(false)


    fun switchCam() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.switchCamera(this)
        }
    }

    fun pauseCam() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.pauseVideo(getOwnRefID(), this)
            isCamEnable.set(false)
            updateLocalViewOnVideoPauseResume(true)
        }
    }

    fun resumeCam() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.resumeVideo(getOwnRefID(), this)
            isCamEnable.set(true)
            updateLocalViewOnVideoPauseResume(false)
        }
    }

    fun pauseScreen() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.pauseVideo(getOwnRefID(), this)
            isScreenEnable.set(false)
        }
    }

    fun resumeScreen() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.resumeVideo(getOwnRefID(), this)
            isScreenEnable.set(true)
        }
    }

    private fun updateLocalViewOnVideoPauseResume(isShown: Boolean) {
        appManager.videoViews.forEach {
            if (it.refID == getOwnRefID()) {
                it.viewRenderer?.showHideAvatar(isShown)
                it.isCamPaused = isShown
            }
        }
    }

    private fun updateLocalViewOnAudioMuteAndUnmute(isMute: Boolean) {
        appManager.videoViews.forEach {
            if (it.refID == getOwnRefID()) {
                it.viewRenderer?.showHideMuteIcon(isMute)
                it.isMuted = isMute
            }
        }
    }

    fun muteMic() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.muteUnMuteMic(getOwnRefID(), this)
            appManager.getCallClient()?.isAudioEnabled(this)?.let {
                isMicEnable.set(it)
                updateLocalViewOnAudioMuteAndUnmute(!it)
            }
        }
    }

    fun muteScreenAppAudio() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.muteUnMuteMic(getOwnRefID(), this)
            appManager.getCallClient()?.isAudioEnabled(this)?.let { isAppAudioEnable.set(it) }
        }
    }

    fun muteScreenMicAudio() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.muteUnMuteMic(getOwnRefID(), this)
            appManager.getCallClient()?.isAudioEnabled(this)?.let { isMicEnable.set(it) }
        }
    }

    fun speakerToggle() {
        if (appManager.getCallClient()?.isSpeakerEnabled() == true) {
            isSpeakerEnable.set(false)
            appManager.getCallClient()?.setSpeakerEnable(false)
        } else {
            isSpeakerEnable.set(true)
            appManager.getCallClient()?.setSpeakerEnable(true)
        }
    }

    fun speakerDefaultState() {
        if (appManager.activeSession[SessionType.CALL]?.mediaType == MediaType.AUDIO) {
            appManager.getCallClient()?.setSpeakerEnable(false)
            isSpeakerEnable.set(false)
        } else {
            isSpeakerEnable.set(true)
            appManager.getCallClient()?.setSpeakerEnable(true)
        }
    }

    fun micEnabled() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.isAudioEnabled(this)?.let { isMicEnable.set(it) }
        }
    }

    fun internalAudioEnabled() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.isAudioEnabled(this)?.let { isAppAudioEnable.set(it) }
        }
    }

    fun camViewEnabled() {
        appManager.activeSession[SessionType.CALL]?.sessionUUID?.apply {
            appManager.getCallClient()?.isVideoEnabled(this)?.let { isCamEnable.set(it) }
        }
    }

    fun screenViewEnabled() {
        appManager.activeSession[SessionType.SCREEN]?.sessionUUID?.apply {
            appManager.getCallClient()?.isVideoEnabled(this)?.let { isScreenEnable.set(it) }
        }
    }

    fun speakerEnabledState() {
        if (appManager.getCallClient()?.isSpeakerEnabled() == true) {
            isSpeakerEnable.set(true)
        } else {
            isSpeakerEnable.set(false)
        }
    }

    fun acceptIncomingCall(callParams: CallParams) {
        val session = appManager.getCallClient()?.acceptIncomingCall(getOwnRefID(), callParams)
        session?.let { it1 ->
            callParams.sessionUUID = it1
            appManager.setSession(callParams.sessionType, callParams)
        }

    }

    fun startTimer() {
        if (!appManager.isTimerRunning.get()) {
            appManager.isTimerRunning.set(true)
            appManager.startTimer()
        }
    }

}
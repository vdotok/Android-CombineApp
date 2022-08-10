package com.vdotok.app.feature.call.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.databinding.FragmentCallConnectedBinding
import com.vdotok.app.feature.call.dialog.CopyURLDialog
import com.vdotok.app.feature.call.viewmodel.CallViewModel
import com.vdotok.app.models.ActiveSession
import com.vdotok.app.uielements.CustomCallView
import com.vdotok.app.utils.Utils.getCallTitle
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.enums.CallStatus
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import org.webrtc.VideoTrack
import java.util.*


class ConnectedCallFragment : BaseFragment<FragmentCallConnectedBinding, CallViewModel>() {

    override val getLayoutRes: Int = R.layout.fragment_call_connected
    override val getViewModel: Class<CallViewModel> = CallViewModel::class.java


    private var isBroadCastingReceiver = false
    private var isScreenCasting = false
    private var isCamCasting = false
    private var countView: ObservableBoolean = ObservableBoolean(false)
    private var isAppAudioIncluded = false
    private var isAudioCall = false
    private var isPaused = false
    private var usersList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        defaultButtonStates()
        setCallData()
        setBinding()
        setListeners()
        setCountViewVisibility()

        if (isCamCasting || isScreenCasting) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        viewModel.startTimer()
        return mView
    }

    private fun setCountViewVisibility() {
        binding.countView = countView
        viewModel.appManager.activeSession.forEach {
            countView.set(it.value.callType != CallType.ONE_TO_ONE && viewModel.appManager.countParticipant.get() > 0)
        }
    }

    private fun setSwipeListeners() {
        binding.videoStripView1.performSingleClick {
            swapViews(binding.videoStripView1)
        }
        binding.videoStripView2.performSingleClick {
            swapViews(binding.videoStripView2)
        }
        binding.videoStripView3.performSingleClick {
            swapViews(binding.videoStripView3)
        }
        binding.videoStripView4.performSingleClick {
            swapViews(binding.videoStripView4)
        }
//        fullScreenView = selectedView.also { selectedView = fullScreenView }
    }

    private fun swapViews(tag: CustomCallView) {
        var fullScreenViewIndex: Int = -1
        var selectedViewIndex: Int = -1

        viewModel.appManager.videoViews.forEachIndexed { index, view ->
            if (view.isFullView) {
                view.isFullView = false
                fullScreenViewIndex = index
            }
            if (tag.refID == view.viewRenderer?.refID && tag.sessionID == view.viewRenderer?.sessionID) {
                view.isFullView = true
                selectedViewIndex = index
            }
        }
        val fullScreenObject = getViewObjectWithIndex(fullScreenViewIndex)
        val smallScreenObject = getViewObjectWithIndex(selectedViewIndex)

        removeViewRefAndAddViewNewRef(fullScreenObject)
        removeViewRefAndAddViewNewRef(smallScreenObject)

        fullScreenObject.viewRenderer = tag
        smallScreenObject.viewRenderer = binding.videoViewFull

        activity?.runOnUiThread {
            fullScreenObject.viewRenderer?.showHideAvatar(fullScreenObject.isCamPaused)
            fullScreenObject.viewRenderer?.showHideMuteIcon(fullScreenObject.isMuted)
            smallScreenObject.viewRenderer?.showHideAvatar(smallScreenObject.isCamPaused)
            smallScreenObject.viewRenderer?.showHideMuteIcon(smallScreenObject.isMuted)
        }

        addNewViewSinkAndSetSessionIDs(fullScreenObject)
        addNewViewSinkAndSetSessionIDs(smallScreenObject)

        Collections.swap(viewModel.appManager.videoViews, fullScreenViewIndex, selectedViewIndex)

    }

    private fun addNewViewSinkAndSetSessionIDs(sessionObj: ActiveSession) {
        sessionObj.videoTrack.addSink(sessionObj.viewRenderer?.preview)
        sessionObj.viewRenderer?.sessionID = sessionObj.sessionID
        sessionObj.viewRenderer?.refID = sessionObj.refID
    }

    private fun removeViewRefAndAddViewNewRef(sessionObj: ActiveSession) {
        sessionObj.videoTrack.removeSink(sessionObj.viewRenderer?.preview)
        sessionObj.viewRenderer = null

    }

    private fun getViewObjectWithIndex(index: Int) = viewModel.appManager.videoViews[index]

    /**
     * method to set button states after back press
     */
    private fun defaultButtonStates() {
        viewModel.micEnabled()
        viewModel.camViewEnabled()
        viewModel.screenViewEnabled()
        viewModel.internalAudioEnabled()
        speakerStateCheck()

    }

    /**
     * method to set default state of speaker and after back press state of speaker
     */
    private fun speakerStateCheck() {
        if (!viewModel.appManager.speakerState) {
            viewModel.speakerDefaultState()
            viewModel.appManager.speakerState = true
        } else {
            viewModel.speakerEnabledState()
        }
    }

    private fun setListeners() {
        binding.endCall.setOnClickListener {
            viewModel.endCall()
            binding.videoViewFull.release()
        }
        binding.camSwitch.setOnClickListener {
            viewModel.switchCam()
        }

        binding.appAudioOnOff.setOnClickListener {
            viewModel.muteScreenAppAudio()
        }

        binding.screenOnOff.setOnClickListener {
            if (viewModel.isScreenEnable.get())
                viewModel.pauseScreen()
            else
                viewModel.resumeScreen()
        }

        binding.camOnOff.setOnClickListener {
            if (viewModel.isCamEnable.get())
                viewModel.pauseCam()
            else
                viewModel.resumeCam()
        }

        binding.mute.setOnClickListener {
            checkAudioType()
        }

        binding.ivSpeaker.setOnClickListener {
            viewModel.speakerToggle()
        }

        binding.copyURL?.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun checkAudioType() {
        if (viewModel.appManager.isCamEnableInMultiCast && !viewModel.appManager.isAppAudioEnableInMultiCast) {
            viewModel.muteMic()
            viewModel.muteScreenMicAudio()
        } else if (viewModel.appManager.isSSEnableInMultiCast && !viewModel.appManager.isAppAudioEnableInMultiCast) {
            viewModel.muteScreenMicAudio()
        } else {
            viewModel.muteMic()
        }
    }

    private fun showBroadCastDialog() {

        viewModel.appManager.broadCastURL?.let { it1 ->
            CopyURLDialog(it1).show(
                childFragmentManager,
                CopyURLDialog.COPY_URL_TAG
            )
            binding.isPublicURLAvailable = ObservableBoolean(true)
        }

    }

    private var isAlreadyShown = false
    private fun setCallData() {
        for ((key, value) in viewModel.appManager.activeSession) {
            isBroadCastingReceiver = (!value.isInitiator && value.callType == CallType.ONE_TO_MANY)
            if (value.isBroadcast == 1 && !isAlreadyShown) {
                showBroadCastDialog()
                isAlreadyShown = true

            }

            if (key == SessionType.CALL) {
                isCamCasting = value.mediaType == MediaType.VIDEO
                isAudioCall = value.mediaType == MediaType.AUDIO
            } else if (key == SessionType.SCREEN) {
                isScreenCasting = true
                isAppAudioIncluded = value.isAppAudio
            }
            val title = getCallTitle(value.customDataPacket.toString())
            if (value.callType == CallType.ONE_TO_ONE) {
                binding.callTitle = title?.calleName.toString()
            } else {
                binding.callTitle = title?.groupName.toString()
            }


        }
    }

    private fun setBinding() {
        binding.countParticipant = viewModel.appManager.countParticipant
        binding.isCamEnable = viewModel.isCamEnable
        binding.isMicEnable = viewModel.isMicEnable
        binding.isSpeakerEnable = viewModel.isSpeakerEnable
        binding.isBroadCastReceiver = isBroadCastingReceiver
        binding.isAppAudioEnable = viewModel.isAppAudioEnable
        binding.isScreenEnable = viewModel.isScreenEnable
        binding.isScreenCasting = isScreenCasting
        binding.isCamCasting = isCamCasting
        binding.isAppAudioIncluded = isAppAudioIncluded
        binding.isAudioCall = isAudioCall
    }

    override fun onResume() {
        super.onResume()
        checkSessionCancel()
        handlerOrientation()
        setSwipeListeners()
    }

    private fun checkSessionCancel() {
        if (isPaused && viewModel.appManager.activeSession.size == 0) {
            activity?.onBackPressed()
        }
    }

    private fun handlerOrientation() {
        activity?.runOnUiThread {
            viewModel.appManager.videoViews.forEachIndexed { index, view ->
                when (index) {
                    0 -> {
                        view.viewRenderer = binding.videoViewFull
                        view.isFullView = true
                    }
                    1 -> {
                        view.viewRenderer = binding.videoStripView1
                    }
                    2 -> {
                        view.viewRenderer = binding.videoStripView2
                    }
                    3 -> {
                        view.viewRenderer = binding.videoStripView3
                    }
                    4 -> {
                        view.viewRenderer = binding.videoStripView4
                    }
                }
                if (index != 0) {
                    binding.videoStrip.visibility = View.VISIBLE
                    binding.invisibleStrip.visibility = View.VISIBLE
                }

                view.viewRenderer?.let {
                    view.videoTrack.addSink(it.preview)
                    it.visibility = View.VISIBLE
                    it.refID = view.refID
                    it.sessionID = view.sessionID
                    view.viewRenderer?.showHideMuteIcon(view.isMuted)
                    view.viewRenderer?.showHideAvatar(view.isCamPaused)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        removeCurrentView()
    }

    private fun removeCurrentView() {
        viewModel.appManager.videoViews.forEach {
            it.videoTrack.removeSink(it.viewRenderer?.proxyVideoSink)
            it.viewRenderer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun countParticipant(count: Int, participantRefIdList: ArrayList<String>) {
        super.countParticipant(count, participantRefIdList)
        if (!usersList.contains(participantRefIdList.toString())) {
            usersList.add(participantRefIdList.toString())
        }
        viewModel.appManager.countParticipant.set(usersList.size)
        setCountViewVisibility()
    }

    override fun onVideoTrack(stream: VideoTrack, refId: String, sessionID: String) {
        super.onVideoTrack(stream, refId, sessionID)
        val availableViewCount = viewModel.appManager.videoViews.size - 1
        val remoteVideoView = when (availableViewCount) {
            0 -> {
                binding.videoViewFull
            }
            1 -> {
                binding.videoStripView1
            }
            2 -> {
                binding.videoStripView2
            }
            3 -> {
                binding.videoStripView3
            }
            else -> {
                binding.videoStripView4
            }
        }
        if (availableViewCount != 0) {
            binding.videoStrip.visibility = View.VISIBLE
            binding.invisibleStrip.visibility = View.VISIBLE
        }
        stream.addSink(remoteVideoView.preview)
        remoteVideoView.visibility = View.VISIBLE
        viewModel.appManager.videoViews.forEach {
            if (it.refID == refId && it.viewRenderer == null) {
                remoteVideoView.sessionID = it.sessionID
                remoteVideoView.refID = it.refID
                it.viewRenderer = remoteVideoView
                it.viewRenderer?.tag = remoteVideoView.tag
                if (availableViewCount == 0)
                    it.isFullView = true
            }
            if (viewModel.appManager.videoViews.size > 1) {
                if (binding.videoStrip.isShown)
                    animateRemoteView(remoteVideoView)
                else
                    expandVideoStrip(remoteVideoView)
            }
        }
    }

    private fun expandVideoStrip(remoteVideoView: CustomCallView) {
        binding.invisibleStrip.visibility = View.VISIBLE

        val valueAnimator = ValueAnimator.ofInt(
            binding.invisibleStrip.measuredHeight,
            binding.invisibleStrip.measuredHeight
        )
        valueAnimator.addUpdateListener { animation ->
            binding.videoStrip.visibility = View.VISIBLE
            binding.videoStrip.layoutParams.height = animation.animatedValue as Int
            binding.videoStrip.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = 1000
        valueAnimator.start()

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                remoteVideoView.visibility = View.VISIBLE
                animateRemoteView(remoteVideoView)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
    }

    private fun animateRemoteView(remoteVideoView: CustomCallView) {

        val videoStripWidth: Float = binding.videoStrip.width.toFloat()
        val remoteViewWidth: Float = remoteVideoView.width.toFloat()
        val animation = TranslateAnimation(
            videoStripWidth, remoteViewWidth,
            0.0f, 0.0f
        ) //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)

        animation.duration = 1000 // animation duration
        remoteVideoView.animation = animation
    }


    override fun callStatus(callInfoResponse: CallInfoResponse) {
        super.callStatus(callInfoResponse)
        when (callInfoResponse.callStatus) {
            CallStatus.PARTICIPANT_LEFT_CALL -> {
                callInfoResponse.callParams?.let {
                    participantLeftCount(it.toRefIds.toString())
                }
            }
            CallStatus.OUTGOING_CALL_ENDED -> {
                activity?.apply {
                    viewModel.appManager.speakerState = false
                    viewModel.appManager.countParticipant.set(0)
                    finish()
                }
            }
            CallStatus.INSUFFICIENT_BALANCE -> {
                activity?.apply {
                    viewModel.appManager.speakerState = false
                    viewModel.appManager.countParticipant.set(0)
                    finish()
                }

            }
            CallStatus.CALL_MISSED -> {
                callInfoResponse.callParams?.let { callParams ->
                    viewModel.updateCallHistory(callParams.sessionUUID, getString(R.string.status_missed_call))
                }
                activity?.apply {
                    viewModel.appManager.speakerState = false
                    viewModel.appManager.countParticipant.set(0)
                    finish()
                }
            }
            else -> {
            }
        }
    }

    private fun participantLeftCount(refId: String) {
        val ref = usersList.find { it == refId }
        usersList.remove(ref)
        viewModel.appManager.countParticipant.set(usersList.size)
        setCountViewVisibility()
    }


    override fun onTimeTicks(timer: String) {
        super.onTimeTicks(timer)
        binding.timer.text = timer
    }

    override fun onLocalCamera(stream: VideoTrack) {
        handlerOrientation()
    }

    private fun copyTextToClipboard() {
        val textToCopy = viewModel.appManager.broadCastURL
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy.toString())
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
    }

}
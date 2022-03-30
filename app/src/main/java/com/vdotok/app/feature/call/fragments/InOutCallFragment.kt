package com.vdotok.app.feature.call.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.databinding.FragmentCallBinding
import com.vdotok.app.feature.call.CallActivity.Companion.CALL_PARAMS
import com.vdotok.app.feature.call.viewmodel.CallViewModel
import com.vdotok.app.utils.PermissionUtils
import com.vdotok.app.utils.Utils.getCallTitle
import com.vdotok.app.utils.ViewUtils.fadeOut
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.enums.CallStatus
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack

class InOutCallFragment : BaseFragment<FragmentCallBinding, CallViewModel>() {
    override val getLayoutRes: Int = R.layout.fragment_call
    override val getViewModel: Class<CallViewModel> = CallViewModel::class.java
    private var callParams: CallParams? = null
    private var isInitiator = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        arguments?.apply {
            getParcelable<CallParams>(CALL_PARAMS)?.let {
                callParams = it
            }
        }

        callParams?.let {
            isInitiator = it.isInitiator
            binding.isOutgoingCall = it.isInitiator
            binding.isVideoCall = it.mediaType == MediaType.VIDEO
            val title = getCallTitle(it.customDataPacket.toString())
            if (it.callType == CallType.ONE_TO_ONE) {
                binding.callTitle = title?.calleName.toString()
            }else{
                binding.callTitle = title?.groupName.toString()
            }

        } ?: run {
            setBinding()
        }
        setButtonListeners()
        return mView
    }

    private fun setButtonListeners() {
        binding.rejectCall.performSingleClick {
            if (isInitiator)
                viewModel.endCall()
            else
                callParams?.sessionUUID?.let { viewModel.rejectCall(it) }
            activity?.onBackPressed()
        }
        binding.acceptCall.performSingleClick {
            checkPermissionsForCall()
        }
    }

    private fun checkPermissionsForCall() {
        when (callParams?.sessionType) {
            SessionType.CALL -> {
                when (callParams?.mediaType) {
                    MediaType.AUDIO -> {
                        PermissionUtils.getAudioCallPermission(
                            requireContext(),
                            this::acceptIncomingCall,
                            this::rejectIncomingCall,
                            this::rejectIncomingCall
                        )
                    }
                    MediaType.VIDEO -> {
                        PermissionUtils.getVideoCallPermissions(
                            requireContext(),
                            this::acceptIncomingCall,
                            this::rejectIncomingCall,
                            this::rejectIncomingCall
                        )
                    }
                    else -> {}
                }
            }
            SessionType.SCREEN -> {
                PermissionUtils.getVideoCallPermissions(
                    requireContext(),
                    this::acceptIncomingCall,
                    this::rejectIncomingCall,
                    this::rejectIncomingCall
                )
            }
            else -> {}
        }
    }

    private fun acceptIncomingCall() {
        callParams?.let { it1 -> viewModel.acceptIncomingCall(it1) }
        viewModel.appManager.acceptSecondCallIfAny()
        val bundle = Bundle()
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_move_to_connect_call, bundle)
    }

    private fun rejectIncomingCall() {
        callParams?.sessionUUID?.let { viewModel.rejectCall(it) }
        activity?.onBackPressed()
    }

    private fun setBinding() {
        for ((key, value) in viewModel.appManager.activeSession) {
            binding.isOutgoingCall = value.isInitiator
            isInitiator = value.isInitiator
            if (key == SessionType.CALL) {
                binding.isVideoCall = value.mediaType == MediaType.VIDEO
            }
            val title = getCallTitle(value.customDataPacket.toString())
            if (value.callType == CallType.ONE_TO_ONE) {
                binding.callTitle = title?.calleName.toString()
            }else{
                binding.callTitle = title?.groupName.toString()
            }
        }
    }

    override fun callStatus(callInfoResponse: CallInfoResponse) {
        super.callStatus(callInfoResponse)
        Log.e("CallStatus", "InOutCallFragment" + callInfoResponse.callStatus.value)
        when (callInfoResponse.callStatus) {
            CallStatus.CALL_REJECTED -> {
                if (isInitiator) {
                    callInfoResponse.callParams?.let { callParams ->
                        viewModel.appManager.activeSession[callParams.sessionType]?.apply {
                            if (toRefIds.size > 1)
                                viewModel.viewModelScope.launch {
                                    val abc = viewModel.getNameFromRefID(callParams.refId)
                                    Toast.makeText(
                                        requireActivity(),
                                        abc.fullName,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } ?: kotlin.run {
                            updateMessageAndFinishActivity("Call Rejected by User")
                            viewModel.updateCallHistory(
                                callParams.sessionUUID,
                                getString(R.string.status_rejected_call)
                            )
                        }
                    }
                }
            }
            CallStatus.CALL_CONNECTED -> {
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_move_to_connect_call)
            }
            CallStatus.CALL_MISSED -> {
                callInfoResponse.callParams?.let { callParams ->
                    viewModel.updateCallHistory(callParams.sessionUUID, getString(R.string.status_missed_call))
                }
                callInfoResponse.callParams?.sessionType?.let {
                    updateMessageAndFinishActivity("Call Missed")
                }
            }
            CallStatus.OUTGOING_CALL_ENDED -> {
                callInfoResponse.callParams?.sessionType?.let {
                    updateMessageAndFinishActivity(callInfoResponse.responseMessage.toString())
                }
            }
            CallStatus.INSUFFICIENT_BALANCE ->{
                updateMessageAndFinishActivity(callInfoResponse.responseMessage.toString())
            }
            else -> {}
        }
    }

    private fun updateMessageAndFinishActivity(message: String) {
        binding.callMessageVisibility = true
        binding.callMessage = message
        Handler(Looper.getMainLooper()).postDelayed({
            activity?.finish()
        }, 3000)
    }

    override fun onLocalCamera(stream: VideoTrack) {
        super.onLocalCamera(stream)
        try {
            binding.transparentView.fadeOut()
            viewModel.appManager.videoViews[0].apply {
                viewRenderer = binding.ownViewView
                videoTrack.addSink(viewRenderer?.preview)
                viewRenderer?.visibility = View.VISIBLE
            }
        } catch (ex: Exception) {
            Log.i(TAG, "Unable to get local track")
            viewModel.appManager.videoViews[0].viewRenderer = null
        }
    }

    override fun onPause() {
        super.onPause()
        removeLocalView()
    }

    override fun onStop() {
        super.onStop()
        removeLocalView()
    }

    private fun removeLocalView() {
        viewModel.appManager.videoViews.forEach {
            if (it.refID == viewModel.getOwnRefID()) {
                it.videoTrack.removeSink(it.viewRenderer?.preview)
                it.viewRenderer = null
            }
        }
    }
}
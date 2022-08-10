package com.vdotok.app.feature.dashboard.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vdotok.app.BuildConfig
import com.vdotok.app.R
import com.vdotok.app.databinding.FragmentBroadcastOptionsBinding
import com.vdotok.app.utils.DialogUtils
import com.vdotok.app.utils.PermissionUtils
import com.vdotok.app.utils.Utils.isInternalAudioAvailable

class BroadcastOptionsFragment(
    val listener: OnOptionSelection,
    val isGroupBroadcast: Boolean
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBroadcastOptionsBinding

    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_broadcast_options, container, false)

        binding.switchAudioSettings.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Handler(Looper.getMainLooper()).postDelayed({
//                SS is enabled
                    if (binding.switchEnableScreenShare.isChecked) {
                        if (!isInternalAudioAvailable()) {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.app_audio_not_supported),
                                Toast.LENGTH_SHORT
                            ).show()
                            buttonView.isChecked = false
                        }
                    }
//                SS is not enabled
                    else {
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.toggle_screen_share_disabled),
                            Toast.LENGTH_SHORT
                        ).show()
                        buttonView.isChecked = false
                    }
                }, 500)
            }
        }

        binding.switchEnableScreenShare.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                binding.switchAudioSettings.isChecked = false
            }

        }

        binding.btnDone.setOnClickListener {
            if (binding.switchEnableScreenShare.isChecked || binding.switchEnableCamera.isChecked) {
                checkPermissionsForSession()
            }else{
                Toast.makeText(context, getString(R.string.selectType), Toast.LENGTH_SHORT).show()
            }
        }


        binding.isGroupBroadcast = isGroupBroadcast
        binding.imgCloseButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun checkPermissionsForSession() {
        PermissionUtils.getVideoCallPermissions(
            requireContext(), {
                listener.selectedOptions(
                    binding.switchAudioSettings.isChecked,
                    binding.switchEnableCamera.isChecked,
                    binding.switchEnableScreenShare.isChecked
                )
                dismiss()
            }, {}, {
                permissionDeniedDialog(binding.switchEnableCamera.isChecked && binding.switchEnableScreenShare.isChecked)
            }
        )
    }

    private fun permissionDeniedDialog(isMultiSession: Boolean) {
        DialogUtils.showPermissionsDeniedAlert(
            this.activity,
            if (isMultiSession) getString(R.string.broadcast_permission_denied) else getString(R.string.video_permission_denied),
            getString(R.string.grant_permissions)
        ) { dialog, which ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )

        }
    }


    companion object {
        const val TAG = "BroadcastOptionsFragment"
    }

    interface OnOptionSelection {
        fun selectedOptions(
            isAppAudioEnabled: Boolean,
            isCameraEnable: Boolean,
            isSSEnable: Boolean
        )
    }
}
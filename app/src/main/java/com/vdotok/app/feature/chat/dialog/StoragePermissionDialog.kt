package com.vdotok.app.feature.chat.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.vdotok.app.databinding.DialogStoragePermissionBinding
import com.vdotok.app.utils.PermissionUtils

class StoragePermissionDialog(private val permissionGrantedAction : () -> Unit, private val permissionDeniedAction : () -> Unit) : DialogFragment() {

    private lateinit var binding: DialogStoragePermissionBinding

    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = DialogStoragePermissionBinding.inflate(inflater, container, false)

        setupButtonClicks()

        return binding.root
    }

    private fun setupButtonClicks() {
        binding.btnGrantPermissions.setOnClickListener {
            PermissionUtils.getStoragePermission(
                requireContext(),
                {
                    permissionGrantedAction.invoke()
                    dismiss()
                },
                {
                    permissionDeniedAction.invoke()
                    dismiss()
                },
                {
                    permissionDeniedAction.invoke()
                    dismiss()
                },
            )
        }
    }

    companion object {
        const val TAG = "STORAGE_PERMISSION_DIALOG"
    }

    interface InterfacePermissionDialog {
        fun storagePermissionGranted()
        fun storagePermissionDenied()
    }

}
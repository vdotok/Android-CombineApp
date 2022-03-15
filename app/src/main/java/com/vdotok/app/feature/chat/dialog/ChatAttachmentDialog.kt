package com.vdotok.app.feature.chat.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.vdotok.app.databinding.ChatAttachmentDialogBinding
import com.vdotok.app.feature.chat.enums.FileSelectionEnum
import com.vdotok.app.utils.ViewUtils.performSingleClick

class ChatAttachmentDialog(
    private val selectAttachment: (fileAttachmentType: FileSelectionEnum) -> Unit,
    private val openMapAndContact: () -> Unit,
    private val selectAttachmentDoc: (fileAttachmentType: FileSelectionEnum) -> Unit
) : DialogFragment() {

    private lateinit var binding: ChatAttachmentDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = ChatAttachmentDialogBinding.inflate(inflater, container, false)

        setupButtonClicks()

        return binding.root
    }

    private fun setupButtonClicks() {
        binding.closeDialog.performSingleClick {
            dismiss()
        }
        binding.audioOption.performSingleClick {
            selectAttachment.invoke(FileSelectionEnum.AUDIO)
            dismiss()
        }

        binding.albumOption.performSingleClick {
            selectAttachment.invoke(FileSelectionEnum.VIDEO)
            dismiss()
        }

        binding.locationOption.performSingleClick {
            openMapAndContact.invoke()
            dismiss()
        }
        binding.contactOption.performSingleClick {
            openMapAndContact.invoke()
            dismiss()
        }
        binding.cameraOption.performSingleClick {
            selectAttachmentDoc.invoke(FileSelectionEnum.CAM)
            dismiss()
        }
        binding.fileOption.performSingleClick {
            selectAttachmentDoc.invoke(FileSelectionEnum.DOC)
            dismiss()
        }
    }

    companion object {
        const val TAG = "CHAT_ATTACHMENT_DIALOG"
    }
}
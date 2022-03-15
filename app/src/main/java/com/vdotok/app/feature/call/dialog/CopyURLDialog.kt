package com.vdotok.app.feature.call.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.vdotok.app.databinding.CopyUrlDialogBinding


class CopyURLDialog(
    broadCastURL: String,
) : DialogFragment() {

    private lateinit var binding: CopyUrlDialogBinding
    var url = ObservableField(broadCastURL)

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

        binding = CopyUrlDialogBinding.inflate(inflater, container, false)
        binding.url = url


        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            copyTextToClipboard()
            dismiss()
        }

        return binding.root
    }

    private fun copyTextToClipboard() {
        val textToCopy = url.get()
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy.toString())
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val COPY_URL_TAG = "UPDATE_GROUP_DIALOG"
    }

}

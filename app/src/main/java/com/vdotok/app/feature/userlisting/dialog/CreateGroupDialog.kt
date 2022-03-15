package com.vdotok.app.feature.userlisting.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.DialogFragment
import com.vdotok.app.R
import com.vdotok.app.databinding.CustomDialogueBinding
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.utils.ValidationUtils.afterTextChanged

class CreateGroupDialog(private val createGroup : (title: String) -> Unit) : DialogFragment(){

    private lateinit var binding: CustomDialogueBinding
    private var isGroupNameEmpty: ObservableBoolean = ObservableBoolean(false)

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

        binding = CustomDialogueBinding.inflate(inflater, container, false)

        setupUI()
        setupButtonClicks()

        return binding.root
    }

    private fun setupUI() {
        binding.isGroupNameEmpty = isGroupNameEmpty
        binding.edtGroupName.afterTextChanged {
            isGroupNameEmpty.set(it.isEmpty())
        }
    }

    private fun setupButtonClicks() {
        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            if (binding.edtGroupName.text.isNotEmpty()) {
                createGroup.invoke(binding.edtGroupName.text.toString())
                dismiss()
            } else {
                binding.root.showSnackBar(getString(R.string.group_name_empty))
            }
        }
    }

    companion object{
        const val TAG = "CREATE_GROUP_DIALOG"
    }

}

package com.vdotok.app.feature.dashboard.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.vdotok.app.R
import com.vdotok.app.databinding.UpdateGroupNameBinding
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.UpdateGroupNameModel

class UpdateGroupNameDialog(private val groupModel: GroupModel, private val updateGroupCall : (UpdateGroupNameModel) -> Unit) : DialogFragment(){

    private lateinit var binding: UpdateGroupNameBinding
    var edtGroupName = ObservableField<String>()


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

        binding = UpdateGroupNameBinding.inflate(inflater, container, false)
        binding.groupName = edtGroupName


        binding.imgClose.setOnClickListener {
            dismiss()
        }
        edtGroupName.set(groupModel.groupTitle)

        binding.btnDone.setOnClickListener {
            edtGroupName.get()?.isNotEmpty()?.let {groupName ->
                if (groupName) {
                    val model = UpdateGroupNameModel()
                    model.groupId = groupModel.id
                    model.groupTitle = edtGroupName.get()
                    updateGroupCall.invoke(model)
                    dismiss()
                } else {
                    binding.root.showSnackBar(getString(R.string.group_name_empty))
                }
            }
        }

        return binding.root
    }

    companion object{
        const val UPDATE_GROUP_TAG = "UPDATE_GROUP_DIALOG"
    }

}

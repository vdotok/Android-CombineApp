package com.vdotok.app.feature.callHistory.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vdotok.app.R
import com.vdotok.app.utils.image_uri
import com.vdotok.app.databinding.ItemCallHistoryRowBinding
import com.vdotok.app.extensions.ViewExtension
import com.vdotok.app.models.CallHistoryData


class CallHistoryAdapter(var context: Context,
                         callList: List<CallHistoryData>,
) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>() {
    private var dataList: ArrayList<CallHistoryData> = ArrayList()

    init {
        dataList.addAll(callList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CallHistoryViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val callLog = dataList[position]
        holder.binding?.model = callLog
            if (callLog.profilePic.isNullOrEmpty()) {
                holder.binding?.imageAvailable = false
                holder.binding?.dp = setInitials(callLog.fullName)
            } else {
                if (callLog.groupAutoCreatedValue.equals("1")) {
                    holder.binding?.imageAvailable = true
                    holder.binding?.imgUser?.profileImage?.let {
                        Glide.with(context).load(callLog.profilePic)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(it)
                    }
                } else {
                    holder.binding?.imageAvailable = false
                    holder.binding?.dp = setInitials(callLog.fullName)
                }

            }

            holder.binding?.time?.text =
                callLog.time?.let { ViewExtension.currentTimeCalculation(it) }
    }



    fun updateData(userModelList: List<CallHistoryData>) {
        dataList.clear()
        dataList.addAll(userModelList)
        notifyDataSetChanged()
    }

    private fun setInitials(fullName: String?): String? {
        val name :String? = if (fullName?.contains("-") == true){
            val initials = fullName
                .split("-")
                .mapNotNull { it.firstOrNull()?.toString() }
                .reduce { acc, s -> acc + s }
            initials
        }else{
            fullName?.substring(0, 1)
        }
        return name
    }


    class CallHistoryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_call_history_row, parent, false)) {
        var binding: ItemCallHistoryRowBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }

    override fun getItemCount(): Int {
        return  dataList.size
    }
}



package com.vdotok.app.feature.dashboard.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vdotok.app.R
import com.vdotok.app.databinding.ItemGroupRowBinding
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.Presence
import com.vdotok.network.models.GroupModel


class AllGroupsListAdapter(
    private val context: Context,
    private val username: String,
    list: List<GroupModel>,
    var removeUnReadCount: (groupModel: GroupModel) -> Unit,
    private val callback: InterfaceOnGroupMenuItemClick,
    private val getUnreadCount: (groupModel: GroupModel) -> Int,
    private val getLastMessage: (groupModel: GroupModel) -> ArrayList<Message>,
    private val groupItemClick: (groupModel: GroupModel) -> Unit
) :
    RecyclerView.Adapter<UserViewHolder>() {

    var items: ArrayList<GroupModel> = ArrayList()
    var filteredItems: ArrayList<GroupModel> = ArrayList()
    var clickedPosition: Int? = null

    var presenceList: ArrayList<Presence> = ArrayList()

    init {
        items.addAll(list)
        filteredItems.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val model = items[position]
        clickedPosition = position
        getLastMessage.invoke(model)

//        show last message of the group
        if (getLastMessage.invoke(model).size > 0) {
            holder.binding?.showLastMessage = true
            if (getLastMessage.invoke(model).map { it.type }.last().toString().equals("text")) {
                holder.binding?.tvLastMessage?.text =
                    getLastMessage.invoke(model).map { it.content }.last().toString()
            } else {
                holder.binding?.tvLastMessage?.text = context.getString(R.string.attachment)
            }
        } else {
            holder.binding?.showLastMessage = false
        }

        holder.binding?.groupModel = model
        if (getUnreadCount.invoke(model) > 0) {
            holder.binding?.showLastMessage = false
            holder.binding?.showMessageCount = true
            holder.binding?.showMessage = true
            holder.binding?.imgCount?.text = getUnreadCount.invoke(model).toString()
        } else {
            holder.binding?.showMessageCount = false
            holder.binding?.showMessage = false
        }

        if (model.autoCreated == 1) {
            holder.binding?.tvStatus?.text = getOneToOneStatus(model)
            holder.binding?.status =
                holder.binding?.tvStatus?.text?.equals(context.resources.getString(R.string.offline)) == false
        } else {
            holder.binding?.tvStatus?.text = getOneToManyStatus(model)
            holder.binding?.status =
                holder.binding?.tvStatus?.text?.contains(context.resources.getString(R.string.offline)) == false
        }



        model.let {
            if (model.autoCreated == 1) {
                it.participants.forEach { name ->
                    if (name.fullname?.equals(username) == false) {
                        holder.binding?.groupTitle?.text = name.fullname

                    }
                }
            } else {
                holder.binding?.groupTitle?.text = it.groupTitle
            }
        }


        holder.itemView.setOnClickListener {
            removeUnReadCount.invoke(model)
            groupItemClick.invoke(model)
            holder.binding?.showMessageCount = false
            holder.binding?.showMessage = false
        }

        holder.binding?.imgMore?.setOnClickListener {
            val popupWindowObj = showMenuPopupWindow(model)
            holder.binding?.imgMore?.x?.toInt()?.let { x ->
                holder.binding?.imgMore?.y?.toInt()?.let { y ->
                    popupWindowObj.showAsDropDown(
                        holder.binding?.imgMore,
                        x + 50, y
                    )
                }
            }
        }
    }


    private fun showMenuPopupWindow(groupModel: GroupModel): PopupWindow {
        val popupWindow = PopupWindow()

        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.group_listing_menu_items, null)

        val tvEdit: TextView = view.findViewById(R.id.btn_edit)

        if (groupModel.autoCreated == 0) {
            tvEdit.isEnabled = true
            tvEdit.setTextColor(Color.BLACK)
        } else {
            tvEdit.isEnabled = false
            tvEdit.setTextColor(Color.GRAY)
        }

        tvEdit.setOnClickListener {
            callback.onEditGroupClick(groupModel)
            popupWindow.dismiss()
        }

        val tvDelete: TextView = view.findViewById(R.id.btn_delete)
        tvDelete.setOnClickListener {
            groupModel.let {
                callback.onDeleteGroupClick(it)
                popupWindow.dismiss()
            }
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        return popupWindow
    }

    private fun getOneToOneStatus(model: GroupModel): CharSequence {

        var status = context.resources.getString(R.string.offline)

        model.participants.forEach { participantList ->

            presenceList.let { list ->
                list.forEach {
                    if (it.isOnline == 0 && it.username == participantList.refID) {
                        status = context.resources.getString(R.string.online)
                        return@forEach
                    }
                }
            }
        }

        return status
    }

    private fun getOneToManyStatus(model: GroupModel): CharSequence {

        val tempList = ArrayList<String>()
        val size = model.participants.size

        model.participants.forEach { participant ->
            presenceList.let { list ->
                list.forEach {
                    if (it.isOnline == 0 && it.username == participant.refID && tempList.contains(it.username)
                            .not()
                    ) {
                        tempList.add(it.username)
                    }
                }
            }
        }

        return if (tempList.size > 0) {
            tempList.size.toString() + "/" + size + " " + context.resources.getString(R.string.online)
        } else {
            tempList.size.toString() + "/" + size + " " + context.resources.getString(R.string.offline)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(userModelList: List<GroupModel>) {
        items.clear()
        items.addAll(userModelList)
        filteredItems.clear()
        filteredItems.addAll(userModelList)
        notifyDataSetChanged()
    }

    fun updatePresenceData(list: ArrayList<Presence>) {
        presenceList.clear()
        presenceList.addAll(list)
        notifyItemRangeChanged(0, items.size)
    }

}

class UserViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_group_row, parent, false)) {
    var binding: ItemGroupRowBinding? = null

    init {
        binding = DataBindingUtil.bind(itemView)
    }
}

interface InterfaceOnGroupMenuItemClick {
    fun onEditGroupClick(groupModel: GroupModel)
    fun onDeleteGroupClick(groupModel: GroupModel)
}


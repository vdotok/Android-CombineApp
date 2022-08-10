package com.vdotok.app.feature.userlisting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vdotok.app.R
import com.vdotok.app.constants.MAX_PARTICIPANTS
import com.vdotok.app.databinding.ItemAllUsersListWithActionsBinding
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.userlisting.interfaces.OnContactItemClickInterface
import com.vdotok.network.models.UserModel
import java.util.*


class SelectUserContactAdapter(var context: Context,
    list: List<UserModel>,
    isCallActive: Boolean,
    private var isGroupChatList: Boolean,
    private val callbacks: OnContactItemClickInterface
) :
    RecyclerView.Adapter<SelectUserContactViewHolder>(), Filterable {

    var selection = false
    var dataList: ArrayList<UserModel> = ArrayList()
    var filteredItems: ArrayList<UserModel> = ArrayList()
    var callSessionActive: Boolean = false

    init {
        dataList.addAll(list)
        filteredItems.addAll(list)
        callSessionActive = isCallActive
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectUserContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SelectUserContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SelectUserContactViewHolder, position: Int) {
        val listData = dataList[position]
        holder.binding?.userModel = listData
        groupInitialUI(listData,holder)

        if (isGroupChatList) {
            setCreateGroupUserListing(holder, position, listData)
        } else {
            setUserListingUI(holder, position)
        }
    }

    private fun groupInitialUI(listData: UserModel, holder: SelectUserContactViewHolder) {
        if (listData.profilePic.isNullOrEmpty()) {
            holder.binding?.imageAvailable = false
            holder.binding?.dp = setInitials(listData.fullName)
        }else {
            holder.binding?.imageAvailable = true
                holder.binding?.groupInitial?.profileImage?.let { it1 ->
                    Glide.with(context).load(listData.profilePic)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(it1)
                }
        }
    }
    private fun setInitials(fullName: String?): String? {
            val name :String? = fullName?.substring(0, 1)
            return name
    }

    private fun setCreateGroupUserListing(
        holder: SelectUserContactViewHolder,
        position: Int,
        listData: UserModel
    ) {
        holder.binding?.isGroupUsersList = isGroupChatList
        holder.binding?.root?.setOnClickListener {

            if (checkItemExists(listData) || getSelectedUsers().count() < MAX_PARTICIPANTS) {
                callbacks.onItemClick(position)
                selection = true
            } else {
                holder.binding?.root?.showSnackBar("You can create groups with four participants only")
            }
        }

        if (listData.isSelected) {
            holder.binding?.imgUserSelected?.visibility = View.VISIBLE
        } else {
            selection = false
            holder.binding?.imgUserSelected?.visibility = View.GONE
        }
    }

    private fun setUserListingUI(holder: SelectUserContactViewHolder, position: Int) {
        holder.binding?.isCallActive = callSessionActive
        holder.binding?.isGroupUsersList = isGroupChatList

        holder.binding?.chatIcon?.setOnClickListener {
            callbacks.onChatIconClick(position)
        }

        holder.binding?.callIcon?.setOnClickListener {
            callbacks.onCallIconClick(position)
        }

        holder.binding?.videoIcon?.setOnClickListener {
            callbacks.onVideoIconClick(position)
        }

    }

    private fun checkItemExists(userModel: UserModel): Boolean {
        return getSelectedUsers().contains(userModel)
    }

    fun getSelectedUsers(): List<UserModel> {
        val users: ArrayList<UserModel> = ArrayList()

        for (user in filteredItems) {
            when {
                user.isSelected -> users.add(user)
            }
        }
        return users
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(userModelList: List<UserModel>) {
        dataList.clear()
        dataList.addAll(userModelList)
        filteredItems.clear()
        filteredItems.addAll(userModelList)
        notifyItemRangeChanged(0, filteredItems.size)
    }

    fun updateCallIcons(isCallActive: Boolean) {
        callSessionActive = isCallActive
        notifyItemRangeChanged(0, filteredItems.size)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    dataList = filteredItems
                } else {
                    val filteredList: ArrayList<UserModel> = ArrayList()
                    for (row in filteredItems) {

                        if (row.fullName?.split(" ")?.first()?.lowercase(Locale.getDefault())
                                ?.contains(
                                    charString.lowercase(Locale.getDefault())
                                ) == true
                            || row.fullName?.split(" ")?.last()?.lowercase(Locale.getDefault())
                                ?.contains(
                                    charString.lowercase(Locale.getDefault())
                                ) == true
                        ) {
                            filteredList.add(row)
                        }
                    }
                    dataList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = dataList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                dataList = filterResults.values as ArrayList<UserModel>
                callbacks.searchResult(dataList.size)
                notifyDataSetChanged()
            }
        }
    }
}

class SelectUserContactViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.item_all_users_list_with_actions,
            parent,
            false
        )
    ) {
    var binding: ItemAllUsersListWithActionsBinding? = null

    init {
        binding = DataBindingUtil.bind(itemView)
    }
}


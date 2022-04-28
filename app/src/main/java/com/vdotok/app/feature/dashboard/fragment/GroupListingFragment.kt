package com.vdotok.app.feature.dashboard.fragment

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.FragmentGroupListingBinding
import com.vdotok.app.extensions.ViewExtension.hide
import com.vdotok.app.extensions.ViewExtension.show
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.chat.ChatActivity.Companion.createChatActivity
import com.vdotok.app.feature.chat.chatUtils.ChatFileUtils
import com.vdotok.app.feature.chat.dialog.StoragePermissionDialog
import com.vdotok.app.feature.dashboard.adapter.AllGroupsListAdapter
import com.vdotok.app.feature.dashboard.adapter.InterfaceOnGroupMenuItemClick
import com.vdotok.app.feature.dashboard.dialog.BroadcastOptionsFragment
import com.vdotok.app.feature.dashboard.dialog.UpdateGroupNameDialog
import com.vdotok.app.feature.dashboard.viewmodel.DashboardViewModel
import com.vdotok.app.feature.profile.ProfileActivity
import com.vdotok.app.feature.userlisting.AllUsersActivity.Companion.createAllUsersActivity
import com.vdotok.app.utils.DialogUtils.showDeleteGroupAlert
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.app.utils.showPopMenu
import com.vdotok.connect.models.*
import com.vdotok.network.models.*
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class GroupListingFragment : BaseFragment<FragmentGroupListingBinding, DashboardViewModel>(),
    InterfaceOnGroupMenuItemClick {

    private var userData: LoginResponse? = null
    private var groupDataList = ArrayList<GroupModel>()
    private lateinit var adapter: AllGroupsListAdapter
    private var broadcastOptionsFragment: BroadcastOptionsFragment? = null
    private lateinit var chatUtils: ChatFileUtils
    private var storagePermissionDialog: StoragePermissionDialog? = null

    override val getLayoutRes: Int = R.layout.fragment_group_listing
    override val getViewModel: Class<DashboardViewModel> = DashboardViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        userData = UserPreferences.userData as LoginResponse
        chatUtils = context?.let { ChatFileUtils(it, viewModel.appManager) }!!
        setupUI()
        setButtonClicks()
        initGroupListAdapter()
        getAllGroups()
        setGroupDataObserver()
        addPullToRefresh()

        return mView
    }

    private fun setGroupDataObserver() {
        viewModel.getGroupsData().observe(viewLifecycleOwner) { groupList ->
            Log.e("GroupDataObserver", "setGroupDataObserver: insideGroupDataUpdateObserver")
            if (groupList.isNotEmpty()) {
                binding.isGroupListEmpty = false
                handleGroupSuccess(ArrayList(groupList))
            } else {
                binding.isGroupListEmpty = true
            }
        }
    }

    private fun setupUI() {
        binding.isActiveSession = viewModel.appManager.isTimerRunning
        binding.showBackIcon = false
        binding.showCheckIcon = false
        binding.showIcon = true
        binding.toolbarTitle = userData?.fullName?.uppercase()
        setProfileImage()
        viewModel.appManager.messageUpdateLiveData.observe(this.viewLifecycleOwner, { message ->
            adapter.notifyDataSetChanged()
            viewModel.sendAcknowledgeMsgToGroup(message)
        })
    }

    private fun setProfileImage() {
        val user = UserPreferences.userData as LoginResponse
        if (!user.profile_pic.isNullOrEmpty()) {
            binding.imageAvailable = true
            Glide.with(requireContext()).load(user.profile_pic)
                .circleCrop()
                .placeholder(R.drawable.profile_img_holder)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.customToolbar.groupInitial.profileImage)
        } else {
            binding.imageAvailable = false
            binding.dp = userData?.fullName?.substring(0, 1)
        }
    }

    override fun onResume() {
        super.onResume()
        setProfileImage()
    }


    private fun addPullToRefresh() {
        binding.swipeRefreshLay.isEnabled = true
        binding.swipeRefreshLay.setOnRefreshListener {
            getAllGroups()

        }
    }

    private fun setButtonClicks() {

        binding.addGroup.performSingleClick {
            startActivity(createAllUsersActivity(requireContext()))
        }

        binding.btnNewChat.performSingleClick {
            startActivity(createAllUsersActivity(requireContext()))
        }

        binding.btnRefresh.performSingleClick {
            getAllGroups()
        }

        binding.customToolbar.groupInitial.profile.performSingleClick {
            startActivity(ProfileActivity.createProfileActivity(requireContext()))
        }

        binding.customToolbar.optionMenu.performSingleClick {
            context?.let { showPopMenu(it, binding.customToolbar.optionMenu, viewModel) }
        }

        binding.customToolbar.imgDone.performSingleClick {
            broadcastOptionsFragment =
                BroadcastOptionsFragment(object : BroadcastOptionsFragment.OnOptionSelection {
                    override fun selectedOptions(
                        isAppAudioEnabled: Boolean,
                        isCameraEnable: Boolean,
                        isSSEnable: Boolean
                    ) {
                        viewModel.appManager.isAppAudioEnableInMultiCast = isAppAudioEnabled
                        viewModel.appManager.isCamEnableInMultiCast = isCameraEnable
                        viewModel.appManager.isSSEnableInMultiCast = isSSEnable
                        if (isSSEnable) {
                            val captureIntent =
                                activity?.let { it1 -> viewModel.startScreenCapture(it1) }
                            startForProjection.launch(captureIntent)
                        } else {
                            viewModel.startPublicBroadCast(
                                null,
                                activity,
                                false,
                                arrayListOf(),
                                "Public Broadcast",
                                null
                            )
                        }
                    }
                }, false)

            broadcastOptionsFragment?.show(
                childFragmentManager,
                BroadcastOptionsFragment.TAG
            )

        }
    }

    override fun multiSessionReady(sessionIds: Pair<String, String>) {
        viewModel.setupMultiSessionData(
            sessionIds, false,
            arrayListOf(),
            "Public Broadcast",
            null
        )
    }

    private var startForProjection: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activity?.let {
                    viewModel.appManager.getCallClient()
                        ?.initSession(
                            activityResult.data,
                            activityResult.resultCode,
                            it,
                            viewModel.appManager.isAppAudioEnableInMultiCast
                        )
                }
            }
        }


    private fun getAllGroups() {
        binding.swipeRefreshLay.isRefreshing = false
        activity?.let { _ ->
            userData?.authToken?.let { token ->
                viewModel.getAllGroups(token = "Bearer $token").observe(viewLifecycleOwner) {
                    when (it) {
                        is Result.Loading -> {
                            showProgress(requireContext(), getString(R.string.loading_groups))
                        }
                        is Result.Success -> {
                            if (it.data.groups.isNotEmpty())
                                handleGroupListingResponse(it.data.groups)
                            hideProgress()
                        }
                        is Result.Failure -> {
                            hideProgress()
                            if (NetworkConnectivity.isInternetAvailable(this@GroupListingFragment.requireContext())
                                    .not()
                            ) {
                                binding.root.showSnackBar(getString(R.string.no_network_available))
                            } else
                                binding.root.showSnackBar(it.exception.message)
                        }
                    }
                }
            }
        }
    }

    private fun handleGroupListingResponse(newUpdatedGroups: ArrayList<GroupModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            val groupIdList = async {
                getDeletedGroupIds(newUpdatedGroups)
            }
//        delete the groups from DB
            deleteChatForGroup(groupIdList.await())
        }

        viewModel.updateGroupListing(newUpdatedGroups)
    }

    private fun deleteChatForGroup(groupIdsList: ArrayList<Int>) {
//        delete chat from deleted group
        groupIdsList.forEach {
            viewModel.deleteChat(it)
            viewModel.deleteGroupData(it)
        }
    }

    private fun getDeletedGroupIds(newUpdatedGroups: ArrayList<GroupModel>): ArrayList<Int> {
        val lastUpdatedGroups = viewModel.getGroupList()
        var listOfDeletedGroupsIds = ArrayList<Int>()

        lastUpdatedGroups.forEach {
            val id = it.id
            val result = newUpdatedGroups.firstOrNull { it.id == id }
            if (result == null) {
                listOfDeletedGroupsIds.add(id)
            }
        }
        return listOfDeletedGroupsIds
    }

    private fun handleGroupSuccess(groupList: ArrayList<GroupModel>) {
        if (groupList.isEmpty()) {
//                hide the list
            binding.isGroupListEmpty = true
            updateGroupMessageDic(groupList)
        } else {
            groupDataList.clear()
            groupDataList.addAll(groupList)
            UserPreferences.groupList = groupDataList
            addLastMessageGroupToTop()

            binding.isGroupListEmpty = false
            setGroupMapData(groupList)
            updateGroupMessageDic(groupList)

            CoroutineScope(Dispatchers.IO).launch {
                doSubscribe()
            }
        }
    }


    private fun updateGroupMessageDic(groupList: ArrayList<GroupModel>) {
        val map: MutableIterator<MutableMap.MutableEntry<String, ArrayList<com.vdotok.connect.models.Message>>> =
            viewModel.appManager.mapGroupMessages.entries.iterator()
        val deletedGroupList = ArrayList<String>()
        while (map.hasNext()) {
            val key = map.next().key
            val resultValue = groupList.firstOrNull { it.channelName == key }
            if (resultValue == null)
                deletedGroupList.add(key)
        }
        deletedGroupList.forEach {
            viewModel.appManager.mapGroupMessages.remove(it)
            viewModel.appManager.mapLastMessage.remove(it)
            viewModel.appManager.mapUnreadCount.remove(it)
        }
    }

    private fun addLastMessageGroupToTop() {
        val prefsGroupsList = UserPreferences.groupList as ArrayList<GroupModel>
        if (prefsGroupsList.size == groupDataList.size) {
            val lastGroupMessageKey = viewModel.appManager.lastMessageGroupKey
            var lastUpdatedGroupIndex = -1
            var lastUpdatedGroupModel: GroupModel? = null

            groupDataList.forEachIndexed { index, groupModel ->
                if (groupModel.channelKey == lastGroupMessageKey) {
                    lastUpdatedGroupIndex = index
                    lastUpdatedGroupModel = groupModel
                    return@forEachIndexed
                }
            }
            lastUpdatedGroupModel?.let {
                groupDataList.removeAt(lastUpdatedGroupIndex)
                groupDataList.add(0, it)
            }
        }

        adapter.updateData(groupDataList)
    }

    /**
     * Function to persist local chat till the user is connected to the socket
     * @param groupList list of all the groups user is connected to
     * */
    private fun setGroupMapData(groupList: ArrayList<GroupModel>) {
        if (groupList.isNotEmpty()) {
            groupList.forEach { groupModel ->
                if (!viewModel.appManager.mapGroupMessages.containsKey(groupModel.channelName)) {
                    viewModel.appManager.mapGroupMessages[groupModel.channelName] =
                        arrayListOf()
                }
            }
        }
    }

    private fun doSubscribe() {
        Handler(Looper.getMainLooper()).postDelayed({
            for (group in groupDataList) {
                viewModel.appManager.getChatClient()
                    ?.subscribeTopic(group.channelKey, group.channelName)
            }
        }, 2000)

        if (viewModel.appManager.userPresenceList.isNotEmpty()) {
            activity?.runOnUiThread {
                adapter.updatePresenceData(viewModel.appManager.userPresenceList)
            }
        }
    }

    private fun initGroupListAdapter() {
        activity?.applicationContext?.let {
            adapter = userData?.fullName?.let { name ->
                AllGroupsListAdapter(
                    it,
                    name,
                    ArrayList(),
                    { groupModel: GroupModel -> removeUnReadCount(groupModel) },
                    this,
                    { groupModel: GroupModel -> getUnreadCount(groupModel) },
                    { groupModel: GroupModel -> getMessageList(groupModel) }) { groupModel: GroupModel? ->
                    startActivity(groupModel?.let { it ->
                        createChatActivity(requireContext(), it)
                    })
                }
            }!!
            binding.rcvGroupList.adapter = adapter
            adapter.updatePresenceData(viewModel.savedPresenceList)
        }
    }

    private fun getUnreadCount(groupModel: GroupModel): Int {
        return viewModel.appManager.mapUnreadCount[groupModel.channelName]
            ?: return 0
    }

    private fun removeUnReadCount(groupModel: GroupModel) {
        viewModel.appManager.mapUnreadCount.remove(groupModel.channelName)
    }

    private fun getMessageList(groupModel: GroupModel): ArrayList<Message> {
        return viewModel.appManager.mapLastMessage[groupModel.channelName]
            ?: return ArrayList()
    }

    private fun updateGroupCall(updateUserModel: UpdateGroupNameModel) {
        activity?.let { _ ->
            userData?.authToken?.let { token ->
                viewModel.updateGroupName(token = "Bearer $token", updateUserModel)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is Result.Loading -> {
                                showProgress(
                                    requireContext(),
                                    getString(R.string.loading_update_group_name)
                                )
                            }
                            is Result.Success -> {
                                hideProgress()
//                                update the DB value
                                viewModel.updateGroupData(it.data.groupModel)
                                binding.root.showSnackBar(getString(R.string.group_updated))
                            }
                            is Result.Failure -> {
                                hideProgress()
                                if (NetworkConnectivity.isInternetAvailable(this@GroupListingFragment.requireContext())
                                        .not()
                                )
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                else
                                    binding.root.showSnackBar(it.exception.message)
                            }
                        }
                    }
            }
        }
    }

    private fun deleteGroup(deleteGroupModel: DeleteGroupModel, groupModel: GroupModel) {
        activity?.let { _ ->
            userData?.authToken?.let { token ->
                viewModel.deleteGroup(token = "Bearer $token", deleteGroupModel)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is Result.Loading -> {
                                showProgress(
                                    requireContext(),
                                    getString(R.string.loading_delete_group)
                                )
                            }
                            is Result.Success -> {
                                hideProgress()
//                                delete group from DB
                                deleteGroupModel.groupId?.let { it1 ->
                                    viewModel.deleteChat(it1)
                                    viewModel.deleteGroupData(it1)
                                }
                                binding.root.showSnackBar(it.data.message)
                            }
                            is Result.Failure -> {
                                hideProgress()
                                if (NetworkConnectivity.isInternetAvailable(this@GroupListingFragment.requireContext())
                                        .not()
                                )
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                else
                                    binding.root.showSnackBar(it.exception.message)
                            }
                        }
                    }
            }
        }
    }

    fun saveUpdatePresenceList(list: ArrayList<Presence>) {

        list.forEach {
            addUniqueElements(it)
        }

        adapter.updatePresenceData(viewModel.savedPresenceList)
        adapter.notifyItemRangeChanged(0, groupDataList.size)
    }

    private fun addUniqueElements(mPresence: Presence) {

        var isUpdated = false

        viewModel.savedPresenceList.forEachIndexed { index, presence ->
            if (presence.username == mPresence.username) {
                viewModel.savedPresenceList[index] = mPresence
                viewModel.appManager.userPresenceList[index] = mPresence
                isUpdated = true
            }
        }

        if (isUpdated.not()) {
            viewModel.savedPresenceList.add(mPresence)
            viewModel.appManager.userPresenceList.add(mPresence)
        }

    }

    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {
        super.onFileReceivedCompleted(headerModel, byteArray, msgId)
        checkForStoragePermissions(msgId, headerModel, byteArray)
        viewModel.groupModel = groupDataList.find { it.channelName == headerModel.topic }!!
        chatUtils.checkAndroidVersionToSave(headerModel, byteArray) {
            chatUtils.sendAttachmentMessage(
                this::onNewMessage,
                headerModel,
                chatUtils.file,
                msgId,
                viewModel.groupModel
            )
        }
    }

    private fun checkForStoragePermissions(
        msgId: String,
        headerModel: HeaderModel,
        byteArray: ByteArray
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionDialog =
                StoragePermissionDialog(
                    { this.saveFile(msgId, headerModel, byteArray) },
                    { this.sendTextMessage(getString(R.string.msg_storage_permission_denied)) },
                )
            activity?.supportFragmentManager?.let { it1 ->
                storagePermissionDialog?.show(it1, StoragePermissionDialog.TAG)
            }
        } else {
            saveFile(msgId, headerModel, byteArray)
        }

    }

    private fun saveFile(msgId: String, headerModel: HeaderModel, byteArray: ByteArray) {
        chatUtils.checkAndroidVersionToSave(headerModel, byteArray) {
            chatUtils.sendAttachmentMessage(
                this::onNewMessage,
                headerModel,
                chatUtils.file,
                msgId,
                viewModel.groupModel
            )
        }
    }

    /**
     * method to send messages to other user through sdk
     */
    private fun sendTextMessage(message: String) {
        viewModel.groupModel.let {
            val chatModel = viewModel.getUserData().refId?.let { it1 ->
                Message(
                    System.currentTimeMillis().toString(),
                    it.channelName,
                    it.channelKey,
                    it1,
                    MessageType.text,
                    message.trim(),
                    0f,
                    isGroupMessage = viewModel.groupModel.participants.size > 1,
                    ReceiptType.SENT.value
                )
            }
            if (chatModel != null) {
                viewModel.appManager.getChatClient()?.publishMessage(chatModel)
            }
        }
    }

    override fun onEditGroupClick(groupModel: GroupModel) {
        UpdateGroupNameDialog(
            groupModel,
            this::updateGroupCall
        ).show(childFragmentManager, UpdateGroupNameDialog.UPDATE_GROUP_TAG)

    }

    override fun onDeleteGroupClick(groupModel: GroupModel) {
        showDeleteGroupAlert(this.activity,
            DialogInterface.OnClickListener { dialog, which ->
                val model = DeleteGroupModel()
                model.groupId = groupModel.id
                deleteGroup(model, groupModel)
            })
    }

    override fun onPresenceReceived(who: ArrayList<Presence>) {
        saveUpdatePresenceList(who)
    }

    override fun onMessageArrive(message: Message) {
        super.onMessageArrive(message)
        viewModel.appManager.lastMessageGroupKey = message.key
        if (message.from != viewModel.getOwnRefID()) {
            message.to.let {
                viewModel.appManager.mapUnreadCount[it] =
                    viewModel.appManager.mapUnreadCount[it]?.plus(1) ?: 1
            }
        }
        onNewMessage(message)
        viewModel.appManager.updateMessageMapData(message)
    }

    private fun onNewMessage(message: Message) {
        activity?.runOnUiThread {
            addLastMessageGroupToTop()
            adapter.notifyDataSetChanged()
            viewModel.sendAcknowledgeMsgToGroup(message)
        }
        //        insert into DB
        insertChatDBData(message, message.key)
    }

    private fun insertChatDBData(message: Message, key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val groupId = viewModel.getGroupId(key)
            chatUtils.file?.let { file ->
                viewModel.insertChatModel(message, groupId, file.absolutePath)
                chatUtils.file = null
            } ?: kotlin.run {
                viewModel.insertChatModel(message, groupId, "")
            }
        }
    }

    override fun onSSSessionReady(
        mediaProjection: MediaProjection?
    ) {
        super.onSSSessionReady(mediaProjection)
        viewModel.startPublicBroadCast(
            mediaProjection,
            activity,
            false,
            arrayListOf(),
            "Public Broadcast",
            null
        )
    }
}

package com.vdotok.app.feature.userlisting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.databinding.FragmentAllUsersListBinding
import com.vdotok.app.extensions.ViewExtension.hideKeyboard
import com.vdotok.app.feature.call.CallActivity
import com.vdotok.app.feature.userlisting.AllUsersActivity
import com.vdotok.app.feature.userlisting.adapter.SelectUserContactAdapter
import com.vdotok.app.feature.userlisting.interfaces.OnContactItemClickInterface
import com.vdotok.app.feature.userlisting.viewmodel.AllUsersViewModel
import com.vdotok.app.utils.PermissionUtils.getAudioCallPermission
import com.vdotok.app.utils.PermissionUtils.getVideoCallPermissions
import com.vdotok.app.utils.Utils.setCallTitleCustomObject
import com.vdotok.app.utils.ValidationUtils.afterTextChanged
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.app.utils.showPopMenu
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.models.UserModel
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.enums.CallStatus
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AllUsersListFragment : BaseFragment<FragmentAllUsersListBinding, AllUsersViewModel>(),
    OnContactItemClickInterface {

    lateinit var adapter: SelectUserContactAdapter
    var edtSearch = ObservableField<String>()

    override val getLayoutRes: Int = R.layout.fragment_all_users_list
    override val getViewModel: Class<AllUsersViewModel> = AllUsersViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        setupUI()
        setButtonClicks()
        initUserListAdapter()
        setUsersListObserver()

        return mView
    }

    private fun setUsersListObserver() {
        viewModel.getUsersData().observe(viewLifecycleOwner) { usersList ->
            if (usersList.isNotEmpty())
                adapter.updateData(usersList)
        }
    }

    private fun setupUI() {
        binding.showBackIcon = true
        binding.showCheckIcon = false
        binding.showIcon = false
        binding.toolbarTitle = getString(R.string.title_new_chat)
        binding.isActiveSession?.set(false)
        binding.search = edtSearch

        binding.searchEditText.afterTextChanged {
            adapter.filter.filter(it)
        }
    }

    private fun setButtonClicks() {
        binding.customToolbar.imgArrowBack.performSingleClick {
            activity?.onBackPressed()
        }

        binding.customToolbar.optionMenu.performSingleClick {
            context?.let { showPopMenu(it, binding.customToolbar.optionMenu, viewModel) }
        }

        binding.tvAddGroupChat.setOnClickListener {
            activity?.hideKeyboard()
            edtSearch.set("")
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_move_to_create_group_users_list)
        }
    }

    private fun initUserListAdapter() {
        adapter = SelectUserContactAdapter(requireContext(),
            list = ArrayList(),
            isCallActive = viewModel.appManager.isTimerRunning.get(),
            isGroupChatList = false,
            callbacks = this
        )
        binding.rcvUserList.adapter = adapter
    }

    //    adapter callbacks
    override fun onChatIconClick(position: Int) {
        createGroup(position)
    }

    private fun createGroup(position: Int) {
        val title = viewModel.getOwnUsername().plus("-")
        val groupTitle = title.plus(adapter.dataList[position].fullName.toString())
        val model = CreateGroupModel()
        model.groupTitle = groupTitle
        model.pariticpants = getParticipant(adapter.dataList[position])
        model.autoCreated = 1
        (activity as AllUsersActivity).createGroupApiCall(model)


    }


    private fun getParticipant(userModel: UserModel): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        userModel.id?.toInt()?.let { list.add(it) }
        return list
    }

    override fun onCallIconClick(position: Int) {
        getAudioCallPermission(
            requireContext(),
            {
                adapter.dataList[position].refID?.let {
                    dialOne2OneCall(
                        MediaType.AUDIO,
                        arrayListOf(it),
                        adapter.dataList[position].fullName.toString()
                    )
                }
            }, {},
            this::showAudioPermissionsRequiredDialog
        )
    }

    override fun onVideoIconClick(position: Int) {
        getVideoCallPermissions(
            requireContext(),
            {
                adapter.dataList[position].refID?.let {
                    dialOne2OneCall(
                        MediaType.VIDEO,
                        arrayListOf(it),
                        adapter.dataList[position].fullName.toString()
                    )
                }
            }, {},
            {
                showVideoCallPermissionsRequiredDialog(false)
            }
        )
    }

    override fun callStatus(callInfoResponse: CallInfoResponse) {
        super.callStatus(callInfoResponse)
        when (callInfoResponse.callStatus) {
            CallStatus.OUTGOING_CALL_ENDED -> {
                activity?.runOnUiThread {
                    adapter.updateCallIcons(viewModel.appManager.isTimerRunning.get())
                }
            }
            else -> {
            }
        }
    }

    private fun dialOne2OneCall(
        mediaType: MediaType,
        refIds: ArrayList<String>,
        callTitle: String
    ) {

        var participantsId :String? = null
        refIds.forEach {
           participantsId = it
        }
        val callParams = CallParams(
            refId = viewModel.getOwnRefID(),
            toRefIds = refIds,
            isInitiator = true,
            isBroadcast = 0,
            mediaType = mediaType,
            callType = CallType.ONE_TO_ONE,
            sessionType = SessionType.CALL,
            customDataPacket = setCallTitleCustomObject(viewModel.getOwnUsername(),null,"1")
        )
        val session = viewModel.appManager.getCallClient()?.dialOne2OneCall(callParams)
        session?.let { it1 ->
            val tempCallParams = callParams.copy()
            tempCallParams.sessionUUID = it1
            tempCallParams.customDataPacket = setCallTitleCustomObject(callTitle,null,"1")
            viewModel.appManager.setSession(SessionType.CALL, tempCallParams)
        }

        participantsId?.let {
            viewModel.insertCallHistory(
                callParams,
                it,
                viewModel.resourcesProvider.getString(R.string.status_outgoing_call),
                true,
                callTitle

            )
        }
        startActivity(CallActivity.createCallActivity(requireContext()))
    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            binding.emptyUserList = position == 0 && it
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchEditText.setText("")
        adapter.updateCallIcons(viewModel.appManager.isTimerRunning.get())
    }
}
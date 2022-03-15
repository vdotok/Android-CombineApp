package com.vdotok.app.feature.userlisting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.FragmentCreateGroupChatBinding
import com.vdotok.app.extensions.ViewExtension.hideKeyboard
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.userlisting.AllUsersActivity
import com.vdotok.app.feature.userlisting.adapter.SelectUserContactAdapter
import com.vdotok.app.feature.userlisting.dialog.CreateGroupDialog
import com.vdotok.app.feature.userlisting.interfaces.OnContactItemClickInterface
import com.vdotok.app.feature.userlisting.viewmodel.AllUsersViewModel
import com.vdotok.app.utils.ValidationUtils.afterTextChanged
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.app.utils.showPopMenu
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.UserModel


class CreateGroupChatFragment : BaseFragment<FragmentCreateGroupChatBinding, AllUsersViewModel>(),
    OnContactItemClickInterface {

    lateinit var adapter: SelectUserContactAdapter
    var edtSearch = ObservableField<String>()
    private var userData: LoginResponse? = null

    override val getLayoutRes: Int = R.layout.fragment_create_group_chat
    override val getViewModel: Class<AllUsersViewModel> = AllUsersViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        userData = UserPreferences.userData as LoginResponse

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
        binding.showCheckIcon = true
        binding.showIcon = true
        binding.toolbarTitle = getString(R.string.create_group_chat)
        binding.search = edtSearch
        binding.isActiveSession?.set(false)

        binding.searchEditText.afterTextChanged {
            adapter.filter?.filter(it)
        }
    }

    private fun setButtonClicks() {
        binding.customToolbar.imgArrowBack.performSingleClick {
            activity?.onBackPressed()
        }

        binding.customToolbar.optionMenu.performSingleClick {
            context?.let { showPopMenu(it, binding.customToolbar.optionMenu, viewModel) }
        }

        binding.customToolbar.imgDone.performSingleClick {
            activity?.hideKeyboard()
            if (adapter.getSelectedUsers().isNotEmpty()) {
                onCreateGroupClick()
            } else {
                binding.root.showSnackBar(getString(R.string.no_user_select))
            }
        }
    }

    private fun onCreateGroupClick() {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()

        if (selectedUsersList.isNotEmpty() && selectedUsersList.size == 1) {
            getGroupTitle(selectedUsersList).let {
                if (it != null) {
                    createGroup(it)
                }
            }
        } else {
            CreateGroupDialog(this::createGroup).show(
                childFragmentManager,
                CreateGroupDialog.TAG
            )
        }
    }

    private fun getGroupTitle(selectedUsersList: List<UserModel>): String {
        val userdata = UserPreferences.userData as LoginResponse
        var title = userdata.fullName.plus("-")
        //In this case, we have only one item in list
        selectedUsersList.forEach {
            title = title.plus(it.userName.toString())
        }
        return title
    }

    private fun createGroup(title: String) {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()

        if (selectedUsersList.isNotEmpty()) {

            val model = CreateGroupModel()
            model.groupTitle = title
            //model.auto_created -> set auto created group, set 1 for only single user, 0 for multiple users
            model.pariticpants = getParticipantsIds(selectedUsersList)

            when (selectedUsersList.size) {
                1 -> model.autoCreated = 1
                else -> model.autoCreated = 0
            }

            (activity as AllUsersActivity).createGroupApiCall(model)
        }
    }

    /**
     * Function for setting participants ids
     * @param selectedUsersList list of selected users to form a group with
     * @return Returns an ArrayList<Int> of selected user ids
     * */
    private fun getParticipantsIds(selectedUsersList: List<UserModel>): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        selectedUsersList.forEach { userModel ->
            userModel.id?.let { list.add(it.toInt()) }
        }
        return list
    }

    private fun initUserListAdapter() {
        adapter = SelectUserContactAdapter(requireContext(),
            list = ArrayList(),
            isCallActive = false,
            isGroupChatList = true,
            callbacks = this
        )
        binding.rcvUserList.adapter = adapter
    }

    override fun onItemClick(position: Int) {
        val item = adapter.dataList[position]
        item.isSelected = item.isSelected.not()
        adapter.notifyItemChanged(position)
    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            binding.emptyUserList = position == 0 && it
        }
    }


}
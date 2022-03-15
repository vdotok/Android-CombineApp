package com.vdotok.app.feature.userlisting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.ActivityAllUsersBinding
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.chat.ChatActivity
import com.vdotok.app.feature.userlisting.viewmodel.AllUsersViewModel
import com.vdotok.connect.models.Presence
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.models.CreateGroupResponse
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.UserModel
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllUsersActivity : BaseActivity<ActivityAllUsersBinding, AllUsersViewModel>() {

    private var userData: LoginResponse? = null
    var userList = ArrayList<UserModel>()

    override val getLayoutRes: Int = R.layout.activity_all_users
    override val getViewModel: Class<AllUsersViewModel> = AllUsersViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.findFragmentById(R.id.all_users_nav_host_fragment) as NavHostFragment

        userData = UserPreferences.userData as LoginResponse
        getAllUsers()

    }

    private fun getAllUsers() {
        val userData = UserPreferences.userData as LoginResponse
        userData.authToken?.let { token ->
            viewModel.getAllUsers(token = "Bearer $token").observe(this) {
                when (it) {
                    is Result.Loading -> {
                        showProgress(getString(R.string.loading_users_list))
                    }
                    is Result.Success -> {
                        userList = it.data.users as ArrayList<UserModel>
                        userList.sortBy {
                       it.fullName}
                       viewModel.updateUsersList(userList)
                       hideProgress()
                    }
                    is Result.Failure -> {
                        if (NetworkConnectivity.isInternetAvailable(this).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)
                    }
                }
            }
        }
    }

   fun createGroupApiCall(model: CreateGroupModel) {
        val userData = UserPreferences.userData as LoginResponse
            userData.authToken?.let { token ->
                viewModel.createGroup(token = "Bearer $token", model = model)
                    .observe(this) {
                        when (it) {
                            is Result.Loading -> {
                                showProgress(
                                    getString(R.string.loading_creating_group)
                                )
                            }
                            is Result.Success -> {
                                hideProgress()
                                binding.root.showSnackBar(getString(R.string.group_created))
                                handleCreateGroupSuccess(it.data)
                            }
                            is Result.Failure -> {
                                hideProgress()
                                if (NetworkConnectivity.isInternetAvailable(this)
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

    private fun handleCreateGroupSuccess(response: CreateGroupResponse) {
        response.groupModel?.let {
            response.groupModel?.let {
//                perform new group subscription and save group in local map
                viewModel.appManager.getChatClient()
                    ?.subscribeTopic(it.channelKey, it.channelName)

                viewModel.appManager.mapGroupMessages[it.channelName] =
                    arrayListOf()

                startActivity(ChatActivity.createChatActivityFromCreateGroup(this, it))
            }
        }
    }

    override fun onPresenceReceived(who: java.util.ArrayList<Presence>) {
        super.onPresenceReceived(who)
        who.forEach {
            viewModel.appManager.userPresenceList.add(it)
        }
    }

    companion object {
        fun createAllUsersActivity(context: Context) = Intent(
            context,
            AllUsersActivity::class.java
        )
    }
}
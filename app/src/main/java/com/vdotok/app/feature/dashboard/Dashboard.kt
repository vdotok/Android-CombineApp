package com.vdotok.app.feature.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.ActivityDashboardBinding
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.dashboard.viewmodel.DashboardViewModel
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import dagger.hilt.android.AndroidEntryPoint


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:39 AM in 2021
 */

@AndroidEntryPoint
class Dashboard : BaseActivity<ActivityDashboardBinding, DashboardViewModel>() {

    override val getLayoutRes: Int = R.layout.activity_dashboard
    override val getViewModel: Class<DashboardViewModel> = DashboardViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBindingData()
        getRegister()
        getAllUsers()
    }

    private fun getRegister() {
        viewModel.appManager.connectChatSdk()
        if (!viewModel.appManager.callSDKRegistrationStatus.get())
            viewModel.appManager.connect()
    }

    private fun setBindingData() {
        binding.isCallConnected = viewModel.appManager.callSDKStatus
        binding.isChatConnected = viewModel.appManager.chatSDKStatus
    }

    private fun getAllUsers() {
        val userData = UserPreferences.userData as LoginResponse
        userData.authToken?.let { token ->
            viewModel.getAllUsers(token = "Bearer $token").observe(this) {
                when (it) {
                    is Result.Loading -> {
                    }
                    is Result.Success -> {
                        viewModel.updateUsersList(it.data.users)
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

    override fun onResume() {
        super.onResume()
        checkSDKConnection()
    }

    private fun checkSDKConnection() {
        viewModel.appManager.callSDKStatus.set(viewModel.appManager.getCallClient()?.isConnected() == true)
        viewModel.appManager.chatSDKStatus.set(viewModel.appManager.getChatClient()?.isConnected() == true)
        binding.isCallConnected = viewModel.appManager.callSDKStatus
        binding.isChatConnected = viewModel.appManager.chatSDKStatus
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    companion object {
        fun createDashBoardActivity(context: Context) = Intent(
            context,
            Dashboard::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }

}


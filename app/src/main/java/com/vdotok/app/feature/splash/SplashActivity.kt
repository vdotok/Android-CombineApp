package com.vdotok.app.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.ActivitySplashBinding
import com.vdotok.app.feature.account.AccountActivity.Companion.createAccountsActivity
import com.vdotok.app.feature.dashboard.Dashboard.Companion.createDashBoardActivity
import com.vdotok.app.feature.splash.viewmodel.SplashViewModel
import com.vdotok.app.services.OnClearFromRecentService
import dagger.hilt.android.AndroidEntryPoint


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:26 PM in 2021
 */

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    override val getLayoutRes: Int = R.layout.activity_splash
    override val getViewModel: Class<SplashViewModel> = SplashViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(this, OnClearFromRecentService::class.java))


        checkUserData()

    }

    private fun checkUserData() {
        Handler(Looper.getMainLooper()).postDelayed({

            UserPreferences.userData?.let {
                startActivity(applicationContext?.let { createDashBoardActivity(it) })
                finish()

            } ?: kotlin.run {
                startActivity(createAccountsActivity(this))
                finish()

            }

        }, 2000)
    }
}
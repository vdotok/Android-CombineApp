package com.vdotok.app.feature.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.databinding.ActivityProfileBinding
import com.vdotok.app.feature.profile.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity<ActivityProfileBinding, ProfileViewModel>() {

    override val getLayoutRes: Int = R.layout.activity_profile
    override val getViewModel: Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.findFragmentById(R.id.nav_host_profile_fragment) as NavHostFragment
    }

    companion object {
        fun createProfileActivity(context: Context) = Intent(
            context,
            ProfileActivity::class.java
        )
    }
}
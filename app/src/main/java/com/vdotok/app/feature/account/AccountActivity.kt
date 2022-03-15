package com.vdotok.app.feature.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.databinding.ActivityUserAccountBinding
import com.vdotok.app.feature.account.viewmodel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:26 PM in 2021
 */
@AndroidEntryPoint
class AccountActivity : BaseActivity<ActivityUserAccountBinding, AccountViewModel>() {
    override val getLayoutRes: Int = R.layout.activity_user_account
    override val getViewModel: Class<AccountViewModel> = AccountViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    companion object {
        fun createAccountsActivity(context: Context) = Intent(
            context,
            AccountActivity::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }

}
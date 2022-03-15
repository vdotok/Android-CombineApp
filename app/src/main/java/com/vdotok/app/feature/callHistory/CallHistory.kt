package com.vdotok.app.feature.callHistory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.databinding.ActivityCallHistoryBinding
import com.vdotok.app.feature.callHistory.viewmodel.CallHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallHistory : BaseActivity<ActivityCallHistoryBinding, CallHistoryViewModel>() {

    override val getLayoutRes: Int = R.layout.activity_call_history
    override val getViewModel: Class<CallHistoryViewModel> = CallHistoryViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.findFragmentById(R.id.nav_host_call_history_fragment) as NavHostFragment
    }

    companion object {
        fun createCallHistoryActivity(context: Context) = Intent(
            context,
            CallHistory::class.java
        )
    }
}
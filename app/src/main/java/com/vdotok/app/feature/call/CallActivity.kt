package com.vdotok.app.feature.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.databinding.ActivityCallBinding
import com.vdotok.app.feature.call.viewmodel.CallViewModel
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 4:26 PM in 2021
 */

@AndroidEntryPoint
class CallActivity : BaseActivity<ActivityCallBinding, CallViewModel>() {
    override val getLayoutRes: Int = R.layout.activity_call
    override val getViewModel: Class<CallViewModel> = CallViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appManager.isCallActivityOpened.value = true
        val nav =
            supportFragmentManager.findFragmentById(R.id.nav_host_call_fragment) as NavHostFragment
        val graphInflater = nav.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.call_nav)
        val destination = if (intent.getStringExtra(SELECTED_SCREEN) == "connected")
            R.id.callConnectedFragment
        else
            R.id.callFragment

        nav.navController.apply {
            Bundle().apply {
                val callParams = intent.getParcelableExtra<CallParams>(CALL_PARAMS)
                callParams?.let {
                    if (it.mediaType == MediaType.AUDIO)
                        enableProximitySensor(true)
                }
                putParcelable(CALL_PARAMS, callParams)
                navGraph.setStartDestination(destination)
                setGraph(navGraph, this)
            }
        }
        viewModel.appManager.activeSession[SessionType.CALL]?.apply {
            if (mediaType == MediaType.AUDIO)
                enableProximitySensor(true)
        }
    }

    companion object {
        val CALL_TITLE = "call_title"
        val SELECTED_SCREEN = "selected_screen"
        val CALL_PARAMS = "call_params"
        fun createCallActivity(context: Context) = Intent(context, CallActivity::class.java).apply {
            putExtra(SELECTED_SCREEN, "not_connected")
        }

        fun createCallActivityForCallConnectedFragment(context: Context) =
            Intent(context, CallActivity::class.java).apply {
                putExtra(SELECTED_SCREEN, "connected")
            }

        fun createCallActivity(context: Context, callParams: CallParams) =
            Intent(context, CallActivity::class.java).apply {
                putExtra(CALL_PARAMS, callParams)
                putExtra(SELECTED_SCREEN, "not_connected")
            }

    }

    override fun onResume() {
        super.onResume()
        viewModel.appManager.isCallActivityOpened.value = true
    }

    override fun onStop() {
        super.onStop()
        viewModel.appManager.isCallActivityOpened.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.appManager.isCallActivityOpened.value = false
    }

    override fun onBackPressed() {
        viewModel.appManager.isCallActivityOpened.value = false
        finish()
    }
}
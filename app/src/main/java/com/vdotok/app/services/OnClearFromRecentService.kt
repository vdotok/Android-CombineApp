package com.vdotok.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.vdotok.app.manager.AppManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnClearFromRecentService : Service() {

    @Inject
    lateinit var appManager: AppManager

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("ClearFromRecentService", "Service Started")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ClearFromRecentService", "Service Destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.e("ClearFromRecentService", "END")
        appManager.getChatClient()?.disconnect()
        stopSelf()
    }
}
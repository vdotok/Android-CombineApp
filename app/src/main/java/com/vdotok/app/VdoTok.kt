package com.vdotok.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.vdotok.app.base.BasePreferences
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.manager.AppManager
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 1:21 PM in 2021
 */
@HiltAndroidApp
class VdoTok : Application() {
    lateinit var appManager: AppManager
    @Inject set
    private var lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (appManager.activeSession.size != 0) {
                    for (entry in appManager.activeSession.values) {
                        if (entry.mediaType == MediaType.VIDEO && entry.sessionType == SessionType.CALL){
                          appManager.getCallClient()?.resumeVideo(appManager.getOwnRefID(),entry.sessionUUID)
                        }
                    }
                }else{
                    Log.d("alpha12","bbbbb")
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                if (appManager.activeSession.size != 0) {
                    for (entry in appManager.activeSession.values) {
                        if (entry.mediaType == MediaType.VIDEO && entry.sessionType == SessionType.CALL){
                            appManager.getCallClient()?.pauseVideo(appManager.getOwnRefID(),entry.sessionUUID)
                        }
                    }
                }else{
                    Log.d("alpha12","hhhhh")
                }
            }
            else -> {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
        BasePreferences.init(this)
    }

}
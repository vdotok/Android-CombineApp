package com.vdotok.app

import android.app.Application
import com.vdotok.app.base.BasePreferences
import dagger.hilt.android.HiltAndroidApp


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 1:21 PM in 2021
 */
@HiltAndroidApp
class VdoTok : Application() {
    override fun onCreate() {
        super.onCreate()
        BasePreferences.init(this)
    }
}
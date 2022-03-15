package com.vdotok.network.network

import android.content.Context

/**
 * Created By: VdoTok
 * Date & Time: On 11/8/21 At 1:30 PM in 2021
 */
object NetworkConnectivity {

    fun isInternetAvailable(context: Context): Boolean {
        return ConnectivityStatus(context).isConnected()
    }
}
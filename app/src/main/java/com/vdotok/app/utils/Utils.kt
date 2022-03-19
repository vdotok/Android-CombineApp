package com.vdotok.app.utils

import android.content.Context
import android.util.TypedValue
import com.google.gson.Gson
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.models.CallNameModel
import com.vdotok.connect.models.Connection
import com.vdotok.network.models.LoginResponse
import org.eclipse.paho.client.mqttv3.MqttAsyncClient


/**
 * Created By: VdoTok
 * Date & Time: On 16/11/2021 At 12:35 PM in 2021
 */
object Utils {
    fun createMessagingConnection(response: LoginResponse) {
        val connection = response.messagingServer?.let { msgServerUrl ->
            response.refId?.let { refId ->
                response.authorizationToken?.let { token ->
                    Connection(
                        refId,
                        token,
                        msgServerUrl.host,
                        msgServerUrl.port,
                        true,
                        MqttAsyncClient.generateClientId(),
                        5,
                        true
                    )
                }
            }
        }
        UserPreferences.messagingConnection = connection
    }

    fun convertDpIntoPx(mContext: Context, yourdpmeasure: Float): Int {
        val r = mContext.resources
        return TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, yourdpmeasure, r.displayMetrics)
            .toInt()
    }

    fun setCallTitleCustomObject(calleName: String?, groupName: String?, autoCreated: String?): String {
        return Gson().toJson(CallNameModel(calleName,groupName,autoCreated), CallNameModel::class.java)
    }

    fun getCallTitle(customObject: String): CallNameModel? {
        return Gson().fromJson(customObject, CallNameModel::class.java)
    }

    fun isInternalAudioAvailable() =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
}

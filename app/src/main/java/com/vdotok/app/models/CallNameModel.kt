package com.vdotok.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Created By: VdoTok
 * Date & Time: On 20/12/2021 At 6:54 PM in 2021
 */
@Parcelize
data class CallNameModel(
    var calleName: String? = null,
    var groupAutoCreatedValue :String? = null
) : Parcelable
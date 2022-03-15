package com.vdotok.app.models

import android.os.Parcelable
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import kotlinx.parcelize.Parcelize

/**
 * Created By: VdoTok
 * Date & Time: On 07/12/2021 At 1:28 PM in 2021
 */

@Parcelize
data class CallHistoryData(
    var sessionId: String? = null,
    var fullName: String? = null,

    var participantRefId: String? = null,

    var callStatus: String? = null,

    var time: Long? = null,

    var associateId: String? = null,

    var mediaType: MediaType,

    var sessionType: SessionType,
    var groupAutoCreatedValue: String? = null,
    var id: Int = 0,

    var profilePic: String? = null

    ) : Parcelable

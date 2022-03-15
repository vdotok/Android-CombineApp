package com.vdotok.app.models

import com.vdotok.app.uielements.CustomCallView
import org.webrtc.VideoTrack


/**
 * Created By: VdoTok
 * Date & Time: On 07/12/2021 At 1:28 PM in 2021
 */
data class ActiveSession constructor(
    val refID: String,
    val sessionID: String,
    var videoTrack: VideoTrack,
    var viewRenderer: CustomCallView? = null,
    var isMuted: Boolean = false,
    var isCamPaused: Boolean = false,
    var isOwnView: Boolean = false,
    var isFullView: Boolean = false
)

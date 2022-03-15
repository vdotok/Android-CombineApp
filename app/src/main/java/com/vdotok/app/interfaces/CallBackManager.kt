package com.vdotok.app.interfaces

import android.media.projection.MediaProjection
import com.vdotok.connect.models.HeaderModel
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.Presence
import com.vdotok.connect.models.ReadReceiptModel
import com.vdotok.streaming.commands.CallInfoResponse
import com.vdotok.streaming.commands.RegisterResponse
import com.vdotok.streaming.enums.EnumConnectionStatus
import com.vdotok.streaming.models.CallParams
import com.vdotok.streaming.models.SessionStateInfo
import org.webrtc.VideoTrack


/**
 * Created By: VdoTok
 * Date & Time: On 29/11/2021 At 7:52 PM in 2021
 */
interface CallBackManager {
    fun callConnectionStatus(enumConnectionStatus: EnumConnectionStatus)
    fun chatConnectionStatus(message: String)
    fun callRegistrationStatus(registerResponse: RegisterResponse)
    fun callStatus(callInfoResponse: CallInfoResponse)
    fun onMessageArrive(message: Message) {}
    fun onTypingMessage(message: Message) {}
    fun onLocalCamera(stream: VideoTrack)
    fun onAudioTrack(refId: String, sessionID: String)
    fun onVideoTrack(stream: VideoTrack, refId: String, sessionID: String)
    fun onFileSend(fileHeaderId: String, fileType: Int){}
    fun onAttachmentProgressSend(fileHeaderId: String, progress: Int){}
    fun onFileReceiveFailed(){}
    fun onByteReceived(payload: ByteArray){}
    fun onReceiptReceive(model: ReadReceiptModel){}
    fun onFileReceivedCompleted(headerModel: HeaderModel, byteArray: ByteArray, msgId: String){}
    fun onPresenceReceived(who: ArrayList<Presence>)
    fun countParticipant(count:Int, participantRefIdList: ArrayList<String>){}
    fun audioVideoState(sessionStateInfo: SessionStateInfo)
    fun incomingCall(callParams: CallParams)
    fun onTimeTicks(timer: String)
    fun onSSSessionReady(mediaProjection: MediaProjection?)
    fun onPublicURL(url: String)
}
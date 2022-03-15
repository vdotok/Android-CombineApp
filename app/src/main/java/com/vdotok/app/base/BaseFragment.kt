package com.vdotok.app.base

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.norgic.spotsdialog.SpotsDialog
import com.vdotok.app.BuildConfig
import com.vdotok.app.R
import com.vdotok.app.interfaces.CallBackManager
import com.vdotok.app.utils.DialogUtils
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

abstract class BaseFragment<DB : ViewDataBinding, VM : BaseViewModel> : Fragment(),
    CallBackManager {

    @get:LayoutRes
    protected abstract val getLayoutRes: Int
    protected abstract val getViewModel: Class<VM>
    lateinit var binding: DB
    lateinit var viewModel: VM
    var mView: View? = null
    private lateinit var spotsDialog: AlertDialog
    val TAG: String = "BaseFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProvider(it)[getViewModel]
        } ?: kotlin.run {
            viewModel = ViewModelProvider(this)[getViewModel]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutRes, container, false)

        return binding.root
    }

    fun showProgress(context: Context, message: String) {
        spotsDialog = SpotsDialog.Builder()
            .setContext(context)
            .setMessage(message)
            .setCancelable(false)
            .setTheme(R.style.LoadingStyleTransparent)
            .build()
        spotsDialog.show()
    }

    fun hideProgress() {
        spotsDialog.dismiss()
    }

    override fun callConnectionStatus(enumConnectionStatus: EnumConnectionStatus) {

    }

    override fun chatConnectionStatus(message: String) {

    }

    override fun callRegistrationStatus(registerResponse: RegisterResponse) {

    }

    override fun callStatus(callInfoResponse: CallInfoResponse) {

    }

    override fun onMessageArrive(message: Message) {

    }

    override fun onTypingMessage(message: Message) {

    }

    override fun onReceiptReceive(model: ReadReceiptModel) {

    }

    override fun onFileSend(fileHeaderId: String, fileType: Int) {

    }

    override fun countParticipant(count: Int, participantRefIdList: ArrayList<String>) {

    }

    override fun onByteReceived(payload: ByteArray) {

    }


    override fun onAttachmentProgressSend(fileHeaderId: String, progress: Int) {

    }

    override fun onPresenceReceived(who: ArrayList<Presence>) {
        Log.i("presense", "successfully subscribed")

    }

    override fun onLocalCamera(stream: VideoTrack) {
        Log.d(TAG, "onLocalCam")
    }

    override fun onAudioTrack(refId: String, sessionID: String) {

    }

    override fun onFileReceiveFailed() {

    }

    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {

    }

    override fun onVideoTrack(stream: VideoTrack, refId: String, sessionID: String) {
        Log.d(TAG, "onVideoTrack")
    }

    override fun onResume() {
        super.onResume()
        viewModel.appManager.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.appManager.removeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.appManager.removeListener(this)
    }

    fun showAudioPermissionsRequiredDialog() {
        DialogUtils.showPermissionsDeniedAlert(
            this.activity,
            getString(R.string.audio_permission_denied),
            getString(R.string.grant_permissions)
        ) { dialog, which ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }

    fun showVideoCallPermissionsRequiredDialog(isMultiSession: Boolean) {
        DialogUtils.showPermissionsDeniedAlert(
            this.activity,
            if (isMultiSession) getString(R.string.broadcast_permission_denied) else getString(R.string.video_permission_denied),
            getString(R.string.grant_permissions)
        ) { dialog, which ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )

        }
    }

    fun showReadWritePermissionsRequiredDialog() {
        DialogUtils.showPermissionsDeniedAlert(
            this.activity,
            getString(R.string.read_write_permission_denied),
            getString(R.string.grant_permissions)
        ) { dialog, which ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )

        }
    }

    override fun audioVideoState(sessionStateInfo: SessionStateInfo) {
    }

    override fun incomingCall(callParams: CallParams) {

    }

    override fun onTimeTicks(timer: String) {

    }

    override fun onSSSessionReady(
        mediaProjection: MediaProjection?
    ) {
        Log.i(TAG, "Session Ready!")
    }

    override fun onPublicURL(url: String) {

    }
}

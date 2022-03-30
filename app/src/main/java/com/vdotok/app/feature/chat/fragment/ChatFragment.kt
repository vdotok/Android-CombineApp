package com.vdotok.app.feature.chat.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.databinding.FragmentChatBinding
import com.vdotok.app.extensions.ViewExtension.hide
import com.vdotok.app.extensions.ViewExtension.show
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.call.CallActivity
import com.vdotok.app.feature.chat.ChatActivity
import com.vdotok.app.feature.chat.adapter.ChatListAdapter
import com.vdotok.app.feature.chat.chatUtils.ChatFileUtils
import com.vdotok.app.feature.chat.clickListenerInterface.FileClickListener
import com.vdotok.app.feature.chat.dialog.ChatAttachmentDialog
import com.vdotok.app.feature.chat.dialog.StoragePermissionDialog
import com.vdotok.app.feature.chat.enums.FileSelectionEnum
import com.vdotok.app.feature.chat.enums.MimeTypeEnum
import com.vdotok.app.feature.chat.viewmodel.ChatViewModel
import com.vdotok.app.feature.dashboard.dialog.BroadcastOptionsFragment
import com.vdotok.app.utils.*
import com.vdotok.app.utils.Utils.setCallTitleCustomObject
import com.vdotok.app.utils.ValidationUtils.afterTextChanged
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.connect.models.*
import com.vdotok.connect.utils.ImageUtils
import com.vdotok.connect.utils.ImageUtils.copyFileToInternalStorage
import com.vdotok.connect.utils.ImageUtils.encodeToBase64
import com.vdotok.network.models.GroupModel
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class ChatFragment : BaseFragment<FragmentChatBinding, ChatViewModel>(), FileClickListener {

    override val getLayoutRes: Int = R.layout.fragment_chat
    override val getViewModel: Class<ChatViewModel> = ChatViewModel::class.java

    private var timer: CountDownTimer? = null
    private var broadcastOptionsFragment: BroadcastOptionsFragment? = null
    private var storagePermissionDialog: StoragePermissionDialog? = null

    private lateinit var adapter: ChatListAdapter
    private lateinit var chatUtils: ChatFileUtils

    var file: File? = null
    private var fileType = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)
        getArgumentsData()
        setBindingData()
        setupUI()
        initChatListAdapter()
        setUpButtonClick()
        getChatData()

        return mView
    }

    private fun getChatData() {
        CoroutineScope(IO).launch {
            adapter.updateData(ArrayList(viewModel.getChatData(viewModel.groupModel.id)))
            scrollToLast()
        }
    }

    /**
     * method to bind data variables with xml
     */
    private fun setBindingData() {
        binding.groupTitle = viewModel.groupTitle
        binding.showTypingText = viewModel.showTypingText
        binding.typingUserName = viewModel.typingText
        binding.messageText = viewModel.messageText
        binding.disableButton = viewModel.disableButton
        binding.isActiveSession = viewModel.appManager.isTimerRunning
        viewModel.messageText.set("")
    }

    /**
     * method to get Data from other activity
     */
    private fun getArgumentsData() {
        chatUtils = context?.let { ChatFileUtils(it, viewModel.appManager) }!!
        arguments?.apply {
            getParcelable<GroupModel>(ChatActivity.GROUP_MODEL)?.let {
                viewModel.groupModel = it
            }
        }
    }

    /**
     * method for clickListeners
     */
    private fun setUpButtonClick() {
        binding.customBottombar.optionMenu.performSingleClick {
            PermissionUtils.getAttachmentsPermission(
                requireContext(),
                {

                    ChatAttachmentDialog(
                        this::selectAttachment,
                        this::openMapAndContact,
                        this::selectDocAttachment
                    ).show(
                        childFragmentManager,
                        ChatAttachmentDialog.TAG
                    )

                }, {},
                this::showReadWritePermissionsRequiredDialog
            )

        }
        binding.customToolbar.videoCall.performSingleClick {

            PermissionUtils.getVideoCallPermissions(
                requireContext(),
                {
                    dialCall(com.vdotok.streaming.enums.MediaType.VIDEO, getRefIDs(),viewModel.groupModel.autoCreated)
                }, {},
                {
                    showVideoCallPermissionsRequiredDialog(false)
                }
            )
        }
        binding.customToolbar.audioCall.performSingleClick {

            PermissionUtils.getAudioCallPermission(
                requireContext(),
                {
                    dialCall(
                        com.vdotok.streaming.enums.MediaType.AUDIO,
                        getRefIDs(),
                        viewModel.groupModel.autoCreated
                    )
                }, {},
                this::showAudioPermissionsRequiredDialog
            )
        }

        binding.customBottombar.sendButton.performSingleClick {
            if (viewModel.messageText.get().toString().trim().isEmpty()) {
                viewModel.disableButton.set(true)
            } else {
                viewModel.disableButton.set(false)
                sendTextMessage(viewModel.messageText.get().toString().trim())
            }
        }

        binding.customToolbar.arrowBack.performSingleClick {
            activity?.onBackPressed()
        }

        binding.customBottombar.galleryImage.performSingleClick {
            PermissionUtils.getAttachmentsPermission(
                requireContext(),
                {
                    selectAttachment(FileSelectionEnum.IMAGE)
                }, {},
                this::showReadWritePermissionsRequiredDialog
            )
        }

        binding.customToolbar.groupBroadcast.performSingleClick{
            broadcastOptionsFragment =
                BroadcastOptionsFragment(object : BroadcastOptionsFragment.OnOptionSelection {
                    override fun selectedOptions(
                        isAppAudioEnabled: Boolean,
                        isCameraEnable: Boolean,
                        isSSEnable: Boolean
                    ) {
                        viewModel.appManager.isAppAudioEnableInMultiCast = isAppAudioEnabled
                        viewModel.appManager.isCamEnableInMultiCast = isCameraEnable
                        viewModel.appManager.isSSEnableInMultiCast = isSSEnable
                        if (isSSEnable) {
                            val captureIntent =
                                activity?.let { it1 -> viewModel.startScreenCapture(it1) }
                            startForProjection.launch(captureIntent)
                        } else
                            viewModel.startPublicBroadCast(
                                null, activity, true, getRefIDs(),
                                viewModel.groupModel.groupTitle.toString(),viewModel.groupModel.autoCreated
                            )

                    }

                }, isGroupBroadcast = true)
            broadcastOptionsFragment?.show(childFragmentManager, BroadcastOptionsFragment.TAG)

        }
    }

    private fun getRefIDs(): ArrayList<String> {
        val refIdList = ArrayList<String>()
        viewModel.groupModel.participants.forEach { participant ->
            if (participant.refID != viewModel.getOwnRefID())
                participant.refID?.let { refIdList.add(it) }
        }
        return refIdList
    }

    private var startForProjection: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                activity?.let {
                    viewModel.appManager.getCallClient()
                        ?.initSession(
                            activityResult.data,
                            activityResult.resultCode,
                            it,
                            viewModel.appManager.isAppAudioEnableInMultiCast
                        )
                }
            }
        }

    /**
     * method to dial call by pressing icon
     */
    private fun dialCall(
        mediaType: com.vdotok.streaming.enums.MediaType,
        refIds: ArrayList<String>,
        autoCreated: Int?
    ) {
        val callParams = CallParams(
            refId = viewModel.getOwnRefID(),
            toRefIds = refIds,
            isInitiator = false,
            isBroadcast = 0,
            mediaType = mediaType,
            callType = CallType.MANY_TO_MANY,
            sessionType = SessionType.CALL,
            customDataPacket = setCallTitleCustomObject(null,viewModel.groupModel.groupTitle,autoCreated.toString())
        )
        val session = viewModel.appManager.getCallClient()?.dialMany2ManyCall(callParams)
        session?.let { it1 ->
            callParams.sessionUUID = it1
            callParams.customDataPacket =
                setCallTitleCustomObject(null,viewModel.groupModel.groupTitle.toString(),autoCreated.toString())
            viewModel.appManager.setSession(SessionType.CALL, callParams)

        }
        var participantsId :String? = null
        if (refIds.size > 1){
            participantsId = null
        }else{
            refIds.forEach {
                participantsId = it
            }
        }
        participantsId?.let {
            viewModel.insertCallHistory(
                callParams,
                it,
                viewModel.resourcesProvider.getString(R.string.status_outgoing_call),
                false,
                ""
            )
        }
        startActivity(CallActivity.createCallActivity(requireContext()))
    }

    /**
     * method to set ui
     */
    private fun setupUI() {
        viewModel.getGroupName()
        binding.progressBar.hide()
        binding.customBottombar.edtMessage.afterTextChanged {
            if (viewModel.messageText.get().toString().trim().isEmpty()) {
                viewModel.disableButton.set(true)
            } else {
                viewModel.disableButton.set(false)
                handleAfterTextChange(viewModel.messageText.get().toString())
            }
        }
    }

    /**
     * method to set the chat List adapter
     */
    private fun initChatListAdapter() {
        activity?.applicationContext?.let {
            adapter = ChatListAdapter(this, ArrayList(), viewModel)
        }
        adapter.setHasStableIds(true)
        binding.rcvMsgList.adapter = adapter
        scrollToLast()
    }

    /**
     * method to send file to other users of type image,audio,video and doc
     */
    override fun onFileSend(fileHeaderId: String, fileType: Int) {
        activity?.runOnUiThread {
            adapter.sendStatus = true
            if (fileType == MediaType.IMAGE.value) {
                adapter.addItem(
                    chatUtils.makeImageItemModel(
                        file,
                        viewModel.getDummyHeader(fileType),
                        viewModel.groupModel,
                        fileHeaderId
                    )!!
                )
                viewModel.appManager.updateMessageMapData(
                    chatUtils.makeImageItemModel(
                        file,
                        viewModel.getDummyHeader(fileType),
                        viewModel.groupModel,
                        fileHeaderId
                    )
                    !!
                )
            } else {
                adapter.addItem(
                    chatUtils.makeFileItemModel(
                        file,
                        viewModel.getDummyHeader(fileType),
                        viewModel.groupModel,
                        fileHeaderId
                    )!!
                )
                viewModel.appManager.updateMessageMapData(
                    chatUtils.makeFileItemModel(
                        file,
                        viewModel.getDummyHeader(fileType),
                        viewModel.groupModel,
                        fileHeaderId
                    )!!
                )
            }
            scrollToLast()
        }

    }

    /**
     * method which would be called when file is receive from other user
     */
    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {
        super.onFileReceivedCompleted(headerModel, byteArray, msgId)
        checkForStoragePermissions(msgId, headerModel, byteArray)
    }

    private fun checkForStoragePermissions(
        msgId: String,
        headerModel: HeaderModel,
        byteArray: ByteArray
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionDialog =
                StoragePermissionDialog(
                    { this.saveFile(msgId, headerModel, byteArray) },
                    { this.sendTextMessage(getString(R.string.msg_storage_permission_denied)) },
                )
            activity?.supportFragmentManager?.let { it1 ->
                storagePermissionDialog?.show(it1, StoragePermissionDialog.TAG)
            }
        } else {
            saveFile(msgId, headerModel, byteArray)
        }

    }

    private fun saveFile(msgId: String, headerModel: HeaderModel, byteArray: ByteArray) {
        chatUtils.checkAndroidVersionToSave(headerModel, byteArray) {
            chatUtils.sendAttachmentMessage(
                this::onNewMessage,
                headerModel,
                chatUtils.file,
                msgId,
                viewModel.groupModel
            )
        }
    }

    /**
     * method to get the status whether the message is seen or not and to get its count
     */
    override fun onReceiptReceive(model: ReadReceiptModel) {
        super.onReceiptReceive(model)
        activity?.runOnUiThread {
            if ((model.from != viewModel.getUserData().refId)) {
                adapter.updateMessageForReceipt(model)
                viewModel.updateChatStatus(model.messageId, model.receiptType)
            }
        }
    }

    /**
     * method to get the byte data from other user
     */
    override fun onByteReceived(payload: ByteArray) {
        super.onByteReceived(payload)
        payload.let {
            val model = viewModel.getUserData().refId?.let { it1 ->
                Message(
                    System.currentTimeMillis().toString(),
                    viewModel.groupModel.channelName,
                    viewModel.groupModel.channelKey,
                    it1,
                    MessageType.media,
                    encodeToBase64(it),
                    0f,
                    isGroupMessage = viewModel.groupModel.participants.size > 1
                )
            }
            if (model != null) {
                adapter.addItem(model)
            }
        }
    }

    /**
     * method is called when receiving of file got interrupted / failed due to some issue
     */
    override fun onFileReceiveFailed() {
        super.onFileReceiveFailed()
        binding.root.showSnackBar(getString(R.string.attachment_failed))
    }

    /**
     * method is called when any message is arrived
     */
    override fun onMessageArrive(message: Message) {
        super.onMessageArrive(message)
        viewModel.appManager.lastMessageGroupKey = message.key
        if (message.from != viewModel.getOwnRefID()) {
            message.to.let {
                viewModel.appManager.mapUnreadCount[it] =
                    viewModel.appManager.mapUnreadCount[it]?.plus(1) ?: 1
            }
        }
        onNewMessage(message)
        viewModel.appManager.updateMessageMapData(message)
    }

    /**
     * method is called when user is typing message
     */
    override fun onTypingMessage(message: Message) {
        super.onTypingMessage(message)
        if (message.key == viewModel.groupModel.channelKey && (message.from == viewModel.getUserData().refId).not()) {
            showOnTypingMessage(message)
        }
    }

    /**
     * method is called when new message is received
     */
    private fun onNewMessage(message: Message) {
        activity?.runOnUiThread {
            if (message.key == viewModel.groupModel.channelKey) {
                viewModel.appManager.mapUnreadCount.clear()
                viewModel.usersList.clear()
                binding.progressBar.hide()
                adapter.addItem(message)
                scrollToLast()
                viewModel.sendAcknowledgeMsgToGroup(message)
                //        insert into DB
                insertDBData(message)
            } else {
                viewModel.appManager.messageUpdateLiveData.postValue(message)
            }
        }
    }

    private fun insertDBData(message: Message) {
        chatUtils.file?.let { file ->
            viewModel.insertChatModel(message, viewModel.groupModel.id, file.absolutePath)
            chatUtils.file = null
        }?: kotlin.run {
            viewModel.insertChatModel(message, viewModel.groupModel.id, "")
        }
    }

    /**
     * method to get the progress of the attachment which is to sent to other user
     */
    override fun onAttachmentProgressSend(fileHeaderId: String, progress: Int) {
        super.onAttachmentProgressSend(fileHeaderId, progress)
        if (progress == 100) {
            activity?.runOnUiThread {
                val itemObject = adapter.items.find { it.id == fileHeaderId }
                val index = adapter.items.indexOf(itemObject)
                adapter.sendStatus = false
                adapter.notifyItemChanged(index)
            }
        }
    }


    /**
     * this method is used to get the text change status in a respective field
     */
    private fun handleAfterTextChange(text: String) {
        if (text.isNotEmpty()) {
            sendTypingMessage(viewModel.getUserData().refId.toString(), true)
            timer?.cancel()
            timer = object : CountDownTimer(1500, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    sendTypingMessage(viewModel.getUserData().refId.toString(), false)
                }
            }.start()
        }
    }


    /**
     * this method is used to show the typing status of the user/s other then the  logged in user
     */
    private fun showOnTypingMessage(message: Message) {
        if (message.content == TYPING_START) {
            message.from = viewModel.getUserName(message)
            viewModel.showTypingText.set(true)
            viewModel.typingText.set(viewModel.getNameOfUsers(message))
            hideTypingText()
        }
    }

    /**
     * this method is used to hide typing status through binding after interval of 2 sec
     */
    private fun hideTypingText() {
        Timer().schedule(timerTask {
            activity?.runOnUiThread {
                viewModel.showTypingText.set(false)
            }
        }, 2000)
    }


    /**
     * this method is to send the typing status to the sdk
     */
    private fun sendTypingMessage(refId: String, isTyping: Boolean) {
        viewModel.groupModel.let {
            val content = if (isTyping) TYPING_START else TYPING_STOP
            val chatModel = Message(
                System.currentTimeMillis().toString(),
                it.channelName,
                it.channelKey,
                refId,
                MessageType.typing,
                content,
                0f,
                isGroupMessage = viewModel.groupModel.participants.size > 1
            )
            viewModel.appManager.getChatClient()
                ?.sendTypingMessage(chatModel, chatModel.key, chatModel.to)
        }
    }

    /**
     * method to send messages to other user through sdk
     */
    private fun sendTextMessage(message: String) {
        binding.progressBar.show()
        viewModel.groupModel.let {
            val chatModel = viewModel.getUserData().refId?.let { it1 ->
                Message(
                    System.currentTimeMillis().toString(),
                    it.channelName,
                    it.channelKey,
                    it1,
                    MessageType.text,
                    message.trim(),
                    0f,
                    isGroupMessage = viewModel.groupModel.participants.size > 1,
                    ReceiptType.SENT.value
                )
            }
            if (chatModel != null) {
                viewModel.appManager.getChatClient()?.publishMessage(chatModel)
            }
            viewModel.messageText.set("")
        }
    }

    /**
     * this is used to get Result respective of which type of attachment is selected i-e video,audio,and image from gallery
     */
    private var startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val data: Intent? = activityResult.data
                handleIntentData(data)
            }
        }

    /**
     * this method is used to send result respective of type of attachment
     */
    private fun selectAttachment(fileSelectionEnum: FileSelectionEnum) {
        chatUtils.file = null
        val uri: Uri? = when (fileSelectionEnum) {
            FileSelectionEnum.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            FileSelectionEnum.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            FileSelectionEnum.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> null
        }
        uri?.let {
            val intent = Intent(Intent.ACTION_GET_CONTENT, it)

            intent.type = fileSelectionEnum.value
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startForResult.launch(intent)
        }
    }

    /**
     * this method is used to send result respective of type of attachment
     */
    private fun selectDocAttachment(fileSelectionEnum: FileSelectionEnum) {
        chatUtils.file = null
        when (fileSelectionEnum) {
            FileSelectionEnum.DOC -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = fileSelectionEnum.value
                val mimeTypes = docMimeType
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startForResultDocument.launch(intent)
            }
            else -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startForResultCamera.launch(intent)
            }
        }
    }

    /**
     * this is used to get Result respective of which type of attachment is selected i-e document
     */
    private var startForResultDocument: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val data: Intent? = activityResult.data
                handleIntentData(data)
            }
        }

    /**
     * this is used to get Result respective of which type of attachment image from camera
     */
    private var startForResultCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val data: Intent? = activityResult.data
                handleIntentDataCamera(data)
            }
        }

    /**
     * this method is used to handle data according to type gathered from registerActivityResult
     */
    private var selectedFileCamera: File? = null
    private fun handleIntentDataCamera(data: Intent?) {
        if (data == null) {
            binding.progressBar.hide()
        } else {
            binding.progressBar.show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                val byteArray = ImageUtils.convertBitmapToByteArray(
                    data?.extras?.get("data") as Bitmap
                )

                chatUtils.saveImage(
                    byteArray!!,
                    "${System.currentTimeMillis()}",
                    "image/jpeg",
                    "${Environment.DIRECTORY_PICTURES}/$directoryName",
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {}
            }
        } else {
            chatUtils.file = getFileData(
                activity as Context,
                chatUtils.getImageUri(
                    activity?.applicationContext!!,
                    data?.extras?.get("data") as Bitmap
                ),
                MediaType.IMAGE
            )

        }
        selectedFileCamera = file
        fileType = 0
        viewModel.appManager.getChatClient()?.sendFileToGroup(
            viewModel.groupModel.channelKey,
            viewModel.groupModel.channelName,
            chatUtils.file,
            fileType
        )
    }

    /**
     * this method is used to handle data according to type gathered from registerActivityResult
     */
    private var selectedFile: File? = null
    private fun handleIntentData(data: Intent?) {
        if (data == null) {
            binding.progressBar.hide()
        } else {
            binding.progressBar.show()
        }
        data?.data?.let { uri ->
            when (val mimeType = getMimeType(requireContext(), uri)) {
                MimeTypeEnum.IMAGE.value -> {
                    val pathDirectory = Environment.DIRECTORY_PICTURES
                    val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val mediaType = MediaType.IMAGE
                    fileType = 0
                    handleDataFromSelection(
                        data,
                        mimeType,
                        pathDirectory,
                        mediaUri,
                        mediaType,
                        fileType
                    )
                }
                MimeTypeEnum.VIDEO.value -> {
                    val pathDirectory = Environment.DIRECTORY_MOVIES
                    val mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    val mediaType = MediaType.VIDEO
                    fileType = 2
                    handleDataFromSelection(
                        data,
                        mimeType,
                        pathDirectory,
                        mediaUri,
                        mediaType,
                        fileType
                    )
                }
                MimeTypeEnum.AUDIO.value -> {
                    val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val pathDirectory = Environment.DIRECTORY_MUSIC
                    val mediaType = MediaType.AUDIO
                    fileType = 1
                    handleDataFromSelection(
                        data,
                        mimeType,
                        pathDirectory,
                        mediaUri,
                        mediaType,
                        fileType
                    )
                }
                MimeTypeEnum.DOC.value -> {
                    val mediaUri = MediaStore.Files.getContentUri("external")
                    val pathDirectory = Environment.DIRECTORY_DOCUMENTS
                    val mediaType = MediaType.FILE
                    fileType = 3
                    handleDataFromSelection(
                        data,
                        mimeType,
                        pathDirectory,
                        mediaUri,
                        mediaType,
                        fileType
                    )
                }
            }
            viewModel.appManager.getChatClient()?.sendFileToGroup(
                viewModel.groupModel.channelKey,
                viewModel.groupModel.channelName,
                chatUtils.file,
                fileType
            )
        }
    }

    /**
     * this method is used to handle the selected attachment and save it in storage
     */
    private fun handleDataFromSelection(
        data: Intent?, mimeType: String, pathDirectory: String, mediaUri: Uri,
        mediaType: MediaType, fileType: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                val byteArray = when (mimeType) {
                    MimeTypeEnum.IMAGE.value -> {
                        ImageUtils.convertImageToByte(context, Uri.parse(data?.data.toString()))
                    }
                    MimeTypeEnum.VIDEO.value -> {
                        val videoPath =
                            data?.data?.let { copyFileToInternalStorage(context, it, "video") }
                        converFileToByteArray(videoPath)
                    }
                    MimeTypeEnum.AUDIO.value -> {
                        val audioPath =
                            data?.data?.let { copyFileToInternalStorage(context, it, "audio") }
                        converFileToByteArray(audioPath)
                    }
                    MimeTypeEnum.DOC.value -> {
                        val filePath =
                            data?.data?.let { copyFileToInternalStorage(context, it, "document") }
                        converFileToByteArray(filePath)
                    }
                    else -> null
                }
                chatUtils.saveImage(
                    byteArray!!,
                    "${System.currentTimeMillis()}",
                    mimeType,
                    "${pathDirectory}/$directoryName",
                    mediaUri
                ) {}
            }
        } else {
            chatUtils.file = getFileData(activity as Context, data?.data, mediaType)
        }
        selectedFile = file
        this.fileType = fileType

    }

    /**
     * this method is used to show snackbar when location and contact is selected form menu
     */
    private fun openMapAndContact() {
        binding.root.showSnackBar(resources.getString(R.string.inProgress))
    }

    /**
     * Function to scroll recyclerview to last index
     * */
    private fun scrollToLast() {
        if (adapter.itemCount > 0)
            binding.rcvMsgList.smoothScrollToPosition(adapter.itemCount)
    }

    /**
     * method to open the attachment / message when clicked on it
     */
    override fun onFileClick() {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            intent.setDataAndType(
                Uri.parse(
                    Environment.getDataDirectory().absolutePath.toString()
                            + File.separator + "cPass" + File.separator
                ), "*/*"
            )
            context?.startActivity(Intent.createChooser(intent, "Complete action using"))
        } else {

            intent.setDataAndType(
                Uri.parse(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
                            + File.separator + "cPass" + File.separator
                ), "*/*"
            )
            context?.startActivity(Intent.createChooser(intent, "Open folder"))
        }
    }

    companion object {
        const val TYPING_START = "1"
        const val TYPING_STOP = "0"
    }

    override fun onSSSessionReady(
        mediaProjection: MediaProjection?
    ) {
        super.onSSSessionReady(mediaProjection)
        viewModel.startPublicBroadCast(
            mediaProjection, activity, true, getRefIDs(),
            viewModel.groupModel.groupTitle.toString(),
            viewModel.groupModel.autoCreated
        )
    }


}
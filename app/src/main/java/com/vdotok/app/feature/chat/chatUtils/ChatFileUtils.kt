package com.vdotok.app.feature.chat.chatUtils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.vdotok.app.constants.directoryName
import com.vdotok.app.manager.AppManager
import com.vdotok.app.utils.createAppDirectory
import com.vdotok.app.utils.getBitmap
import com.vdotok.app.utils.saveFileDataOnExternalData
import com.vdotok.connect.models.*
import com.vdotok.connect.utils.ImageUtils
import com.vdotok.network.models.GroupModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ChatFileUtils(var context: Context, var appManager: AppManager) {
    var file: File? = null
    var message: Message? = null

    fun checkAndroidVersionToSave(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        saveComplete: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var mediaUri: Uri? = null
            var pathDirectory: String? = null
            var mimeType: String? = null
            when (headerModel.type) {
                MediaType.IMAGE.value -> {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    pathDirectory = Environment.DIRECTORY_PICTURES
                    mimeType = "image/jpeg"
                }
                MediaType.VIDEO.value -> {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    pathDirectory = Environment.DIRECTORY_MOVIES
                    mimeType = "video/mp4"
                }
                MediaType.AUDIO.value -> {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    pathDirectory = Environment.DIRECTORY_MUSIC
                    mimeType = "audio/x-wav"
                }
                MediaType.FILE.value -> {
                    mediaUri = MediaStore.Files.getContentUri("external")
                    pathDirectory = Environment.DIRECTORY_DOCUMENTS
                    mimeType = "application/pdf"
                }
            }
            if (mimeType != null && mediaUri != null) {
                saveImage(
                    byteArray,
                    "${System.currentTimeMillis()}",
                    mimeType,
                    "${pathDirectory}/$directoryName",
                    mediaUri
                )
                { saveComplete.invoke() }
            }

        } else {
            val fileName =
                "file_".plus(System.currentTimeMillis()).plus(".")
                    .plus(headerModel.fileExtension)
            val filePath = createAppDirectory(headerModel.type) + "/$fileName"
            file = saveFileDataOnExternalData(filePath, byteArray)
            saveComplete.invoke()

        }

    }

    fun saveImage(
        bytes: ByteArray,
        displayName: String,
        mimeType: String,
        path: String,
        contentUri: Uri,
        saveComplete: () -> Unit
    ) {

        val resol = context.applicationContext?.contentResolver
        val contentValu = ContentValues()
        contentValu.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValu.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValu.put(MediaStore.MediaColumns.RELATIVE_PATH, path)

        val imageurl = resol?.insert(contentUri, contentValu)

        val parcelFileDescriptor =
            context.applicationContext?.contentResolver?.openFileDescriptor(imageurl!!, "w", null)

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(bytes)
        fileOutputStream.close()
        imageurl?.let { uri ->
            context.applicationContext?.let { context ->
                file = File(ImageUtils.copyFileToInternalStorage(context, uri, directoryName)!!)
                contentValu.clear()
                context.applicationContext?.contentResolver?.update(uri, contentValu, null, null)
                saveComplete.invoke()

            }
        }
    }

    fun sendAttachmentMessage(
        onNewMessage: (message: Message) -> Unit,
        headerModel: HeaderModel,
        files: File?,
        msgId: String,
        groupModel: GroupModel
    ) {
        if (groupModel.channelName == headerModel.topic) {
            groupModel.participants!!.size > 1
            when (headerModel.type) {
                MediaType.IMAGE.value -> {
                    message = makeImageItemModel(files, headerModel, groupModel, msgId)!!
                    message?.let { message ->
                        appManager.updateMessageMapData(message)
                        getMessageCount(message)
                        onNewMessage.invoke(message)
                    }

                }
                MediaType.AUDIO.value -> {
                    message = makeFileItemModel(files, headerModel, groupModel, msgId)!!
                    message?.let { message ->
                        appManager.updateMessageMapData(message)
                        getMessageCount(message)
                        onNewMessage.invoke(message)
                    }

                }
                MediaType.VIDEO.value -> {
                    message = makeFileItemModel(files, headerModel, groupModel, msgId)!!
                    message?.let { message ->
                        appManager.updateMessageMapData(message)
                        getMessageCount(message)
                        onNewMessage.invoke(message)
                    }
                }

                MediaType.FILE.value -> {
                    message = makeFileItemModel(files, headerModel, groupModel, msgId)!!
                    message?.let { message ->
                        appManager.updateMessageMapData(message)
                        getMessageCount(message)
                        onNewMessage.invoke(message)
                    }
                }

                else -> {
                }
            }
        }
    }

    fun makeImageItemModel(
        file: File?,
        headerModel: HeaderModel,
        groupModel: GroupModel,
        msgId: String
    ): Message? {
        val bitmap = file?.let { getBitmap(it, 500, 500) }
        return bitmap?.let {
            ImageUtils.encodeToBase64(it)?.let { base64String ->
                Message(
                    msgId,
                    groupModel.channelName,
                    groupModel.channelKey,
                    headerModel.from,
                    MessageType.media,
                    base64String,
                    0f,
                    isGroupMessage = groupModel.participants!!.size > 1,
                    ReceiptType.SENT.value,
                    headerModel.type
                )
            }
        }
    }

    fun makeFileItemModel(
        file: File?,
        headerModel: HeaderModel,
        groupModel: GroupModel,
        msgId: String
    ): Message? {
        return file?.toUri()?.let {
            Message(
                msgId,
                groupModel.channelName,
                groupModel.channelKey,
                headerModel.from,
                MessageType.media,
                it.toString(),
                0f,
                isGroupMessage = groupModel.participants!!.size > 1,
                ReceiptType.SENT.value,
                headerModel.type
            )
        }
    }

    private fun getMessageCount(message: Message) {
        message.to.let {
            appManager.mapUnreadCount[it] = appManager.mapUnreadCount[it]?.plus(1) ?: 1
        }
    }

    /**
     * this method is used to get the image uri path
     */
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
}

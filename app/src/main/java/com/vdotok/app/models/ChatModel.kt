package com.vdotok.app.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.vdotok.connect.models.MediaType
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.MessageType
import com.vdotok.connect.models.ReceiptType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ChatData")
data class ChatModel (

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "messageId")
    @SerializedName("id")
    var id: String,

    @ColumnInfo(name = "groupId")
    @SerializedName("groupId")
    var groupId: Int,

    @ColumnInfo(name = "messageTo")
    @SerializedName("to")
    var to: String,

    @ColumnInfo(name = "messageKey")
    @SerializedName("key")
    var key: String,

    @ColumnInfo(name = "messageFrom")
    @SerializedName("from")
    var from: String,

    @ColumnInfo(name = "messageType")
    @SerializedName("type")
    var type: MessageType?,

    @ColumnInfo(name = "content")
    @SerializedName("content")
    var content: String,

    @ColumnInfo(name = "size")
    @SerializedName("size")
    var size: Float,

    @ColumnInfo(name = "isGroupMessage")
    @SerializedName("isGroupMessage")
    var isGroupMessage: Boolean,

    @ColumnInfo(name = "status")
    @SerializedName("status")
    var status: Int = ReceiptType.SENT.value,

    @ColumnInfo(name = "subType")
    @SerializedName("subType")
    var subType: Int = MediaType.IMAGE.value,

    @ColumnInfo(name = "progress")
    @SerializedName("progress")
    var progress: Double = 0.0,

    @ColumnInfo(name = "readCount")
    @SerializedName("readCount")
    var readCount: Int = 0,

    @ColumnInfo(name = "fileUri")
    @SerializedName("fileUri")
    var fileUri: String = "",

    @ColumnInfo(name = "createdAt")
    @SerializedName("date")
    var date: Long = 0
) : Parcelable {

    companion object {
        fun Message.toChatModel(groupId: Int, fileUri: String) = ChatModel(
            id = this.id,
            groupId = groupId,
            to = this.to,
            key = this.key,
            from = this.from,
            type = this.type,
            content = this.content,
            size = this.size,
            isGroupMessage = this.isGroupMessage,
            status = this.status,
            subType = this.subType,
            fileUri = fileUri,
            date = this.date
        )

        fun ChatModel.toMessageModel() = Message(
            id = this.id,
            to = this.to,
            key = this.key,
            from = this.from,
            type = this.type,
            content = this.content,
            size = this.size,
            isGroupMessage = this.isGroupMessage,
            status = this.status,
            subType = this.subType,
            date = this.date
        )
    }

}

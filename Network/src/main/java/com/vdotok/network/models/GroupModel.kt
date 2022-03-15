package com.vdotok.network.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model class for mapping group information
 */
@Parcelize
@Entity(tableName="Groups")
data class GroupModel (

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "groupId")
    @SerializedName("id")
    var id: Int = 0,

    @ColumnInfo(name = "channelName")
    @SerializedName("channel_name")
    var channelName: String = "",

    @ColumnInfo(name = "adminId")
    @SerializedName("admin_id")
    var adminId: Int? = null,

    @ColumnInfo(name = "groupTitle")
    @SerializedName("group_title")
    var groupTitle: String? = null,

    @ColumnInfo(name = "participants")
    @SerializedName("participants")
    var participants: ArrayList<Participants> = ArrayList(),

    @ColumnInfo(name = "autoCreated")
    @SerializedName("auto_created")
    var autoCreated: Int? = null,

    @ColumnInfo(name = "channelKey")
    @SerializedName("channel_key")
    var channelKey: String = "",

    @ColumnInfo(name = "createdDateTime")
    @SerializedName("created_datetime")
    var createdDateTime: String = "",

    @Ignore
    var isUnreadMessage: Boolean = false

): Parcelable{
    companion object{
        const val TAG = "GROUP_MODEL"
    }
}
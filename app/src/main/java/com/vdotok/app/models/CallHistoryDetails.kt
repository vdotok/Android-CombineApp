package com.vdotok.app.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import kotlinx.parcelize.Parcelize

/**
 * Created By: VdoTok
 * Date & Time: On 07/12/2021 At 1:28 PM in 2021
 */

@Parcelize
@Entity(tableName="CallHistory")
data class CallHistoryDetails(

    @ColumnInfo(name = "sessionId")
    @SerializedName("sessionId")
    var sessionId: String? = null,

    @ColumnInfo(name = "fullName")
    @SerializedName("fullName")
    var fullName: String? = null,

    @ColumnInfo(name = "participantRefId")
    @SerializedName("participantRefId")
    var participantRefId: String? = null,

    @ColumnInfo(name = "callStatus")
    @SerializedName("callStatus")
    var callStatus: String? = null,

    @ColumnInfo(name = "time")
    @SerializedName("time")
    var time: Long? = null,

    @ColumnInfo(name = "associateId")
    @SerializedName("associateId")
    var associateId: String? = null,

    @ColumnInfo(name = "mediaType")
    @SerializedName("mediaType")
    var mediaType: MediaType,

    @ColumnInfo(name = "sessionType")
    @SerializedName("sessionType")
    var sessionType: SessionType,
    @ColumnInfo(name = "groupAutoCreatedValue")
    @SerializedName("groupAutoCreatedValue")
    val groupAutoCreatedValue: String?
) : Parcelable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    var id: Int = 0
}

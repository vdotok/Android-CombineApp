package com.vdotok.network.models

import android.os.Parcelable
import android.text.TextUtils
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
 * Request model map class to send that a user is selected to form a group
 */
@Parcelize
@Entity(tableName="Users")
data class UserModel (

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "userId")
    @SerializedName("user_id")
    var id: String = "",

    @ColumnInfo(name = "fullName")
    @SerializedName("full_name")
    var fullName: String? = null,

    @ColumnInfo(name = "userEmail")
    @SerializedName("email")
    var email: String? = null,

    @ColumnInfo(name = "refId")
    @SerializedName("ref_id")
    var refID: String? = null,

    @ColumnInfo(name = "profilePic")
    @SerializedName("profile_pic")
    var profilePic: String? = null,

    @Ignore
    var isSelected: Boolean = false

) : Parcelable{

    val userName: String?
        get() {
            return if (!TextUtils.isEmpty(fullName)) {
                fullName
            } else
                email
        }

}

@Parcelize
data class CallerData (
    var calleName: String? = null
): Parcelable

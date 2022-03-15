package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthenticationRequest(
    @SerializedName("auth_token")
    var apiKey: String,

    @SerializedName("project_id")
    var projectId: String
): Parcelable
package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Norgic
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class getting the response after user has successfully logged in
 */
class ProfileImageResponse {

    @SerializedName("status")
    var status: String? = null

    @SerializedName("data")
    var data: String? = null

    @SerializedName("filename")
    var image: String? = null

    @SerializedName("message")
    var message: String? = null

}
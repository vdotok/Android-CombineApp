package com.vdotok.app.feature.chat.enums


/**
 * Created By: VdoTok
 * Date & Time: On 08/12/2021 At 5:29 PM in 2021
 */

enum class MimeTypeEnum(value: String) {

    IMAGE("image/jpeg"),
    AUDIO("audio/x-wav"),
    VIDEO("video/mp4"),
    DOC("application/pdf");

    var value: String
        internal set

    init {
        this.value = value
    }
}
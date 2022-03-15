package com.vdotok.app.feature.chat.enums


/**
 * Created By: VdoTok
 * Date & Time: On 08/12/2021 At 5:29 PM in 2021
 */

enum class FileSelectionEnum(value: String) {

    IMAGE("image/*"),
    AUDIO("audio/*"),
    VIDEO("video/*"),
    DOC("*/*"),
    CAM("");

    var value: String
        internal set

    init {
        this.value = value
    }
}
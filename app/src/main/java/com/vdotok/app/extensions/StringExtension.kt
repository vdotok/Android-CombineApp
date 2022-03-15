package com.vdotok.app.extensions

import java.util.regex.Pattern


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:37 AM in 2021
 */
object StringExtension {

    fun String.containsNonAlphaNumeric() : Boolean {
        val p = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-¥¢£ø]")
        return p.matcher(this).find()
    }


    fun String.containsNonAlphaNumericName() : Boolean {
        val p = Pattern.compile("[!@#$%&*()+=|<>?{}\\[\\]~-¥¢£ø]")
        return p.matcher(this).find()
    }

}
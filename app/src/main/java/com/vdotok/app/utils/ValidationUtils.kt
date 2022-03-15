package com.vdotok.app.utils

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import com.vdotok.app.R
import com.vdotok.app.extensions.StringExtension.containsNonAlphaNumeric
import com.vdotok.app.extensions.StringExtension.containsNonAlphaNumericName
import com.vdotok.app.extensions.ViewExtension.showSnackBar


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:27 AM in 2021
 */
object ValidationUtils {

    fun EditText.checkEmail(email: String, showErrorMsg: Boolean = false): Boolean {
        return if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (showErrorMsg) this.showSnackBar(this.context.getString(R.string.invalid_email))
            false
        } else {
            true
        }
    }

    fun EditText.checkUserName(username: String, showErrorMsg: Boolean = false): Boolean {
        return if (username.containsNonAlphaNumericName() || username.length < 4 ||
            username.length > 20 || username.isEmpty() || TextUtils.isDigitsOnly(username)
        ) {
            if (showErrorMsg) this.showSnackBar(this.context.getString(R.string.invalid_username))
            false
        } else {
            true
        }
    }

    fun EditText.checkPassword(password: String, showErrorMsg: Boolean = false): Boolean {
        return if (password.containsNonAlphaNumeric() || password.length < 8 || password.isEmpty()) {
            if (showErrorMsg) this.showSnackBar(this.context.getString(R.string.invalid_password))
            false
        } else {
            true
        }
    }

    fun EditText.checkValidation(input: String): Boolean {
        return if (input.contains("@") && input.contains(".com")) {
            this.checkEmail(input)
        } else {
            this.checkUserName(input)
        }
    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

}
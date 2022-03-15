package com.vdotok.app.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 11:33 AM in 2021
 */
object ViewExtension {

    fun View.hide() {
        this.visibility = View.GONE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.invisible() {
        this.visibility = View.INVISIBLE
    }

    fun View.toggleVisibility() {
        if (this.visibility == View.VISIBLE) this.hide()
        else this.show()
    }

    fun View.showSnackBar(message: String?) {
        message?.let { Snackbar.make(this, it, Snackbar.LENGTH_LONG).show() }
    }

    fun Activity.hideKeyboard() {
        val focusedView: View? = this.currentFocus
        if (focusedView is EditText) {
            (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(focusedView.getWindowToken(), 0)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun timeCheck(milli: Long): String {
        val simpleDateFormat = SimpleDateFormat("hh:mm")
        return simpleDateFormat.format(milli)
    }


@SuppressLint("SimpleDateFormat")
fun currentTimeCalculation(milli: Long): CharSequence? {

    val smsTime = Calendar.getInstance()
    smsTime.timeInMillis = milli

    val now = Calendar.getInstance()

    val timeFormatString = "h:mm aa";
    return if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
        DateFormat.format(timeFormatString, smsTime).toString()
    } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
        "Yesterday "
    }  else {
        DateFormat.format("dd/MM/yyyy", smsTime)
    }
}
}




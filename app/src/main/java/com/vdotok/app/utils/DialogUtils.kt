package com.vdotok.app.utils

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.vdotok.app.R


/**
 * Created By: VdoTok
 * Date & Time: On 25/11/2021 At 1:51 PM in 2021
 */
object DialogUtils {
    fun showDeleteGroupAlert(
        activity: FragmentActivity?,
        dialogListener: DialogInterface.OnClickListener
    ) {
        activity?.let {
            val alertDialog = AlertDialog.Builder(it)
                .setMessage(activity.getString(R.string.delete_group_des))
                .setPositiveButton(R.string.delete_group, dialogListener)
                .setNegativeButton(R.string.cancel, null).create()
            alertDialog.show()
        }
    }

    fun showPermissionsDeniedAlert(
        activity: FragmentActivity?,
        message: String,
        buttonText: String,
        dialogListener: DialogInterface.OnClickListener
    ) {
        activity?.let {
            val alertDialog = AlertDialog.Builder(it)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(buttonText, dialogListener).create()
            alertDialog.show()
        }
    }
}
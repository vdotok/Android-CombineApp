package com.vdotok.app.utils

import android.Manifest
import android.content.Context
import com.markodevcic.peko.Peko
import com.markodevcic.peko.Peko.requestPermissionsAsync
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created By: VdoTok
 * Date & Time: On 03/12/2021 At 2:00 PM in 2021
 */
object PermissionUtils {

    fun getAudioCallPermission(
        context: Context, grantedAction: () -> Unit,
        deniedAction: () -> Unit, deniedPermanentlyAction: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = Peko.requestPermissionsAsync(context, Manifest.permission.RECORD_AUDIO)
            when (result) {
                is PermissionResult.Granted -> {
                    grantedAction.invoke()
                }
                is PermissionResult.Denied.JustDenied,
                is PermissionResult.Denied.NeedsRationale -> {
                    deniedAction.invoke()
                }
                is PermissionResult.Denied.DeniedPermanently -> {
                    deniedPermanentlyAction.invoke()
                }
                else -> {
                }
            }
        }
    }

    fun getVideoCallPermissions(
        context: Context, grantedAction: () -> Unit,
        deniedAction: () -> Unit, deniedPermanentlyAction: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = requestPermissionsAsync(context,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
            when (result) {
                is PermissionResult.Granted -> {
                    // we have both permissions
                    grantedAction.invoke()
                }
                is PermissionResult.Denied.JustDenied,
                is PermissionResult.Denied.NeedsRationale -> {
                    deniedAction.invoke()
                }
                is PermissionResult.Denied.DeniedPermanently -> {
                    deniedPermanentlyAction.invoke()
                }
                else -> {
                }
            }
        }
    }

    fun getAttachmentsPermission(
        context: Context, grantedAction: () -> Unit,
        deniedAction: () -> Unit, deniedPermanentlyAction: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = requestPermissionsAsync(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            when (result) {
                is PermissionResult.Granted -> {
                    // we have both permissions
                    grantedAction.invoke()
                }
                is PermissionResult.Denied.JustDenied,
                is PermissionResult.Denied.NeedsRationale -> {
                    deniedAction.invoke()
                }
                is PermissionResult.Denied.DeniedPermanently -> {
                    deniedPermanentlyAction.invoke()
                }
                else -> {
                }
            }
        }
    }

    fun getStoragePermission(
        context: Context, grantedAction: () -> Unit,
        deniedAction: () -> Unit, deniedPermanentlyAction: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = requestPermissionsAsync(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            when (result) {
                is PermissionResult.Granted -> {
                    // we have both permissions
                    grantedAction.invoke()
                }
                is PermissionResult.Denied.JustDenied,
                is PermissionResult.Denied.NeedsRationale -> {
                    deniedAction.invoke()
                }
                is PermissionResult.Denied.DeniedPermanently -> {
                    deniedPermanentlyAction.invoke()
                }
                else -> {
                }
            }
        }
    }

}
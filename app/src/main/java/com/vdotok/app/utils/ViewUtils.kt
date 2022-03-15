package com.vdotok.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import java.util.concurrent.TimeUnit


/**
 * Created By: VdoTok
 * Date & Time: On 16/11/2021 At 12:35 PM in 2021
 */
object ViewUtils {

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setStatusBarGradient(activity: Activity, toSetDrawable: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            val background = activity.resources.getDrawable(toSetDrawable, null)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(android.R.color.transparent, null)
            window.navigationBarColor =
                activity.resources.getColor(android.R.color.transparent, null)
            window.setBackgroundDrawable(background)
        }
    }

    fun View.performSingleClick(method: () -> Unit) {
        setOnClickListener {
            disableDoubleClick()
            method.invoke()
        }
    }

    private fun View.disableDoubleClick() {
        isClickable = false
        postDelayed({ isClickable = true }, TimeUnit.SECONDS.toMillis(2))
    }

    fun View.fadeIn() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = 5000

        val animation = AnimationSet(false)
        animation.addAnimation(fadeIn)
        this.animation = fadeIn
    }

    fun View.fadeOut() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.startOffset = 1000
        fadeOut.duration = 500

        val animation = AnimationSet(false)
        animation.addAnimation(fadeOut)
        this.animation = fadeOut
        this.visibility = View.GONE
    }

}
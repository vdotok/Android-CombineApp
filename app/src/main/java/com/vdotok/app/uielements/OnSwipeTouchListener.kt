package com.vdotok.app.uielements

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


/**
 * Created By: VdoTok
 * Date & Time: On 26/11/2021 At 3:04 PM in 2021
 */
open class OnSwipeTouchListener(context: Context?) : View.OnTouchListener {

    private val SWIPE_DISTANCE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100
    private val gestureDetector: GestureDetector
    fun onSwipeLeft() {}
    fun onSwipeRight() {}
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return false //super.onSingleTapConfirmed(e);
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}
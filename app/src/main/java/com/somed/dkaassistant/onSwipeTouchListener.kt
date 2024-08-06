package com.somed.dkaassistant

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs
internal open class OnSwipeTouchListener(c: Context?) :
    OnTouchListener {
    private val gestureDetector: GestureDetector
     var currentTouchY = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }
    private inner class GestureListener : SimpleOnGestureListener() {
        private val swipeTHRESHOLD: Int = 100
        private val swipeVELOCITYTHRESHOLD: Int = 100
        override fun onDown(e: MotionEvent): Boolean {
            onTouchDown()
            return false
        }

        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return false
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return false
        }
        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                currentTouchY=e2.y
                val diffY = e2.y - e1!!.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > swipeTHRESHOLD && abs(
                            velocityX
                        ) > swipeVELOCITYTHRESHOLD
                    ) {
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        else {
                            onSwipeLeft()
                        }
                    }
                }
                else {
                    if (abs(diffY) > swipeTHRESHOLD && abs(
                            velocityY
                        ) > swipeVELOCITYTHRESHOLD
                    ) {
                        if (diffY < 0) {
                            onSwipeUp()
                        }
                        else {
                            onSwipeDown()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                onTouchUp()
            }
            return false
        }
    }
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onClick() {}
    open fun onDoubleClick() {}
    open fun onLongClick() {}
    open fun onTouchDown() {}
    open fun onTouchUp() {}

    open fun onMove(){}
    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }
}
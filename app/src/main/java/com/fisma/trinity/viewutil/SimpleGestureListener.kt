package com.fisma.trinity.viewutil

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Created by CoXier on 17-2-21.
 */

class SimpleGestureListener : GestureDetector.SimpleOnGestureListener() {
  private var mListener: Listener? = null
  private val SWIPE_THRESHOLD = 100
  private val SWIPE_VELOCITY_THRESHOLD = 100

  override fun onDown(e: MotionEvent): Boolean {
    return true
  }

  override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
    var result = false
    try {
      val diffY = e2.y - e1.y
      val diffX = e2.x - e1.x
      if (Math.abs(diffX) > Math.abs(diffY)) {
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffX > 0) {
            mListener!!.onSwipeRight(velocityX)
          } else {
            mListener!!.onSwipeLeft(velocityX)
          }
          result = true
        }
      } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
        if (diffY > 0) {
          mListener!!.onSwipeBottom(velocityY)
        } else {
          mListener!!.onSwipeTop(velocityY)
        }
        result = true
      }
    } catch (exception: Exception) {
      exception.printStackTrace()
    }

    return result
  }

  override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
    Log.i(TAG, e1.toString() + "\n" + e2.toString())
    Log.d(TAG, "distanceX = $distanceX,distanceY = $distanceY")
    if (mListener == null)
      return true

    if (distanceX == 0f && abs(distanceY) > 1) {
      mListener!!.onScrollVertical(distanceY)
    }

    if (distanceY == 0f && abs(distanceX) > 1) {
      mListener!!.onScrollHorizontal(distanceX)
    }
    return true
  }


  fun setListener(mListener: Listener) {
    this.mListener = mListener
  }

  interface Listener {
    fun onSwipeRight(velocityX: Float)
    fun onSwipeLeft(velocityX: Float)
    fun onSwipeTop(velocityY: Float)
    fun onSwipeBottom(velocityY: Float)
    /**
     * left scroll dx >0
     * right scroll dx <0
     * @param dx
     */
    fun onScrollHorizontal(dx: Float)

    /**
     * upward scroll dy > 0
     * downward scroll dy < 0
     * @param dy
     */
    fun onScrollVertical(dy: Float)
  }

  companion object {
    private val TAG = "SimpleGestureListener"
  }
}
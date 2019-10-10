package com.fisma.trinity.viewutil

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import com.fisma.trinity.widgets.Workspace


class WorkspaceGestureListener(private val _desktop: Workspace, private val _callback: WorkspaceGestureCallback) : SimpleFingerGestures.OnFingerGestureListener {

  enum class Type {
    SwipeUp,
    SwipeDown,
    SwipeLeft,
    SwipeRight,
    Pinch,
    Unpinch,
    DoubleTap
  }

  override fun onSwipeUp(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.SwipeUp)
  }

  override fun onSwipeDown(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.SwipeDown)
  }

  override fun onSwipeLeft(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.SwipeLeft)
  }

  override fun onSwipeRight(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.SwipeRight)
  }

  override fun onPinch(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.Pinch)
  }

  override fun onUnpinch(i: Int, l: Long, v: Double): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.Unpinch)
  }

  override fun onDoubleTap(i: Int): Boolean {
    return _callback.onDrawerGesture(_desktop, Type.DoubleTap)
  }

  interface WorkspaceGestureCallback {
    fun onDrawerGesture(desktop: Workspace, event: Type): Boolean
  }
}

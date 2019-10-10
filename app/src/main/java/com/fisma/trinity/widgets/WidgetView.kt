package com.fisma.trinity.widgets

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.view.View


class WidgetView(context: Context) : AppWidgetHostView(context) {
  private var _onTouchListener: OnTouchListener? = null
  private var _longClick: OnLongClickListener? = null
  private var _down: Long = 0

  override fun setOnTouchListener(onTouchListener: OnTouchListener) {
    _onTouchListener = onTouchListener
  }

  override fun setOnLongClickListener(l: OnLongClickListener?) {
    _longClick = l
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (_onTouchListener != null)
      _onTouchListener!!.onTouch(this, ev)
    when (ev.actionMasked) {
      MotionEvent.ACTION_DOWN -> _down = System.currentTimeMillis()
      MotionEvent.ACTION_MOVE -> {
        val upVal = System.currentTimeMillis() - _down > 300L
        if (upVal) {
          _longClick!!.onLongClick(this)
        }
      }
    }
    return false
  }

}

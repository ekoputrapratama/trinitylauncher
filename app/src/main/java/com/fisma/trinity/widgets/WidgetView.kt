package com.fisma.trinity.widgets

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.widget.RemoteViews


class WidgetView(val mContext: Context) : AppWidgetHostView(mContext) {
  private var _onTouchListener: OnTouchListener? = null
  private var _longClick: OnLongClickListener? = null
  private var _down: Long = 0
  private var mPreviousOrientation: Int = 0

  override fun updateAppWidget(remoteViews: RemoteViews?) {
    // Store the orientation in which the widget was inflated
    mPreviousOrientation = mContext.resources.configuration.orientation
    super.updateAppWidget(remoteViews)
  }

  fun isReinflateRequired(): Boolean {
    // Re-inflate is required if the orientation has changed since last inflated.
    val orientation = mContext.resources.configuration.orientation
    return mPreviousOrientation != orientation
  }

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

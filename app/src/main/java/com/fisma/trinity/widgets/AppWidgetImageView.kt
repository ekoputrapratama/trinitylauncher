package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ImageView


internal class AppWidgetImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
  var mAllowRequestLayout = true

  override fun onFinishInflate() {
    super.onFinishInflate()
  }

  override fun requestLayout() {
    if (mAllowRequestLayout) {
      super.requestLayout()
    }
  }

//    override fun onDraw(canvas: Canvas) {
//        canvas.save()
//        canvas.clipRect(scrollX + paddingLeft,
//                scrollY + paddingTop,
//                scrollX + right - left - paddingRight,
//                scrollY + bottom - top - paddingBottom)
//
//        super.onDraw(canvas)
//        canvas.restore()
//
//    }
}
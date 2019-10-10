package com.fisma.trinity.model

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.graphics.Bitmap

class AppWidget : Item {
  var previewImage: Bitmap? = null
  var projectionImage: Bitmap? = null
  var widgetInfo: AppWidgetProviderInfo? = null
  var minSpanX: Int = 0
  var minSpanY: Int = 0
  var maxSpanX: Int = 0
  var maxSpanY: Int = 0
  var minWidth: Int = 0
  var minHeight: Int = 0
  var boundWidget: AppWidgetHostView? = null
  var packageName: String? = null
  var className: String? = null
  var appWidgetId: Int = 0
    set(value) {
      field = value
      widgetValue = value
    }

  constructor() : super() {}
  constructor(widgetInfo: AppWidgetProviderInfo) : super() {
    type = Type.APPWIDGET
    label = widgetInfo.label
    spanX = 1
    spanY = 1
    this.widgetInfo = widgetInfo
  }
}


package com.fisma.trinity.viewutil

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.fisma.trinity.widgets.WidgetView

class WidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {
  override fun onCreateView(context: Context, appWidgetId: Int, appWidget: AppWidgetProviderInfo): AppWidgetHostView {
    return WidgetView(context)
  }
}

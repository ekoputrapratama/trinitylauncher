package com.fisma.trinity.compat

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle

abstract class AppWidgetManagerCompat internal constructor(internal val mContext: Context) {

  internal val mAppWidgetManager: AppWidgetManager

  abstract val allProviders: List<AppWidgetProviderInfo>

  init {
    mAppWidgetManager = AppWidgetManager.getInstance(mContext)
  }

  fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo {
    return mAppWidgetManager.getAppWidgetInfo(appWidgetId)
  }

  abstract fun loadLabel(info: AppWidgetProviderInfo): String

  abstract fun bindAppWidgetIdIfAllowed(
    appWidgetId: Int,
    info: AppWidgetProviderInfo,
    options: Bundle?
  ): Boolean

  abstract fun startConfigActivity(
    info: AppWidgetProviderInfo,
    widgetId: Int,
    activity: Activity,
    host: AppWidgetHost,
    requestCode: Int
  )
  abstract fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle?)
  abstract fun loadPreview(info: AppWidgetProviderInfo): Drawable

  abstract fun loadIcon(info: AppWidgetProviderInfo): Drawable

  abstract fun getBadgeBitmap(info: AppWidgetProviderInfo, bitmap: Bitmap): Bitmap
  abstract fun getUser(info: AppWidgetProviderInfo): UserHandleCompat

  companion object {

    private val sInstanceLock = Any()
    private var sInstance: AppWidgetManagerCompat? = null

    fun getInstance(context: Context): AppWidgetManagerCompat {
      synchronized(sInstanceLock) {
        if (sInstance == null) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sInstance = AppWidgetManagerCompatVL(context.applicationContext)
          } else {
            sInstance = AppWidgetManagerCompatV16(context.applicationContext)
          }
        }
        return sInstance!!
      }
    }
  }
}

package com.fisma.trinity.compat

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Process.myUserHandle
import com.fisma.trinity.util.Tool

internal class AppWidgetManagerCompatV16(context: Context) : AppWidgetManagerCompat(context) {


  override val allProviders: List<AppWidgetProviderInfo>
    get() = mAppWidgetManager.installedProviders

  override fun loadLabel(info: AppWidgetProviderInfo): String {
    return info.label.trim { it <= ' ' }
  }

  override fun bindAppWidgetIdIfAllowed(
    appWidgetId: Int,
    info: AppWidgetProviderInfo,
    options: Bundle?
  ): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
      mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
    } else {
      mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider, options)
    }
  }
  override fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle?) {
    mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options)
  }
  override fun startConfigActivity(
    info: AppWidgetProviderInfo,
    widgetId: Int,
    activity: Activity,
    host: AppWidgetHost,
    requestCode: Int
  ) {
    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
    intent.component = info.configure
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    Tool.startActivityForResultSafely(activity, intent, requestCode)
  }

  override fun loadPreview(info: AppWidgetProviderInfo): Drawable {
    return mContext.packageManager.getDrawable(
      info.provider.packageName, info.previewImage, null)
  }

  override fun loadIcon(info: AppWidgetProviderInfo): Drawable {
    return Tool.getFullResIcon(mContext, info.provider.packageName, info.icon)
  }

  override fun getBadgeBitmap(info: AppWidgetProviderInfo, bitmap: Bitmap): Bitmap {
    return bitmap
  }

  override fun getUser(info: AppWidgetProviderInfo): UserHandleCompat {
    return UserHandleCompat.myUserHandle()
  }
}

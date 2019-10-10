package com.fisma.trinity.compat

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.view.View
import android.widget.Toast
import com.fisma.trinity.R

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class AppWidgetManagerCompatVL(val context: Context) : AppWidgetManagerCompat(context) {


  private val mUserManager: UserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
  private val mPm: PackageManager = context.packageManager

  override val allProviders: List<AppWidgetProviderInfo>
    get() {
      val providers = ArrayList<AppWidgetProviderInfo>()
      for (user in mUserManager.userProfiles) {
        providers.addAll(mAppWidgetManager.getInstalledProvidersForProfile(user))
      }
      return providers
    }

  override fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle?) {
    mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options)
  }
  override fun loadLabel(info: AppWidgetProviderInfo): String {
    return info.loadLabel(mPm)
  }

  override fun bindAppWidgetIdIfAllowed(
    appWidgetId: Int,
    info: AppWidgetProviderInfo,
    options: Bundle?
  ): Boolean {
    return mAppWidgetManager.bindAppWidgetIdIfAllowed(
      appWidgetId, info.profile, info.provider, options)
  }

  override fun getUser(info: AppWidgetProviderInfo): UserHandleCompat {
    return UserHandleCompat.fromUser(info.profile)!!
  }

  override fun startConfigActivity(
    info: AppWidgetProviderInfo,
    widgetId: Int,
    activity: Activity,
    host: AppWidgetHost,
    requestCode: Int
  ) {
    try {
      host.startAppWidgetConfigureActivityForResult(activity, widgetId, 0, requestCode, null)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
      Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  override fun loadPreview(info: AppWidgetProviderInfo): Drawable {
    return info.loadPreviewImage(context, 0)
  }

  override fun loadIcon(info: AppWidgetProviderInfo): Drawable {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val fullResIconDpi = activityManager.launcherLargeIconDensity
    return info.loadIcon(context, fullResIconDpi)
  }

  override fun getBadgeBitmap(info: AppWidgetProviderInfo, bitmap: Bitmap): Bitmap {
    if (info.profile == android.os.Process.myUserHandle()) {
      return bitmap
    }

    // Add a user badge in the bottom right of the image.
    val res = context.resources
    val badgeSize = res.getDimensionPixelSize(R.dimen.profile_badge_size)
    val badgeMargin = res.getDimensionPixelSize(R.dimen.profile_badge_margin)
    val badgeLocation = Rect(0, 0, badgeSize, badgeSize)

    val top = bitmap.height - badgeSize - badgeMargin
    if (res.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
      badgeLocation.offset(badgeMargin, top)
    } else {
      badgeLocation.offset(bitmap.width - badgeSize - badgeMargin, top)
    }

    val drawable = mPm.getUserBadgedDrawableForDensity(
      BitmapDrawable(res, bitmap), info.profile, badgeLocation, 0)

    if (drawable is BitmapDrawable) {
      return drawable.bitmap
    }

    bitmap.eraseColor(Color.TRANSPARENT)
    val c = Canvas(bitmap)
    drawable.setBounds(0, 0, bitmap.width, bitmap.height)
    drawable.draw(c)
    c.setBitmap(null)
    return bitmap
  }
}

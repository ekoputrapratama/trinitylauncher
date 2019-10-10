package com.fisma.trinity.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.TrinityApplication
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.model.App
import kotlin.math.ceil

class Tool {
  companion object {
    fun hideKeyboard(context: Context, view: View) {
      val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ?: return
      inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboard(context: Context, view: View) {
      val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ?: return
      inputMethodManager.toggleSoftInputFromWindow(view.windowToken, InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun vibrate(view: View) {
      val vibrator = view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      if (vibrator == null) {
        // some manufacturers do not vibrate on long press
        // might as well make this a fallback method
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, 80))
      } else {
        vibrator.vibrate(50)
      }
    }

    fun toast(context: Context, str: Int) {
      Toast.makeText(context, context.resources.getString(str), Toast.LENGTH_SHORT).show()
    }

    fun toast(context: Context, str: String) {
      Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
      try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        return true
      } catch (e: PackageManager.NameNotFoundException) {
        return false
      }
    }

    fun startActivityForResultSafely(
      activity: Activity,
      intent: Intent,
      requestCode: Int
    ) {
      try {
        activity.startActivityForResult(intent, requestCode)
      } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
      } catch (e: SecurityException) {
        Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
        Log.e("Tool", "Launcher does not have the permission to launch " + intent +
          ". Make sure to create a MAIN intent-filter for the corresponding activity " +
          "or use the exported attribute for this activity.", e)
      }
    }

    fun dp2px(dp: Float): Int {
      val resources = Resources.getSystem()
      val px = dp * resources.displayMetrics.density
      return ceil(px.toDouble()).toInt()
    }

    fun px2dp(px: Float): Int {
      val resources = Resources.getSystem()
      val dp = px / resources.displayMetrics.density
      return ceil(px.toDouble()).toInt()
    }

    fun sp2px(sp: Float): Int {
      val resources = Resources.getSystem()
      val px = sp * resources.displayMetrics.scaledDensity
      return ceil(px.toDouble()).toInt()
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float): Float {
      val resources = Resources.getSystem()
      return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
      val resources = Resources.getSystem()
      return px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun clampInt(target: Int, min: Int, max: Int): Int {
      return Math.max(min, Math.min(max, target))
    }

    fun clampFloat(target: Float, min: Float, max: Float): Float {
      return Math.max(min, Math.min(max, target))
    }

    fun startApp(context: Context, app: App, view: View?) {
      val launcher = HomeActivity.launcher
      launcher.onStartApp(context, app, view)
    }

    fun convertPoint(fromPoint: Point, fromView: View, toView: View): Point {
      val fromCoordinate = IntArray(2)
      val toCoordinate = IntArray(2)
      fromView.getLocationOnScreen(fromCoordinate)
      toView.getLocationOnScreen(toCoordinate)

      return Point(fromCoordinate[0] - toCoordinate[0] + fromPoint.x, fromCoordinate[1] - toCoordinate[1] + fromPoint.y)
    }

    fun getFullResIcon(context: Context, iconId: Int): Drawable {
      val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      val fullResIconDpi = activityManager.launcherLargeIconDensity

      var d: Drawable?
      try {
        if (iconId == android.R.mipmap.sym_def_app_icon) {
          d = Resources.getSystem().getDrawableForDensity(iconId, fullResIconDpi)
        } else {
          d = context.resources.getDrawableForDensity(iconId, fullResIconDpi)
        }
      } catch (e: Resources.NotFoundException) {
        d = null
      }

      return d ?: getFullResIcon(context, android.R.mipmap.sym_def_app_icon)
    }

    fun getFullResIcon(context: Context, packageName: String, iconId: Int): Drawable {
      val mPackageManager = context.packageManager
      var resources: Resources? = null
      try {
        resources = mPackageManager.getResourcesForApplication(packageName)
      } catch (e: PackageManager.NameNotFoundException) {
        resources = null
      }

      if (resources != null) {
        if (iconId != 0) {
          return getFullResIcon(context, iconId)
        }
      }

      return getFullResIcon(context, android.R.mipmap.sym_def_app_icon)
    }

    fun isViewContains(view: View, rx: Int, ry: Int): Boolean {
      val _tempArrayOfInt2 = IntArray(2)
      view.getLocationOnScreen(_tempArrayOfInt2)
      val x = _tempArrayOfInt2[0]
      val y = _tempArrayOfInt2[1]
      val w = view.width
      val h = view.height
      return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    fun getDefaultAppInfo(packageManager: PackageManager, category: Constants.AppCategory): ResolveInfo? {
      val intent = IntentUtil.getDefaultAppIntent(category)
      val activitiesInfo = packageManager.queryIntentActivities(intent, 0)
      Log.d("getDefaultAppInfo", "${activitiesInfo.size} activities for category ${category.toString()}")

      return if (activitiesInfo.size > 0) {
        Log.d("getDefaultAppInfo", "icon for ${category.toString()} = ${activitiesInfo[0].iconResource}")
//        activitiesInfo[0].resolvePackageName
//        packageManager.getApplicationInfo(activitiesInfo[0].resolvePackageName, 0)
        activitiesInfo[0]
      } else null
    }
  }
}


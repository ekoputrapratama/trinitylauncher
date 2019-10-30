package com.fisma.trinity.model


import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import com.fisma.trinity.util.ImageUtil


class App {
  var icon: Drawable
  var label: String
  var packageName: String
  var className: String

  val componentName: String
    get() = ComponentName(packageName, className).toString()

  constructor(pm: PackageManager, info: ResolveInfo) {
    icon = info.loadIcon(pm)
    label = info.loadLabel(pm).toString()
    packageName = info.activityInfo.packageName
    className = info.activityInfo.name

    if (icon != null) {
      // remove margin from original icon, so that we can set the padding with consistent icon size
      var bitmap = ImageUtil.removeMargins(ImageUtil.drawableToBitmap(icon)!!, Color.TRANSPARENT)
      // normalize icon and make it square for image which doesn't have a square shape
      if (bitmap.width > bitmap.height) {
        val paddingY = bitmap.width - bitmap.height
        bitmap = ImageUtil.addPadding(bitmap, 0f, paddingY.toFloat())
      } else if (bitmap.height > bitmap.width) {
        val paddingX = bitmap.height - bitmap.width
        bitmap = ImageUtil.addPadding(bitmap, paddingX.toFloat(), 0f)
      }

      icon = BitmapDrawable(Resources.getSystem(), bitmap)
    }
  }

  constructor(pm: PackageManager, info: ApplicationInfo) {
    icon = info.loadIcon(pm)
    label = info.loadLabel(pm).toString()
    packageName = info.packageName
    className = info.name
    try {
      // there is definitely a better way to store the apps
      // should probably just store component name
      val intent = pm.getLaunchIntentForPackage(packageName)
      val componentName = intent!!.component
      className = componentName!!.className
    } catch (e: Exception) {
      e.printStackTrace()
    }

  }

  override fun equals(`object`: Any?): Boolean {
    if (`object` is App) {
      val app = `object` as App?
      return packageName == app!!.packageName
    } else {
      return false
    }
  }
}

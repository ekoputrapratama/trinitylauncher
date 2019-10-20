package com.fisma.trinity.util

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import com.fisma.trinity.model.App


object IconPackHelper {
  fun applyIconPack(appManager: AppManager, iconSize: Int, iconPackName: String, apps: List<App>) {
    var iconPackResources: Resources? = null
    var intResourceIcon = 0
    var intResourceBack = 0
    var intResourceMask = 0
    var intResourceUpon = 0
    var scale = 1f

    val p = Paint(Paint.FILTER_BITMAP_FLAG)
    p.isAntiAlias = true

    val origP = Paint(Paint.FILTER_BITMAP_FLAG)
    origP.isAntiAlias = true

    val maskP = Paint(Paint.FILTER_BITMAP_FLAG)
    maskP.isAntiAlias = true
    maskP.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    if (iconPackName != "") {
      try {
        iconPackResources = appManager._packageManager.getResourcesForApplication(iconPackName)
      } catch (e: Exception) {
        println(e)
      }

      if (iconPackResources != null) {
        if (getResource(iconPackResources, iconPackName, "iconback", null) != null)
          intResourceBack = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconback", null), "drawable", iconPackName)
        if (getResource(iconPackResources, iconPackName, "iconmask", null) != null)
          intResourceMask = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconmask", null), "drawable", iconPackName)
        if (getResource(iconPackResources, iconPackName, "iconupon", null) != null)
          intResourceUpon = iconPackResources.getIdentifier(getResource(iconPackResources, iconPackName, "iconupon", null), "drawable", iconPackName)
        if (getResource(iconPackResources, iconPackName, "scale", null) != null)
          scale = java.lang.Float.parseFloat(getResource(iconPackResources, iconPackName, "scale", null)!!)
      }
    }

    val uniformOptions = BitmapFactory.Options()
    uniformOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
    uniformOptions.inScaled = false
    uniformOptions.inDither = false

    var back: Bitmap? = null
    var mask: Bitmap? = null
    var upon: Bitmap? = null
    var canvasOrig: Canvas
    var canvas: Canvas
    var scaledBitmap: Bitmap
    var scaledOrig: Bitmap
    var orig: Bitmap

    if (iconPackName.compareTo("") != 0 && iconPackResources != null) {
      try {
        if (intResourceBack != 0)
          back = BitmapFactory.decodeResource(iconPackResources, intResourceBack, uniformOptions)
        if (intResourceMask != 0)
          mask = BitmapFactory.decodeResource(iconPackResources, intResourceMask, uniformOptions)
        if (intResourceUpon != 0)
          upon = BitmapFactory.decodeResource(iconPackResources, intResourceUpon, uniformOptions)
      } catch (e: Exception) {
        println(e)
      }

    }

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = false
    options.inPreferredConfig = Bitmap.Config.RGB_565
    options.inDither = true

    for (i in apps.indices) {
      if (iconPackResources != null) {
        val iconResource = getResource(iconPackResources, iconPackName, null, apps[i].componentName)
        if (iconResource != null) {
          intResourceIcon = iconPackResources.getIdentifier(iconResource, "drawable", iconPackName)
        } else {
          intResourceIcon = 0
        }

        if (intResourceIcon != 0) {
          // has single drawable for app
          apps[i].icon = BitmapDrawable(BitmapFactory.decodeResource(iconPackResources, intResourceIcon, uniformOptions))
        } else {
          try {
            orig = Bitmap.createBitmap(apps[i].icon.intrinsicWidth, apps[i].icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
          } catch (e: Exception) {
            continue
          }

          apps[i].icon.setBounds(0, 0, apps[i].icon.intrinsicWidth, apps[i].icon.intrinsicHeight)
          apps[i].icon.draw(Canvas(orig))

          scaledOrig = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
          scaledBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
          canvas = Canvas(scaledBitmap)

          if (back != null)
            canvas.drawBitmap(back, getResizedMatrix(back, iconSize, iconSize), p)

          canvasOrig = Canvas(scaledOrig)
          orig = getResizedBitmap(orig, (iconSize * scale).toInt(), (iconSize * scale).toInt())
          canvasOrig.drawBitmap(orig, (scaledOrig.width - orig.width / 2 - scaledOrig.width / 2).toFloat(), (scaledOrig.width - orig.width / 2 - scaledOrig.width / 2).toFloat(), origP)

          if (mask != null)
            canvasOrig.drawBitmap(mask, getResizedMatrix(mask, iconSize, iconSize), maskP)

          canvas.drawBitmap(getResizedBitmap(scaledOrig, iconSize, iconSize), 0f, 0f, p)

          if (upon != null)
            canvas.drawBitmap(upon, getResizedMatrix(upon, iconSize, iconSize), p)

          apps[i].icon = BitmapDrawable(appManager._context.resources, scaledBitmap)
        }
      }
    }
  }

  private fun getResource(resources: Resources, packageName: String, resourceName: String?, componentName: String?): String? {
    val xrp: XmlResourceParser
    var resource: String? = null
    try {
      val resourceValue = resources.getIdentifier("appfilter", "xml", packageName)
      if (resourceValue != 0) {
        xrp = resources.getXml(resourceValue)
        while (xrp.eventType != XmlResourceParser.END_DOCUMENT) {
          if (xrp.eventType == 2) {
            try {
              val string = xrp.name
              if (componentName != null) {
                if (xrp.getAttributeValue(0).compareTo(componentName) == 0) {
                  resource = xrp.getAttributeValue(1)
                }
              } else if (string == resourceName) {
                resource = xrp.getAttributeValue(0)
              }
            } catch (e: Exception) {
              println(e)
            }

          }
          xrp.next()
        }
      }
    } catch (e: Exception) {
      println(e)
    }

    return resource
  }

  private fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
  }

  private fun getResizedMatrix(bm: Bitmap, newHeight: Int, newWidth: Int): Matrix {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return matrix
  }
}

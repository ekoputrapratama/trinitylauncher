package com.fisma.trinity.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction
import java.io.ByteArrayOutputStream
import java.util.*

class Shortcut {
  var packageName: String? = null
  var className: String? = null
  var label: String? = null
  var icon: Bitmap? = null
  var iconTheme: Bitmap? = null
  var type: Type = Type.APP
  var action: LauncherAction.Action? = null
  var index: Int = 0
  var id: Int

  init {
    val random = Random()
    id = random.nextInt()
  }

  fun setIcon(drawable: Drawable) {
    icon = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    ImageUtil.renderDrawableToBitmap(drawable, icon, 0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
  }

  fun getIconBlob(): ByteArray? {
    icon ?: return null

    val stream = ByteArrayOutputStream()
    icon!!.compress(Bitmap.CompressFormat.PNG, 0, stream)
    return stream.toByteArray()
  }

  fun getIconDrawable(res: Resources? = null): Drawable {
    res ?: return BitmapDrawable(Resources.getSystem(), icon)
    return BitmapDrawable(res, icon)
  }

  override fun equals(other: Any?): Boolean {
    return other is Shortcut && other.hashCode() == hashCode()
  }

  override fun hashCode(): Int {
    var result = packageName?.hashCode() ?: 0
    result = 31 * result + (className?.hashCode() ?: 0)
    result = 31 * result + (label?.hashCode() ?: 0)
    result = 31 * result + (icon?.hashCode() ?: 0)
    result = 31 * result + (iconTheme?.hashCode() ?: 0)
    result = 31 * result + type.hashCode()
    result = 31 * result + (action?.hashCode() ?: 0)
    result = 31 * result + index
    result = 31 * result + id
    return result
  }

  class Builder {
    var instance: Shortcut? = null

    init {
      instance = Shortcut()
    }

    fun setPackageName(packageName: String): Builder {
      instance!!.packageName = packageName
      return this
    }

    fun setClassName(className: String): Builder {
      instance!!.className = className
      return this
    }

    fun setIcon(bitmap: Bitmap): Builder {
      instance!!.icon = bitmap
      return this
    }

    fun setIcon(drawable: Drawable): Builder {
      instance!!.setIcon(drawable)
      return this
    }

    fun setIcon(byteArray: ByteArray?): Builder {
      if (byteArray != null) {
        instance!!.icon = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
      }
      return this
    }


    fun setLabel(label: String): Builder {
      instance!!.label = label
      return this
    }

    fun setType(type: Type): Builder {
      instance!!.type = type
      return this
    }

    fun setAction(action: LauncherAction.Action): Builder {
      instance!!.action = action
      return this
    }

    fun setIndex(index: Int): Builder {
      instance!!.index = index
      return this
    }

    fun setId(id: Int): Builder {
      instance!!.id = id
      return this
    }

    fun build(): Shortcut {
      return instance!!
    }
  }

  enum class Type {
    APP, ACTION
  }

  companion object {
    fun fromApp(app: App): Shortcut {
      return Builder()
        .setClassName(app.className)
        .setPackageName(app.packageName)
        .setIcon(app.icon)
        .setLabel(app.label)
        .build()
    }
  }
}
package com.fisma.trinity.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import com.fisma.trinity.util.ImageUtil
import java.io.ByteArrayOutputStream

class Plugin {
  var packageName: String? = null
  var className: String? = null
  var label: String? = null
  var enabled: Boolean = true
  var uri: Uri? = null
  var id: Int = 0
  var icon: Bitmap? = null

  fun setUri(uri: String): Plugin {
    this.uri = Uri.parse(uri)
    return this
  }

  fun setIcon(icon: Bitmap): Plugin {
    this.icon = icon
    return this
  }

  fun setIcon(icon: Drawable): Plugin {
    this.icon = ImageUtil.drawableToBitmap(icon)
    return this
  }

  fun getIconBitmap(): Bitmap? {
    return icon
  }

  fun getIconDrawable(res: Resources? = null): Drawable {
    res ?: return BitmapDrawable(Resources.getSystem(), icon)
    return BitmapDrawable(res, icon)
  }

  fun getIconBlob(): ByteArray? {
    icon ?: return null

    val stream = ByteArrayOutputStream()
    icon!!.compress(Bitmap.CompressFormat.PNG, 0, stream)
    return stream.toByteArray()
  }

  override fun equals(other: Any?): Boolean {
    return other is Plugin && other.hashCode() != hashCode()
  }


  override fun hashCode(): Int {
    var result = packageName?.hashCode() ?: 0
    result = 31 * result + (label?.hashCode() ?: 0)
    result = 31 * result + enabled.hashCode()
    result = 31 * result + (uri?.hashCode() ?: 0)
    return result
  }

  class Builder {
    var instance: Plugin? = null

    init {
      instance = Plugin()
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

    fun setLabel(label: String?): Builder {
      instance!!.label = label
      return this
    }

    fun setUri(uri: String): Builder {
      instance!!.uri = Uri.parse(uri)
      return this
    }

    fun setUri(uri: Uri): Builder {
      instance!!.uri = uri
      return this
    }

    fun setEnabled(enabled: Boolean): Builder {
      instance!!.enabled = enabled
      return this
    }

    fun setEnabled(enabledInt: Int): Builder {
      instance!!.enabled = enabledInt == 1
      return this
    }


    fun setId(id: Int): Builder {
      instance!!.id = id
      return this
    }

    fun build(): Plugin {
      return instance!!
    }
  }
}
package com.fisma.trinity.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.fisma.trinity.util.ImageUtil
import java.util.*

class Plugin {
  var _packageName: String? = null
    private set
  var _className: String? = null
    private set
  var _label: String? = null
    private set
  var _enabled: Boolean = true
    private set
  var _uri: Uri? = null
  var _id: Int = 0
  var _icon: Bitmap? = null

  init {
    val random = Random()
    _id = random.nextInt()
  }

  fun id(id: Int): Plugin {
    _id = id
    return this
  }

  fun uri(uri: String): Plugin {
    _uri = Uri.parse(uri)
    return this
  }

  fun uri(uri: Uri): Plugin {
    _uri = uri
    return this
  }

  fun icon(icon: Bitmap): Plugin {
    _icon = icon
    return this
  }

  fun icon(icon: Drawable): Plugin {
    _icon = ImageUtil.drawableToBitmap(icon)
    return this
  }

  fun packageName(text: String?): Plugin {
    _packageName = text
    return this
  }

  fun className(className: String) {
    _className = className
  }

  fun label(text: String?): Plugin {
    _label = text
    return this
  }

  fun enabled(enabled: Boolean): Plugin {
    _enabled = enabled
    return this
  }

  override fun equals(other: Any?): Boolean {
    return other is Plugin && other.hashCode() != hashCode()
  }

  inline fun build(func: Plugin.() -> Unit): Plugin {
    this.func()
    return this
  }

  override fun hashCode(): Int {
    var result = _packageName?.hashCode() ?: 0
    result = 31 * result + (_label?.hashCode() ?: 0)
    result = 31 * result + _enabled.hashCode()
    result = 31 * result + (_uri?.hashCode() ?: 0)
    return result
  }
}
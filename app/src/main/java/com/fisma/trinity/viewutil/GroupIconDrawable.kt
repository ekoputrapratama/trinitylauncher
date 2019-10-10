package com.fisma.trinity.viewutil

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.App
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.Tool


class GroupIconDrawable(context: Context, item: Item, iconSize: Int) : Drawable() {

  private var _icons: Array<Drawable?>? = null
  private var _iconsCount: Int = 0
  private var _paintInnerCircle: Paint? = null
  private var _paintOuterCircle: Paint? = null
  private var _paintIcon: Paint? = null
  private var _needAnimate: Boolean = false
  private var _needAnimateScale: Boolean = false
  private var _scaleFactor = 1f
  private var _iconSize: Float = 0.toFloat()
  private var _padding: Float = 0.toFloat()
  private var _padding31: Float = 0.toFloat() // For group of 3 icons (1st row extra padding)
  private var _padding32: Float = 0.toFloat() // For group of 3 icons (2st row extra padding)
  private var _outline: Int = 0
  private var _iconSizeDiv2: Int = 0
  private var _iconSizeDiv4: Int = 0

  init {
    val size = Tool.dp2px(iconSize.toFloat()).toFloat()
    val icons = arrayOfNulls<Drawable>(4)
    for (i in 0..3) {
      icons[i] = null
    }
    init(icons, item.getItems().size, size)
    var i = 0
    while (i < 4 && i < item.getItems().size) {
      val temp = item.getItems().get(i)
      var app: App? = null
      if (temp != null) {
        app = Settings.appLoader().findItemApp(temp)
      }
      if (app == null) {
        Settings.logger().log(this, Log.DEBUG, TAG, "Item %s has a null app at index %d (IntentUtil: %s)", item.label, i, if (temp == null) "Item is NULL" else temp!!.intent)
        icons[i] = ColorDrawable(Color.TRANSPARENT)
      } else {
        _icons!![i] = app.icon
      }
      i++
    }
  }

  override fun getIntrinsicHeight(): Int {
    return _iconSize.toInt()
  }

  override fun getIntrinsicWidth(): Int {
    return _iconSize.toInt()
  }

  private fun init(icons: Array<Drawable?>, iconsCount: Int, size: Float) {
    _icons = icons
    _iconsCount = iconsCount
    _iconSize = size
    _iconSizeDiv2 = Math.round(_iconSize / 2f)
    _iconSizeDiv4 = Math.round(_iconSize / 4f)
    _padding = _iconSize / 25f
    val b = _iconSize / 2f + 2 * _padding
    _padding31 = b * PADDING31_KOEF
    _padding32 = b * (PADDING32_KOEF - PADDING31_KOEF)

    _paintInnerCircle = Paint()
    _paintInnerCircle!!.color = Color.WHITE
    _paintInnerCircle!!.alpha = 150
    _paintInnerCircle!!.isAntiAlias = true

    _paintOuterCircle = Paint()
    _paintOuterCircle!!.color = Color.WHITE
    _paintOuterCircle!!.isAntiAlias = true
    _paintOuterCircle!!.flags = Paint.ANTI_ALIAS_FLAG
    _paintOuterCircle!!.style = Paint.Style.STROKE
    _outline = Tool.dp2px(1f)
    _paintOuterCircle!!.strokeWidth = _outline.toFloat()

    _paintIcon = Paint()
    _paintIcon!!.isAntiAlias = true
    _paintIcon!!.isFilterBitmap = true
  }

  fun popUp() {
    _needAnimate = true
    _needAnimateScale = true
    invalidateSelf()
  }

  fun popBack() {
    _needAnimate = false
    _needAnimateScale = false
    invalidateSelf()
  }

  override fun draw(canvas: Canvas) {
    canvas.save()

    if (_needAnimateScale) {
      _scaleFactor = Tool.clampFloat(_scaleFactor - 0.09f, 0.5f, 1f)
    } else {
      _scaleFactor = Tool.clampFloat(_scaleFactor + 0.09f, 0.5f, 1f)
    }

    canvas.scale(_scaleFactor, _scaleFactor, _iconSize / 2, _iconSize / 2)

    val clip = Path()
    clip.addCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, Path.Direction.CW)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      canvas.clipPath(clip, Region.Op.REPLACE)
    }

    canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, _paintInnerCircle!!)

    if (_iconsCount > 3) {
      if (_icons!![0] != null) {
        drawIcon(canvas, _icons!![0], _padding, _padding, _iconSizeDiv2 - _padding, _iconSizeDiv2 - _padding, _paintIcon!!)
      }
      if (_icons!![1] != null) {
        drawIcon(canvas, _icons!![1], _iconSizeDiv2 + _padding, _padding, _iconSize - _padding, _iconSizeDiv2 - _padding, _paintIcon!!)
      }
      if (_icons!![2] != null) {
        drawIcon(canvas, _icons!![2], _padding, _iconSizeDiv2 + _padding, _iconSizeDiv2 - _padding, _iconSize - _padding, _paintIcon!!)
      }
      if (_icons!![3] != null) {
        drawIcon(canvas, _icons!![3], _iconSizeDiv2 + _padding, _iconSizeDiv2 + _padding, _iconSize - _padding, _iconSize - _padding, _paintIcon!!)
      }
    } else if (_iconsCount > 2) {
      if (_icons!![0] != null) {
        drawIcon(canvas, _icons!![0], _padding, _padding + _padding31, _iconSizeDiv2 - _padding, _iconSizeDiv2 - _padding + _padding31, _paintIcon!!)
      }
      if (_icons!![1] != null) {
        drawIcon(canvas, _icons!![1], _iconSizeDiv2 + _padding, _padding + _padding31, _iconSize - _padding, _iconSizeDiv2 - _padding + _padding31, _paintIcon!!)
      }
      if (_icons!![2] != null) {
        drawIcon(canvas, _icons!![2], _padding + _iconSizeDiv4, _iconSizeDiv2.toFloat() + _padding + _padding32, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _iconSize - _padding + _padding32, _paintIcon!!)
      }
    } else {// if (_iconsCount <= 2) {
      if (_icons!![0] != null) {
        drawIcon(canvas, _icons!![0], _padding, _padding + _iconSizeDiv4, _iconSizeDiv2 - _padding, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _paintIcon!!)
      }
      if (_icons!![1] != null) {
        drawIcon(canvas, _icons!![1], _iconSizeDiv2 + _padding, _padding + _iconSizeDiv4, _iconSize - _padding, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _paintIcon!!)
      }
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      canvas.clipRect(0f, 0f, _iconSize, _iconSize, Region.Op.REPLACE)
    }
    // draw the border
    canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, _paintOuterCircle!!)
    canvas.restore()

    if (_needAnimate) {
      _paintIcon!!.alpha = Tool.clampInt(_paintIcon!!.alpha - 25, 0, 255)
      invalidateSelf()
    } else if (_paintIcon!!.alpha != 255) {
      _paintIcon!!.alpha = Tool.clampInt(_paintIcon!!.alpha + 25, 0, 255)
      invalidateSelf()
    }
  }

  private fun drawIcon(canvas: Canvas, icon: Drawable?, l: Float, t: Float, r: Float, b: Float, paint: Paint) {
    icon!!.setBounds(l.toInt(), t.toInt(), r.toInt(), b.toInt())
    icon.isFilterBitmap = true
    icon.alpha = paint.alpha
    icon.draw(canvas)
  }

  override fun setAlpha(i: Int) {}

  override fun setColorFilter(colorFilter: ColorFilter?) {}

  override fun getOpacity(): Int {
    return PixelFormat.TRANSPARENT
  }

  companion object {
    private val TAG = "GroupIconDrawable"
    private val PADDING31_KOEF = 1f - Math.sqrt(3.0).toFloat() / 2f
    private val PADDING32_KOEF = (Math.sqrt(3.0) - 1f).toFloat() / (2f * Math.sqrt(3.0).toFloat())
  }
}

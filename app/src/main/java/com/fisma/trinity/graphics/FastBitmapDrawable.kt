package com.fisma.trinity.graphics

import android.R.string.cancel
import android.R.attr.start
import android.animation.ObjectAnimator
import androidx.constraintlayout.solver.widgets.WidgetContainer.getBounds
import androidx.core.view.ViewCompat.setAlpha
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.util.SparseArray
import android.animation.TimeInterpolator
import android.graphics.*
import android.graphics.drawable.Drawable


internal class FastBitmapDrawable(val bitmap: Bitmap) : Drawable() {

  private val mPaint = Paint(FILTER_BITMAP_FLAG)
  private var mAlpha: Int = 0

  var brightness = 0
    set(brightness) {
      if (this.brightness != brightness) {
        field = brightness
        updateFilter()
        invalidateSelf()
      }
    }
  /**
   * When enabled, the icon is grayed out and the contrast is increased to give it a 'ghost'
   * appearance.
   */
  var isGhostModeEnabled = false
    set(enabled) {
      if (isGhostModeEnabled != enabled) {
        field = enabled
        updateFilter()
      }
    }

  private var mPressed = false
  private var mPressedAnimator: ObjectAnimator? = null

  init {
    mAlpha = 255
    setBounds(0, 0, bitmap.width, bitmap.height)
  }

  override fun draw(canvas: Canvas) {
    val r = bounds
    // Draw the bitmap into the bounding rect
    canvas.drawBitmap(bitmap, null, r, mPaint)
  }

  override fun setColorFilter(cf: ColorFilter?) {
    // No op
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun setAlpha(alpha: Int) {
    mAlpha = alpha
    mPaint.setAlpha(alpha)
  }

  override fun setFilterBitmap(filterBitmap: Boolean) {
    mPaint.setFilterBitmap(filterBitmap)
    mPaint.setAntiAlias(filterBitmap)
  }

  override fun getAlpha(): Int {
    return mAlpha
  }

  override fun getIntrinsicWidth(): Int {
    return bitmap.width
  }

  override fun getIntrinsicHeight(): Int {
    return bitmap.height
  }

  override fun getMinimumWidth(): Int {
    return bounds.width()
  }

  override fun getMinimumHeight(): Int {
    return bounds.height()
  }

  fun setPressed(pressed: Boolean) {
    if (mPressed != pressed) {
      mPressed = pressed
      if (mPressed) {
        mPressedAnimator = ObjectAnimator
          .ofInt(this, "brightness", PRESSED_BRIGHTNESS)
          .setDuration(CLICK_FEEDBACK_DURATION)
        mPressedAnimator!!.interpolator = CLICK_FEEDBACK_INTERPOLATOR
        mPressedAnimator!!.start()
      } else if (mPressedAnimator != null) {
        mPressedAnimator!!.cancel()
        brightness = 0
      }
    }
    invalidateSelf()
  }

  private fun updateFilter() {
    if (isGhostModeEnabled) {
      if (sGhostModeMatrix == null) {
        sGhostModeMatrix = ColorMatrix()
        sGhostModeMatrix!!.setSaturation(0f)

        // For ghost mode, set the color range to [GHOST_MODE_MIN_COLOR_RANGE, 255]
        val range = (255 - GHOST_MODE_MIN_COLOR_RANGE) / 255.0f
        sTempMatrix.set(floatArrayOf(range, 0f, 0f, 0f, GHOST_MODE_MIN_COLOR_RANGE.toFloat(), 0f, range, 0f, 0f, GHOST_MODE_MIN_COLOR_RANGE.toFloat(), 0f, 0f, range, 0f, GHOST_MODE_MIN_COLOR_RANGE.toFloat(), 0f, 0f, 0f, 1f, 0f))
        sGhostModeMatrix!!.preConcat(sTempMatrix)
      }

      if (brightness == 0) {
        mPaint.setColorFilter(ColorMatrixColorFilter(sGhostModeMatrix!!))
      } else {
        setBrightnessMatrix(sTempMatrix, brightness)
        sTempMatrix.postConcat(sGhostModeMatrix)
        mPaint.setColorFilter(ColorMatrixColorFilter(sTempMatrix))
      }
    } else if (brightness != 0) {
      var filter: ColorFilter? = sCachedBrightnessFilter.get(brightness)
      if (filter == null) {
        filter = PorterDuffColorFilter(Color.argb(brightness, 255, 255, 255),
          PorterDuff.Mode.SRC_ATOP)
        sCachedBrightnessFilter.put(brightness, filter)
      }
      mPaint.setColorFilter(filter)
    } else {
      mPaint.setColorFilter(null)
    }
  }

  companion object {

    val CLICK_FEEDBACK_INTERPOLATOR: TimeInterpolator = TimeInterpolator { input ->
      if (input < 0.05f) {
        input / 0.05f
      } else if (input < 0.3f) {
        1f
      } else {
        (1 - input) / 0.7f
      }
    }
    val CLICK_FEEDBACK_DURATION: Long = 2000

    private val PRESSED_BRIGHTNESS = 100
    private var sGhostModeMatrix: ColorMatrix? = null
    private val sTempMatrix = ColorMatrix()

    /**
     * Store the brightness colors filters to optimize animations during icon press. This
     * only works for non-ghost-mode icons.
     */
    private val sCachedBrightnessFilter = SparseArray<ColorFilter>()

    private val GHOST_MODE_MIN_COLOR_RANGE = 130

    private fun setBrightnessMatrix(matrix: ColorMatrix, brightness: Int) {
      // Brightness: C-new = C-old*(1-amount) + amount
      val scale = 1 - brightness / 255.0f
      matrix.setScale(scale, scale, scale, 1f)
      val array = matrix.array

      // Add the amount to RGB components of the matrix, as per the above formula.
      // Fifth elements in the array correspond to the constant being added to
      // red, blue, green, and alpha channel respectively.
      array[4] = brightness.toFloat()
      array[9] = brightness.toFloat()
      array[14] = brightness.toFloat()
    }
  }
}
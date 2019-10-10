package com.fisma.trinity.widgets


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.View
import com.fisma.trinity.util.Tool

class PagerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), ViewPager.OnPageChangeListener {
  private var _pager: ViewPager? = null
  private val _paint = Paint(1)

  private var _mode = Mode.DOTS
  private val _pad: Float
  private var _dotSize: Float = 0.toFloat()
  private var _previousPage = -1
  private var _realPreviousPage: Int = 0

  // current position and offset
  private var _scrollOffset: Float = 0.toFloat()
  private var _scrollPosition: Int = 0

  // dot animations
  private var _shrinkFactor = 1.0f
  private var _expandFactor = 1.5f

  object Mode {
    val DOTS = 0
    val LINES = 1
  }

  init {
    setWillNotDraw(false)
    _pad = Tool.dp2px(4f).toFloat()
    _paint.color = Color.WHITE
    _paint.strokeWidth = Tool.dp2px(4f).toFloat()
    _paint.isAntiAlias = true
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    _dotSize = (height / 2).toFloat()
    super.onLayout(changed, left, top, right, bottom)
  }

  override fun onDraw(canvas: Canvas) {
    if (_pager == null) return
    val pageCount = _pager!!.adapter!!.count
    when (_mode) {
      Mode.DOTS -> {
        val circlesWidth = pageCount * (_dotSize + _pad * 2)
        canvas.translate(width / 2 - circlesWidth / 2, 0f)

        if (_realPreviousPage != _pager!!.currentItem) {
          _shrinkFactor = 1f
          _realPreviousPage = _pager!!.currentItem
        }

        for (dot in 0 until pageCount) {
          val stepFactor = 0.05f
          val smallFactor = 1.0f
          val largeFactor = 1.5f
          if (dot == _pager!!.currentItem) {
            // draw shrinking dot
            if (_previousPage == -1)
              _previousPage = dot
            _shrinkFactor = Tool.clampFloat(_shrinkFactor + stepFactor, smallFactor, largeFactor)
            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (height / 2).toFloat(), _shrinkFactor * _dotSize / 2, _paint)
            if (_shrinkFactor != largeFactor)
              invalidate()
          } else if (dot != _pager!!.currentItem && dot == _previousPage) {
            // draw expanding dot
            _expandFactor = Tool.clampFloat(_expandFactor - stepFactor, smallFactor, largeFactor)
            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (height / 2).toFloat(), _expandFactor * _dotSize / 2, _paint)
            if (_expandFactor != smallFactor)
              invalidate()
            else {
              _expandFactor = 1.5f
              _previousPage = -1
            }
          } else {
            // draw normal dot
            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (height / 2).toFloat(), _dotSize / 2, _paint)
          }
        }
      }
      Mode.LINES -> {
        val width = (width / pageCount).toFloat()
        val startX = (_scrollPosition + _scrollOffset) * width
        val startY = (height / 2).toFloat()

        canvas.drawLine(startX, startY, startX + width, startY, _paint)
        if (_scrollOffset != 0f) invalidate()
      }
    }
  }

  fun setMode(mode: Int) {
    _mode = mode
    invalidate()
  }

  fun setViewPager(pager: ViewPager?) {
    if (pager == null && _pager != null) {
      _pager!!.removeOnPageChangeListener(this)
      _pager = null
    } else {
      _pager = pager
      pager?.addOnPageChangeListener(this)
    }
    invalidate()
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    _scrollOffset = positionOffset
    _scrollPosition = position
    invalidate()
  }

  override fun onPageSelected(position: Int) {
    // nothing
  }

  override fun onPageScrollStateChanged(state: Int) {
    // nothing
  }
}

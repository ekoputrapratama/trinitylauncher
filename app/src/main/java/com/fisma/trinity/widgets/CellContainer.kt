package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import java.util.ArrayList
import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.util.Log
import com.fisma.trinity.activity.HomeActivity

import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.fisma.trinity.util.Tool
import android.view.*
import com.fisma.trinity.model.Item


open class CellContainer(context: Context, attr: AttributeSet? = null) : ViewGroup(context, attr) {
  private var _animateBackground: Boolean = false
  private val _bgPaint = Paint(1)
  private var _blockTouch: Boolean = false
  private var _cachedOutlineBitmap: Bitmap? = null
  var cellHeight: Int = 0
    private set
  var cellSpanH: Int = 0
    private set
  var cellSpanV: Int = 0
    private set
  var cellWidth: Int = 0
    private set
  private var _cells: Array<Array<Rect?>>? = null
  private val _currentOutlineCoordinate = Point(-1, -1)
  private val _currentOutlineSpan = arrayListOf(0, 0)
  private var _currentOutlineType = Item.Type.APP
  private val _down = java.lang.Long.valueOf(0)
  private var _gestures: SimpleFingerGestures? = null
  private var _hideGrid = true
  private val _paint = Paint(1)
  private var _occupied: Array<BooleanArray>? = null
  private val _outlinePaint = Paint(1)
  private var _peekDirection: PeekDirection? = null
  private var _peekDownTime: Long? = java.lang.Long.valueOf(-1)
  private var _preCoordinate = Point(-1, -1)
  private var _startCoordinate: Point? = Point()
  private val _tempRect = Rect()

  val allCells: List<View>
    get() {
      val views = ArrayList<View>()
      val childCount = childCount
      for (i in 0 until childCount) {
        views.add(getChildAt(i))
      }
      return views
    }

  companion object {
    /**
     * Computes the required horizontal and vertical cell spans to always
     * fit the given rectangle.
     *
     * @param width Width in pixels
     * @param height Height in pixels
     * @param result An array of length 2 in which to store the result (may be null).
     */
    const val TAG = "CellContainer"
  }

  enum class DragState {
    CurrentNotOccupied, OutOffRange, ItemViewNotFound, CurrentOccupied
  }

  class LayoutParams : ViewGroup.LayoutParams {
    var x: Int = 0
    var xSpan = 1
    var y: Int = 0
    var ySpan = 1

    companion object {
      const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    constructor(w: Int, h: Int, x: Int, y: Int) : super(w, h) {
      this.x = x
      this.y = y
    }

    constructor(w: Int, h: Int, x: Int, y: Int, xSpan: Int, ySpan: Int) : super(w, h) {
      this.x = x
      this.y = y
      this.xSpan = xSpan
      this.ySpan = ySpan
    }

    constructor(w: Int, h: Int) : super(w, h) {}
  }

  enum class PeekDirection {
    UP, LEFT, RIGHT, DOWN
  }

  fun setBlockTouch(v: Boolean) {
    _blockTouch = v
  }

  fun setGestures(v: SimpleFingerGestures?) {
    _gestures = v
  }

  init {
    _paint.style = Paint.Style.STROKE
    _paint.strokeWidth = 2.0f
    _paint.strokeJoin = Paint.Join.ROUND
    _paint.color = -1
    _paint.alpha = 0
    _bgPaint.style = Paint.Style.FILL
    _bgPaint.color = -1
    _bgPaint.alpha = 0
    _outlinePaint.color = -1
    _outlinePaint.alpha = 0
    init()
  }

  fun setGridSize(x: Int, y: Int) {
    cellSpanV = y
    cellSpanH = x

    _occupied = Array(cellSpanH) { BooleanArray(cellSpanV) }
    for (i in 0 until cellSpanH) {
      for (j in 0 until cellSpanV) {
        _occupied!![i][j] = false
      }
    }
    requestLayout()
  }

  fun setHideGrid(hideGrid: Boolean) {
    _hideGrid = hideGrid
    invalidate()
  }

  fun resetOccupiedSpace() {
    if (cellSpanH > 0 && cellSpanV > 0) {
      _occupied = Array(cellSpanH) { BooleanArray(cellSpanV) }
    }
  }

  override fun removeAllViews() {
    resetOccupiedSpace()
    super.removeAllViews()
  }

  fun projectImageOutlineAt(newCoordinate: Point, bitmap: Bitmap?, spanX: Int = 1, spanY: Int = 1, type: Item.Type = Item.Type.APP) {
    _cachedOutlineBitmap = bitmap
    if (_currentOutlineCoordinate != newCoordinate) {
      _outlinePaint.alpha = 0
    }
    _currentOutlineCoordinate.set(newCoordinate.x, newCoordinate.y)

    _currentOutlineSpan[0] = spanX
    _currentOutlineSpan[1] = spanY
    _currentOutlineType = type
    invalidate()
  }

  private fun drawCachedOutlineBitmap(canvas: Canvas, cell: Rect?) {
    if (_cachedOutlineBitmap != null) {
      val bitmap = _cachedOutlineBitmap
      val centerX = cell!!.centerX().toFloat()
      val centerY = cell.centerY().toFloat()
      // if span x and y is more than one it's probably appwidget preview bitmap
      if (_currentOutlineType == Item.Type.APPWIDGET) {
        Log.d(TAG, "drawing widget projection")
        if ((_currentOutlineCoordinate.x + _currentOutlineSpan[0] - 1) < _cells!!.size)
          canvas.drawBitmap(bitmap!!, cell.left.toFloat(), cell.top.toFloat(), _outlinePaint)
      } else {
        canvas.drawBitmap(bitmap!!, centerX - bitmap.width / 2, centerY - bitmap.height / 2, _outlinePaint)
      }
    }
  }

  fun clearCachedOutlineBitmap() {
    _outlinePaint.alpha = 0
    _cachedOutlineBitmap = null
    invalidate()
  }

  fun peekItemAndSwap(event: DragEvent, coordinate: Point): DragState {
    return peekItemAndSwap(event.x.toInt(), event.y.toInt(), coordinate)
  }

  fun peekItemAndSwap(x: Int, y: Int, coordinate: Point, spanX: Int = 1, spanY: Int = 1): DragState {
    touchPosToCoordinate(coordinate, x, y, spanX, spanY, false, false)
    if (coordinate.x != -1 && coordinate.y != -1) {
      if (_startCoordinate == null) {
        _startCoordinate = coordinate
      }
      if (_preCoordinate != coordinate) {
        _peekDownTime = java.lang.Long.valueOf(-1)
      }

      if (_peekDownTime != null && _peekDownTime == java.lang.Long.valueOf(-1)) {
        _peekDirection = getPeekDirectionFromCoordinate(_startCoordinate!!, coordinate)
        _peekDownTime = System.currentTimeMillis()
        _preCoordinate = coordinate
      }
      return if (_occupied!![coordinate.x][coordinate.y]) {
        DragState.CurrentOccupied
      } else {
        DragState.CurrentNotOccupied
      }
    }
    return DragState.OutOffRange
  }

  private fun getPeekDirectionFromCoordinate(from: Point, to: Point): PeekDirection? {
    if (from.y - to.y > 0) {
      return PeekDirection.UP
    }
    if (from.y - to.y < 0) {
      return PeekDirection.DOWN
    }
    if (from.x - to.x > 0) {
      return PeekDirection.LEFT
    }
    return if (from.x - to.x < 0) {
      PeekDirection.RIGHT
    } else null
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (_blockTouch) {
      return super.onTouchEvent(event)
    }
    // this thing is probably making the UI laggy when swiping between ViewPager page
    // other alternative is by just disable all one finger gesture so it wonn't need to
    // execute unneeded code
    try {
      val simpleFingerGestures = _gestures
      simpleFingerGestures!!.onTouch(this, event)
    } catch (e: Exception) {
      e.printStackTrace()
      return super.onTouchEvent(event)
    }
    return super.onTouchEvent(event)
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return if (_blockTouch) {
      true
    } else super.onInterceptTouchEvent(ev)
  }

  fun init() {
    setWillNotDraw(false)
  }

  fun animateBackgroundShow() {
    _animateBackground = true
    invalidate()
  }

  fun animateBackgroundHide() {
    _animateBackground = false
    invalidate()
  }

  fun findFreeSpace(): Point? {
    for (y in 0 until _occupied!![0].size) {
      for (x in _occupied!!.indices) {
        if (!_occupied!![x][y]) {
          return Point(x, y)
        }
      }
    }
    return null
  }

  fun findFreeSpace(spanX: Int, spanY: Int): Point? {
    for (y in 0 until _occupied!![0].size) {
      for (x in _occupied!!.indices) {
        if (!_occupied!![x][y] && !checkOccupied(Point(x, y), spanX, spanY)) {
          return Point(x, y)
        }
      }
    }
    return null
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), _bgPaint)

    if (_cells == null)
      return

    val s = 7f
    for (x in 0 until cellSpanH) {
      for (y in 0 until cellSpanV) {
        if (x >= _cells!!.size || y >= _cells!![0].size)
          continue

        val cell = _cells!![x][y]!!

        canvas.save()
        canvas.rotate(45f, cell.left.toFloat(), cell.top.toFloat())
        canvas.drawRect(cell.left - s, cell.top - s, cell.left + s, cell.top + s, _paint)
        canvas.restore()

        canvas.save()
        canvas.rotate(45f, cell.left.toFloat(), cell.bottom.toFloat())
        canvas.drawRect(cell.left - s, cell.bottom - s, cell.left + s, cell.bottom + s, _paint)
        canvas.restore()

        canvas.save()
        canvas.rotate(45f, cell.right.toFloat(), cell.top.toFloat())
        canvas.drawRect(cell.right - s, cell.top - s, cell.right + s, cell.top + s, _paint)
        canvas.restore()

        canvas.save()
        canvas.rotate(45f, cell.right.toFloat(), cell.bottom.toFloat())
        canvas.drawRect(cell.right - s, cell.bottom - s, cell.right + s, cell.bottom + s, _paint)
        canvas.restore()
      }
    }

    //Animating alpha and drawing projected image
    val homeActivity = HomeActivity.launcher
    if (homeActivity != null && homeActivity.dragLayer.dragExceedThreshold &&
      _currentOutlineCoordinate.x != -1 && _currentOutlineCoordinate.y != -1) {
      if (_outlinePaint.alpha != 160)
        _outlinePaint.alpha = Math.min(_outlinePaint.alpha + 20, 160)

      drawCachedOutlineBitmap(canvas, _cells!![_currentOutlineCoordinate.x][_currentOutlineCoordinate.y])

      if (_outlinePaint.alpha <= 160)
        invalidate()
    }

    //Animating alpha
    if (_hideGrid && _paint.alpha != 0) {
      _paint.alpha = Math.max(_paint.alpha - 20, 0)
      invalidate()
    } else if (!_hideGrid && _paint.alpha != 255) {
      _paint.alpha = Math.min(_paint.alpha + 20, 255)
      invalidate()
    }

    //Animating alpha
    if (!_animateBackground && _bgPaint.alpha != 0) {
      _bgPaint.alpha = Math.max(_bgPaint.alpha - 10, 0)
      invalidate()
    } else if (_animateBackground && _bgPaint.alpha != 100) {
      _bgPaint.alpha = Math.min(_bgPaint.alpha + 10, 100)
      invalidate()
    }
  }

  override fun addView(view: View) {
    val lp = view.layoutParams as CellContainer.LayoutParams
    setOccupied(true, lp)
    super.addView(view)
  }

  override fun removeView(view: View) {
    val lp = view.layoutParams as CellContainer.LayoutParams
    setOccupied(false, lp)
    super.removeView(view)
  }

  fun addViewToGrid(view: View, x: Int, y: Int, xSpan: Int, ySpan: Int) {
    view.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, x, y, xSpan, ySpan)
    addView(view)
  }

  fun addViewToGrid(view: View) {
    addView(view)
  }

  fun setOccupied(b: Boolean, lp: LayoutParams) {
    val xSpan = lp.x + lp.xSpan
    for (x in lp.x until xSpan) {
      val ySpan = lp.y + lp.ySpan
      for (y in lp.y until ySpan) {
        _occupied!![x][y] = b
      }
    }
  }

  fun checkOccupied(start: Point, spanX: Int, spanY: Int): Boolean {
    var i = start.x + spanX
    if (i <= _occupied!!.size) {
      i = start.y + spanY
      if (i <= _occupied!![0].size) {
        val i2 = start.y + spanY
        i = start.y
        while (i < i2) {
          val i3 = start.x + spanX
          for (x in start.x until i3) {
            if (_occupied!![x][i]) {
              return true
            }
          }
          i++
        }
        return false
      }
    }
    return true
  }

  fun coordinateToChildView(pos: Point?): View? {
    if (pos == null) {
      return null
    }
    for (i in 0 until childCount) {
      val lp = getChildAt(i).layoutParams as LayoutParams
      if (pos.x >= lp.x && pos.y >= lp.y && pos.x < lp.x + lp.xSpan && pos.y < lp.y + lp.ySpan) {
        return getChildAt(i)
      }
    }
    return null
  }

  fun coordinateToLayoutParams(mX: Int, mY: Int, xSpan: Int, ySpan: Int): LayoutParams? {
    val pos = Point()
    touchPosToCoordinate(pos, mX, mY, xSpan, ySpan, true)
    return if (!pos.equals(-1, -1)) LayoutParams(WRAP_CONTENT, WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan) else null
  }

  @JvmOverloads
  fun touchPosToCoordinate(coordinate: Point, mX: Int, mY: Int, xSpan: Int, ySpan: Int, checkAvailability: Boolean, checkBoundary: Boolean = false) {
    var mX = mX
    var mY = mY
    if (_cells == null) {
      coordinate.set(-1, -1)
      return
    }

    mX -= ((xSpan - 1) * cellWidth / 2f).toInt()
    mY -= ((ySpan - 1) * cellHeight / 2f).toInt()

    var x = 0
    while (x < cellSpanH) {
      var y = 0
      while (y < cellSpanV) {
        val cell = _cells!![x][y]!!
        if (mY >= cell.top && mY <= cell.bottom && mX >= cell.left && mX <= cell.right) {
          if (checkAvailability) {
            if (_occupied!![x][y]) {
              coordinate.set(-1, -1)
              return
            }

            var dx = x + xSpan - 1
            var dy = y + ySpan - 1

            if (dx >= cellSpanH - 1) {
              dx = cellSpanH - 1
              x = dx + 1 - xSpan
            }
            if (dy >= cellSpanV - 1) {
              dy = cellSpanV - 1
              y = dy + 1 - ySpan
            }

            for (x2 in x until x + xSpan) {
              for (y2 in y until y + ySpan) {
                if (_occupied!![x2][y2]) {
                  coordinate.set(-1, -1)
                  return
                }
              }
            }
          }
          if (checkBoundary) {
            val offsetCell = Rect(cell)
            val dp2 = Tool.dp2px(6f)
            offsetCell.inset(dp2, dp2)
            if (mY >= offsetCell.top && mY <= offsetCell.bottom && mX >= offsetCell.left && mX <= offsetCell.right) {
              coordinate.set(-1, -1)
              return
            }
          }
          coordinate.set(x, y)
          return
        }
        y++
      }
      x++
    }
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val width = r - l - paddingLeft - paddingRight
    val height = b - t - paddingTop - paddingBottom
    if (cellSpanH == 0) {
      cellSpanH = 1
    }
    if (cellSpanV == 0) {
      cellSpanV = 1
    }
    cellWidth = width / cellSpanH
    cellHeight = height / cellSpanV
    initCellInfo(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
    val count = childCount
    if (_cells != null) {
      var i = 0
      while (i < count) {
        val child = getChildAt(i)
        if (child.visibility != View.GONE) {
          val lp = child.layoutParams as LayoutParams
          child.measure(MeasureSpec.makeMeasureSpec(lp.xSpan * cellWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.ySpan * cellHeight, MeasureSpec.EXACTLY))
          val rectArr = _cells
          val upRect = rectArr!![lp.x][lp.y]!!
          var downRect: Rect? = _tempRect
          if (lp.x + lp.xSpan - 1 < cellSpanH && lp.y + lp.ySpan - 1 < cellSpanV) {
            val rectArr2 = _cells
            downRect = rectArr2!![lp.x + lp.xSpan - 1][lp.y + lp.ySpan - 1]
          }

          if (lp.xSpan == 1 && lp.ySpan == 1) {
            child.layout(upRect.left, upRect.top, upRect.right, upRect.bottom)
          } else if (lp.xSpan > 1 && lp.ySpan > 1) {
            child.layout(upRect.left, upRect.top, downRect!!.right, downRect.bottom)
          } else if (lp.xSpan > 1) {
            child.layout(upRect.left, upRect.top, downRect!!.right, upRect.bottom)
          } else if (lp.ySpan > 1) {
            child.layout(upRect.left, upRect.top, upRect.right, downRect!!.bottom)
          }
        }
        i++
      }
    }
  }

  private fun initCellInfo(l: Int, t: Int, r: Int, b: Int) {
    _cells = Array(cellSpanH) { arrayOfNulls<Rect>(cellSpanV) }

    var curLeft = l
    var curTop = t
    var curRight = l + cellWidth
    var curBottom = t + cellHeight

    for (i in 0 until cellSpanH) {
      if (i != 0) {
        curLeft += cellWidth
        curRight += cellWidth
      }
      for (j in 0 until cellSpanV) {
        if (j != 0) {
          curTop += cellHeight
          curBottom += cellHeight
        }

        val rect = Rect(curLeft, curTop, curRight, curBottom)
        _cells!![i][j] = rect
      }
      curTop = t
      curBottom = t + cellHeight
    }
  }
}

package com.fisma.trinity.widgets

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.ImageUtil


class AppWidgetResizeFrame(context: Context, val mDragLayer: DragLayer) : FrameLayout(context) {
  var mWidgetContainer: FrameLayout? = null
  var mWidgetView: WidgetView? = null
  var mLeftHandle: ResizeHandle? = null
  var mRightHandle: ResizeHandle? = null
  var mTopHandle: ResizeHandle? = null
  var mBottomHandle: ResizeHandle? = null


  var mResizeMode: Int = 0

  var mHandleLeftResize = false
  var mHandleRightResize = false
  var mHandleTopResize = false
  var mHandleBottomResize = false

  var mHandler = Handler()

  companion object {
    private const val TAG = "AppWidgetResizeFrame"
    private var mInstance: AppWidgetResizeFrame? = null
    private var mResizeHandlePaint: Paint? = null
    var mCurrentItem: Item? = null
    var mCurrentWidth = 0
    var mCurrentHeight = 0
    var mCurrentX = 0
    var mCurrentY = 0
    var mCurrentBitmap: Bitmap? = null
    var mCurrentPadding: Rect? = null
    var mMinWidth: Int = 0
    var mMinHeight: Int = 0

    @SuppressLint("SwitchIntDef")
    fun updateResizedWidget(widgetContainer: FrameLayout, item: Item) {

      val widgetView = widgetContainer.getChildAt(0) as WidgetView
      val widgetInfo = widgetView.appWidgetInfo
      val resizeMode = widgetInfo.resizeMode
      mInstance!!.mWidgetContainer = widgetContainer
      mInstance!!.mWidgetView = widgetView
      mInstance!!.mResizeMode = resizeMode

      val grid = HomeActivity.mWorkspaceGrid
      val leftItem = grid!!.getChildAt(item.x, item.y)
      val rightItem = grid.getChildAt(item.x + item.spanX - 1, item.y + item.spanY - 1)

      mCurrentItem = item
      mCurrentWidth = rightItem.right - leftItem.left
      mCurrentHeight = rightItem.bottom - leftItem.top
      mCurrentX = leftItem.left
      mCurrentY = leftItem.top

      val padding = AppWidgetHostView.getDefaultPaddingForWidget(mInstance!!.context, widgetInfo.provider, null)
      if (resizeMode != AppWidgetProviderInfo.RESIZE_NONE) { // don't bother to create the bitmap if the widget resize mode is none
        mCurrentBitmap = Bitmap.createBitmap(mCurrentWidth, mCurrentHeight, Bitmap.Config.ARGB_8888)
        mCurrentBitmap = ImageUtil.drawBorder(mCurrentBitmap!!, 10f, Color.parseColor("#5c6bc0"), 150, padding)
      } else {
        mCurrentBitmap = null
      }

      mCurrentPadding = padding
      mMinWidth = grid.getMinWidthForWidget(mInstance!!.context, widgetInfo)
      mMinHeight = grid.getMinHeightForWidget(mInstance!!.context, widgetInfo)

      when (resizeMode) {
        AppWidgetProviderInfo.RESIZE_HORIZONTAL -> {
          mInstance!!.updateLeftHandle(padding, true)
          mInstance!!.updateRightHandle(padding, true)
          mInstance!!.updateTopHandle(padding, false)
          mInstance!!.updateBottomHandle(padding, false)
        }
        AppWidgetProviderInfo.RESIZE_VERTICAL -> {
          mInstance!!.updateLeftHandle(padding, false)
          mInstance!!.updateRightHandle(padding, false)
          mInstance!!.updateTopHandle(padding, true)
          mInstance!!.updateBottomHandle(padding, true)
        }
        AppWidgetProviderInfo.RESIZE_BOTH -> {
          mInstance!!.updateRightHandle(padding, true)
          mInstance!!.updateLeftHandle(padding, true)
          mInstance!!.updateTopHandle(padding, true)
          mInstance!!.updateBottomHandle(padding, true)
        }
        AppWidgetProviderInfo.RESIZE_NONE -> {
          mInstance!!.updateLeftHandle(mCurrentPadding, false)
          mInstance!!.updateRightHandle(mCurrentPadding, false)
          mInstance!!.updateTopHandle(mCurrentPadding, false)
          mInstance!!.updateBottomHandle(mCurrentPadding, false)

        }
      }
      mInstance!!.invalidate()
    }

    fun showResizeFrame() {
      mInstance!!.visibility = View.VISIBLE
      mInstance!!.animate().alpha(1f).withEndAction {
        val action = Runnable {
          hideResizeFrame()
        }

        mInstance!!.mHandler.postDelayed(action, 3000)
      }
    }

    fun hideResizeFrame() {
      mInstance!!.animate().alpha(0f).withEndAction {
        mInstance!!.visibility = View.INVISIBLE
      }
    }
  }


  init {
    setWillNotDraw(false)
    mInstance = this
    setPadding(0, 0, 0, 0)

    mResizeHandlePaint = Paint()
    mResizeHandlePaint!!.color = Color.WHITE
    mResizeHandlePaint!!.style = Paint.Style.FILL
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    if (canvas != null) {
      canvas.save()

      if (mCurrentBitmap != null)
        canvas.drawBitmap(mCurrentBitmap!!, mCurrentX.toFloat(), mCurrentY.toFloat(), null)

      if (mLeftHandle != null) {
        canvas.drawBitmap(mLeftHandle!!.bitmap, mLeftHandle!!.left.toFloat(), mLeftHandle!!.top.toFloat(), null)
      }

      if (mRightHandle != null) {
        canvas.drawBitmap(mRightHandle!!.bitmap, mRightHandle!!.left.toFloat(), mRightHandle!!.top.toFloat(), null)
      }

      if (mTopHandle != null) {
        canvas.drawBitmap(mTopHandle!!.bitmap, mTopHandle!!.left.toFloat(), mTopHandle!!.top.toFloat(), null)
      }

      if (mBottomHandle != null) {
        canvas.drawBitmap(mBottomHandle!!.bitmap, mBottomHandle!!.left.toFloat(), mBottomHandle!!.top.toFloat(), null)
      }

      canvas.restore()
    }
  }

  fun resizeWidgetIfNeeded() {
    val workspace = HomeActivity.launcher.workspace
    val grid = HomeActivity.mWorkspaceGrid!!

    var cellParams = grid.getCellParams()
    val cellHalfHeight = cellParams.height / 2
    val cellHalfWidth = cellParams.width / 2


    var lp: CellContainer.LayoutParams? = null
    workspace.currentPage.setOccupied(false, mWidgetContainer!!.layoutParams as CellContainer.LayoutParams)
    if (mHandleLeftResize) {
      var x = mWidgetContainer!!.x

      lp = if (mCurrentX < x && (x - mCurrentX) >= cellHalfWidth) { // if current x is less than x, we assume that the user probably try to make the widget bigger
        // check if the coordinate is already occupied by other widget or shortcut
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x - 1, mCurrentItem!!.y), mCurrentItem!!.spanX + 1, mCurrentItem!!.spanY)) {
          mCurrentItem!!.x = mCurrentItem!!.x - 1
          mCurrentItem!!.spanX = mCurrentItem!!.spanX + 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else if (mCurrentX > x && (mCurrentX - x) >= cellHalfWidth) { //
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x + 1, mCurrentItem!!.y), mCurrentItem!!.spanX - 1, mCurrentItem!!.spanY)) {
          mCurrentItem!!.x = mCurrentItem!!.x + 1
          mCurrentItem!!.spanX = mCurrentItem!!.spanX - 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else null
    }

    if (mHandleRightResize) {

      val right = mWidgetContainer!!.x + mWidgetContainer!!.width
      val currentRight = mCurrentX + mCurrentWidth
      Log.d(TAG, "right=$right mCurrentX=$mCurrentX")
      lp = if (currentRight > right && (currentRight - right) >= cellHalfWidth) {
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y), mCurrentItem!!.spanX + 1, mCurrentItem!!.spanY)) {
          mCurrentItem!!.spanX = mCurrentItem!!.spanX + 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else if (currentRight < right && (right - currentRight) >= cellHalfWidth) { //
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y), mCurrentItem!!.spanX - 1, mCurrentItem!!.spanY)) {
          mCurrentItem!!.spanX = mCurrentItem!!.spanX - 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else null
    }

    if (mHandleTopResize) {
      val y = mWidgetContainer!!.y
      lp = if (mCurrentY < y && y - mCurrentY >= cellHalfHeight) {
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y - 1), mCurrentItem!!.spanX, mCurrentItem!!.spanY + 1)) {
          mCurrentItem!!.y = mCurrentItem!!.y - 1
          mCurrentItem!!.spanY = mCurrentItem!!.spanY + 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else if (mCurrentY > y && mCurrentY - y >= cellHalfHeight) {
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y + 1), mCurrentItem!!.spanX, mCurrentItem!!.spanY - 1)) {
          mCurrentItem!!.y = mCurrentItem!!.y + 1
          mCurrentItem!!.spanY = mCurrentItem!!.spanY - 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else null
    }

    if (mHandleBottomResize) {
      val bottom = mWidgetContainer!!.y + mWidgetContainer!!.height
      val currentBottom = mCurrentY + mCurrentHeight

      lp = if (currentBottom > bottom && (currentBottom - bottom) >= cellHalfHeight) {
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y), mCurrentItem!!.spanX, mCurrentItem!!.spanY + 1)) {
          mCurrentItem!!.spanY = mCurrentItem!!.spanY + 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else if (currentBottom < bottom && (bottom - currentBottom) >= cellHalfHeight) { //
        if (!workspace.currentPage.checkOccupied(Point(mCurrentItem!!.x, mCurrentItem!!.y), mCurrentItem!!.spanX, mCurrentItem!!.spanY - 1)) {
          mCurrentItem!!.spanY = mCurrentItem!!.spanY - 1
          CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT,
            CellContainer.LayoutParams.WRAP_CONTENT, mCurrentItem!!.x, mCurrentItem!!.y, mCurrentItem!!.spanX, mCurrentItem!!.spanY)
        } else null
      } else null
    }

    if (lp != null) {
      mWidgetContainer!!.layoutParams = lp
    } else {
      workspace.currentPage.setOccupied(true, mWidgetContainer!!.layoutParams as CellContainer.LayoutParams)
    }
  }

  private fun snapToWidget() {
    val x = mWidgetContainer!!.x
    val y = mWidgetContainer!!.y
    val width = mWidgetContainer!!.width
    val height = mWidgetContainer!!.height

    if (mCurrentX.toFloat() != x) {
      mCurrentX = x.toInt()
    } else if (mCurrentY.toFloat() != y) {
      mCurrentY = y.toInt()
    }

    if (mCurrentWidth != width) {
      mCurrentWidth = width
    } else if (mCurrentHeight != height) {
      mCurrentHeight = height
    }

    when (mResizeMode) {
      AppWidgetProviderInfo.RESIZE_HORIZONTAL -> {
        mInstance!!.updateLeftHandle(mCurrentPadding, true)
        mInstance!!.updateRightHandle(mCurrentPadding, true)
        mInstance!!.updateTopHandle(mCurrentPadding, false)
        mInstance!!.updateBottomHandle(mCurrentPadding, false)
      }
      AppWidgetProviderInfo.RESIZE_VERTICAL -> {
        mInstance!!.updateLeftHandle(mCurrentPadding, false)
        mInstance!!.updateRightHandle(mCurrentPadding, false)
        mInstance!!.updateTopHandle(mCurrentPadding, true)
        mInstance!!.updateBottomHandle(mCurrentPadding, true)
      }
      AppWidgetProviderInfo.RESIZE_BOTH -> {
        mInstance!!.updateRightHandle(mCurrentPadding, true)
        mInstance!!.updateLeftHandle(mCurrentPadding, true)
        mInstance!!.updateTopHandle(mCurrentPadding, true)
        mInstance!!.updateBottomHandle(mCurrentPadding, true)
      }
      AppWidgetProviderInfo.RESIZE_NONE -> {
        mInstance!!.updateLeftHandle(mCurrentPadding, false)
        mInstance!!.updateRightHandle(mCurrentPadding, false)
        mInstance!!.updateTopHandle(mCurrentPadding, false)
        mInstance!!.updateBottomHandle(mCurrentPadding, false)
      }
    }

    mCurrentBitmap = Bitmap.createBitmap(mCurrentWidth, mCurrentHeight, Bitmap.Config.ARGB_8888)
    mCurrentBitmap = ImageUtil.drawBorder(mCurrentBitmap!!, 10f, Color.parseColor("#5c6bc0"), 150, mCurrentPadding)

    invalidate()
  }

  fun shouldInterceptTouchEvent(ev: MotionEvent?): Boolean {
    var shoulIntercept = super.onInterceptTouchEvent(ev)
    if (ev != null) {
      when (ev.action) {
        MotionEvent.ACTION_DOWN -> {
          if (mLeftHandle != null) {
            if (ev.x >= mLeftHandle!!.left && ev.x <= mLeftHandle!!.right && ev.y >= mLeftHandle!!.top && ev.y <= mLeftHandle!!.bottom) {
              Log.d(TAG, "handle left resize")
              mHandler.removeCallbacksAndMessages(null)
              shoulIntercept = true
              mHandleLeftResize = true
            }
          }

          if (mRightHandle != null) {
            if (ev.x >= mRightHandle!!.left && ev.x <= mRightHandle!!.right && ev.y >= mRightHandle!!.top && ev.y <= mRightHandle!!.bottom) {
              Log.d(TAG, "handle right resize")
              mHandler.removeCallbacksAndMessages(null)
              shoulIntercept = true
              mHandleRightResize = true
            }
          }

          if (mTopHandle != null) {
            if (ev.x >= mTopHandle!!.left && ev.x <= mTopHandle!!.right && ev.y >= mTopHandle!!.top && ev.y <= mTopHandle!!.bottom) {
              Log.d(TAG, "handle top resize")
              mHandler.removeCallbacksAndMessages(null)
              shoulIntercept = true
              mHandleTopResize = true
            }
          }

          if (mBottomHandle != null) {
            if (ev.x >= mBottomHandle!!.left && ev.x <= mBottomHandle!!.right && ev.y >= mBottomHandle!!.top && ev.y <= mBottomHandle!!.bottom) {
              Log.d(TAG, "handle top resize")
              mHandler.removeCallbacksAndMessages(null)
              shoulIntercept = true
              mHandleBottomResize = true
            }
          }
        }
      }
    }
    return shoulIntercept
  }

  fun onTouchEnd() {
    mHandleLeftResize = false
    mHandleRightResize = false
    mHandleTopResize = false
    mHandleBottomResize = false

    val action = Runnable {
      hideResizeFrame()
    }

    mHandler.postDelayed(action, 3000)
    snapToWidget()
  }

  fun onTouchMove(event: MotionEvent?) {
    if (event != null) {
      if (mHandleLeftResize) {
        val right = mCurrentX + mCurrentWidth
        val currentWidth = right - event.x.toInt()
        mCurrentX = if (currentWidth >= mMinWidth) {
          event.x.toInt()
        } else {
          right - mMinWidth
        }

        mCurrentWidth = right - mCurrentX

        updateLeftHandle(mCurrentPadding, true)

        if (mResizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
          updateTopHandle(mCurrentPadding!!, true)
          updateBottomHandle(mCurrentPadding!!, true)
        }
      }

      if (mHandleRightResize) {
        val left = mCurrentX
        val currentWidth = event.x.toInt() - left
        mCurrentWidth = if (currentWidth >= mMinWidth) {
          currentWidth
        } else {
          mMinWidth
        }
        updateRightHandle(mCurrentPadding!!, true)

        if (mResizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
          updateTopHandle(mCurrentPadding!!, true)
          updateBottomHandle(mCurrentPadding!!, true)
        }
      }

      if (mHandleTopResize) {
        val bottom = mCurrentY + mCurrentHeight
        val currentHeight = bottom - event.y
        mCurrentY = if (currentHeight >= mMinHeight) {
          event.y.toInt()
        } else {
          bottom - mMinHeight
        }

        mCurrentHeight = bottom - mCurrentY
        updateTopHandle(mCurrentPadding!!, true)

        if (mResizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
          updateLeftHandle(mCurrentPadding!!, true)
          updateRightHandle(mCurrentPadding!!, true)
        }
      }

      if (mHandleBottomResize) {
        val top = mCurrentY
        val currentHeight = event.y.toInt() - top
        mCurrentHeight = if (currentHeight >= mMinHeight) {
          currentHeight
        } else {
          mMinHeight
        }

        updateBottomHandle(mCurrentPadding!!, true)

        if (mResizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
          updateLeftHandle(mCurrentPadding!!, true)
          updateRightHandle(mCurrentPadding!!, true)
        }
      }

      mCurrentBitmap = Bitmap.createBitmap(mCurrentWidth, mCurrentHeight, Bitmap.Config.ARGB_8888)
      mCurrentBitmap = ImageUtil.drawBorder(mCurrentBitmap!!, 10f, Color.parseColor("#5c6bc0"), 150, mCurrentPadding)
    }
    resizeWidgetIfNeeded()
    invalidate()
  }

  fun updateLeftHandle(padding: Rect? = Rect(), show: Boolean) {
    if (show) {
      val handle = ResizeHandle()
      handle.cx = (mCurrentX + padding!!.left).toFloat()
      handle.cy = (mCurrentY + (mCurrentHeight / 2)).toFloat()
      handle.left = handle.cx.toInt() - (handle.mHandleWidth / 2)
      handle.right = handle.left + handle.mHandleWidth
      handle.top = handle.cy.toInt() - (handle.mHandleHeight / 2)
      handle.bottom = handle.top + handle.mHandleHeight
      mLeftHandle = handle
    } else {
      mLeftHandle = null
    }
  }

  fun updateRightHandle(padding: Rect? = Rect(), show: Boolean) {
    if (show) {
      val handle = ResizeHandle()
      handle.cx = (mCurrentX + mCurrentWidth - padding!!.right).toFloat()
      handle.cy = (mCurrentY + (mCurrentHeight / 2)).toFloat()
      handle.left = handle.cx.toInt() - (handle.mHandleWidth / 2)
      handle.right = handle.left + handle.mHandleWidth
      handle.top = handle.cy.toInt() - (handle.mHandleHeight / 2)
      handle.bottom = handle.top + handle.mHandleHeight

      mRightHandle = handle
    } else {
      mRightHandle = null
    }
  }

  fun updateTopHandle(padding: Rect? = Rect(), show: Boolean) {
    if (show) {
      val handle = ResizeHandle(true)
      handle.cx = (mCurrentX + (mCurrentWidth / 2)).toFloat()
      handle.cy = (mCurrentY + padding!!.top).toFloat()
      handle.left = handle.cx.toInt() - (handle.mHandleWidth / 2)
      handle.right = handle.left + (handle.mHandleWidth)
      handle.top = handle.cy.toInt() - (handle.mHandleHeight / 2)
      handle.bottom = handle.top + handle.mHandleHeight

      mTopHandle = handle
    } else {
      mTopHandle = null
    }
  }

  fun updateBottomHandle(padding: Rect? = Rect(), show: Boolean) {
    if (show) {
      val handle = ResizeHandle(true)
      handle.cx = (mCurrentX + (mCurrentWidth / 2)).toFloat()
      handle.cy = (mCurrentY + mCurrentHeight - padding!!.bottom).toFloat()
      handle.left = handle.cx.toInt() - (handle.mHandleWidth / 2)
      handle.right = handle.left + (handle.mHandleWidth)
      handle.top = handle.cy.toInt() - (handle.mHandleHeight / 2)
      handle.bottom = handle.top + handle.mHandleHeight

      mBottomHandle = handle
    } else {
      mBottomHandle = null
    }
  }

  class ResizeHandle {
    var cx = 0f
    var cy = 0f
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0
    var bitmap: Bitmap? = null
    var mHandleHeight = 100
    var mHandleWidth = 32
    var mCurveSize = 20f
    var isVerticalHandler = false
      set(value) {
        if (field != value) {
          field = value
          if (value) {
            mHandleHeight = 32
            mHandleWidth = 100
          } else {
            mHandleHeight = 100
            mHandleWidth = 32
          }
          updateHandleSize()
        }
      }

    constructor(verticalHandler: Boolean = false) {
      this.isVerticalHandler = verticalHandler
      if (!verticalHandler) {
        updateHandleSize()
      }
    }

    fun updateHandleSize() {

      val halfHeight = mCurrentHeight / 2
      val halfWidth = mCurrentWidth / 2
      if (isVerticalHandler) {
        if (mHandleWidth > halfWidth) {
          mHandleWidth = halfWidth
        }
        bitmap = Bitmap.createBitmap(mHandleWidth, mHandleHeight, Bitmap.Config.ARGB_8888)
        val handleWidth = mHandleWidth.toFloat()
        val handleHeight = mHandleHeight.toFloat()
        val handleHalfHeight = handleHeight / 2

        val canvas = Canvas(bitmap)
        val path = Path()
        path.moveTo(mCurveSize, 0f)
        path.lineTo(handleWidth - mCurveSize, 0f)
        path.quadTo(handleWidth, 0f, handleWidth, handleHalfHeight)
        path.quadTo(handleWidth, handleHeight, handleWidth - mCurveSize, handleHeight)
        path.lineTo(mCurveSize, handleHeight)
        path.quadTo(0f, handleHeight, 0f, handleHalfHeight)
        path.quadTo(0f, 0f, mCurveSize, 0f)
        path.close()
        canvas.drawPath(path, mResizeHandlePaint)

      } else {
        if (mHandleHeight > halfHeight) {
          mHandleHeight = halfHeight
        }
        bitmap = Bitmap.createBitmap(mHandleWidth, mHandleHeight, Bitmap.Config.ARGB_8888)

        val handleWidth = mHandleWidth.toFloat()
        val handleHeight = mHandleHeight.toFloat()
        val handleHalfWidth = handleWidth / 2

        val canvas = Canvas(bitmap)
        val path = Path()
        path.moveTo(0f, mCurveSize)
        path.quadTo(0f, 0f, handleHalfWidth, 0f)
        path.quadTo(handleWidth, 0f, handleWidth, mCurveSize)
        path.lineTo(handleWidth, handleHeight - mCurveSize)
        path.quadTo(handleWidth, handleHeight, handleHalfWidth, handleHeight)
        path.quadTo(0f, handleHeight, 0f, handleHeight - mCurveSize)
        path.lineTo(0f, mCurveSize)
        path.close()
        canvas.drawPath(path, mResizeHandlePaint)
      }
    }
  }

}
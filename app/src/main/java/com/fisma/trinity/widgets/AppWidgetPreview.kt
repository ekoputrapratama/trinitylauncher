package com.fisma.trinity.widgets

import android.view.MotionEvent
import android.widget.TextView
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import com.fisma.trinity.compat.AppWidgetManagerCompat
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.fisma.trinity.graphics.FastBitmapDrawable
import com.fisma.trinity.R
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.model.Item


/**
 * The linear layout used strictly for the widget/wallpaper tab of the customization tray
 */
class AppWidgetPreview(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

  private val mDimensionsFormatString: String
  internal var mPendingCheckForShortPress: CheckForShortPress? = null
  internal var mShortPressListener: ShortPressListener? = null
  internal var mShortPressTriggered = false
  internal var mIsAppWidget: Boolean = false
  private val mOriginalImagePadding = Rect()
  private var mInfo: Any? = null
  private val mManager: AppWidgetManagerCompat = AppWidgetManagerCompat.getInstance(context)

  val previewSize: IntArray
    get() {
      val i = findViewById<View>(R.id.widget_preview) as ImageView
      val maxSize = IntArray(2)
      maxSize[0] = i.width - mOriginalImagePadding.left - mOriginalImagePadding.right
      maxSize[1] = i.height - mOriginalImagePadding.top
      return maxSize
    }

  init {

    val r = context.resources
    mDimensionsFormatString = r.getString(R.string.widget_dims_format)

    setWillNotDraw(false)
    clipToPadding = false
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    val image = findViewById<View>(R.id.widget_preview) as ImageView
    mOriginalImagePadding.left = image.paddingLeft
    mOriginalImagePadding.top = image.paddingTop
    mOriginalImagePadding.right = image.paddingRight
    mOriginalImagePadding.bottom = image.paddingBottom

    // Ensure we are using the right text size
//        val app = LauncherAppState.getInstance()
//        val grid = app.getDynamicGrid().getDeviceProfile()
//        val name = findViewById<View>(R.id.widget_name) as TextView
//        name?.setTextSize(TypedValue.COMPLEX_UNIT_PX, grid.iconTextSizePx)
//        val dims = findViewById<View>(R.id.widget_dims) as TextView
//        dims?.setTextSize(TypedValue.COMPLEX_UNIT_PX, grid.iconTextSizePx)
  }

//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//
//        if (sDeletePreviewsWhenDetachedFromWindow) {
//            val image = findViewById<View>(R.id.widget_preview) as ImageView
//            if (image != null) {
//                image.setImageDrawable(null)
//            }
//        }
//    }

  fun applyFromItem(item: AppWidget, maxWidth: Int) {
    Log.d("AppWidgetPreview", "applyFromItem")
    mIsAppWidget = true
    val image = findViewById<View>(R.id.widget_preview) as ImageView
    val name = findViewById<View>(R.id.widget_name) as TextView
    val dims = findViewById<View>(R.id.widget_dims) as TextView

    name.text = item.label
    if (dims != null) {
      val hSpan = item.spanX
      val vSpan = item.spanY
      dims.text = String.format(mDimensionsFormatString, hSpan, vSpan)
    }

    if (item.previewImage != null) {
      var drawable = FastBitmapDrawable(item.previewImage!!)
      image.setImageDrawable(drawable)
      image.alpha = 1f
    }
  }

  fun applyFromAppWidgetProviderInfo(info: AppWidgetProviderInfo,
                                     maxWidth: Int, cellSpan: List<Int>) {
//        val app = LauncherAppState.getInstance()
//        val grid = app.getDynamicGrid().getDeviceProfile()

    mIsAppWidget = true
    mInfo = info
    val image = findViewById<View>(R.id.widget_preview) as ImageView
    if (maxWidth > -1) {
      image.maxWidth = maxWidth
    }
    val name = findViewById<View>(R.id.widget_name) as TextView
    name.text = AppWidgetManagerCompat.getInstance(context).loadLabel(info)
    val dims = findViewById<View>(R.id.widget_dims) as TextView
    if (dims != null) {
      val hSpan = Math.min(cellSpan[0], 2)
      val vSpan = Math.min(cellSpan[1], 2)
      dims.text = String.format(mDimensionsFormatString, hSpan, vSpan)
    }

    var drawable: Drawable? = null
    if (info.previewImage != 0) {
      drawable = mManager.loadPreview(info)
      if (drawable != null) {
        drawable = drawable.mutate()
      } else {
//                Log.w(TAG, "Can't load widget preview drawable 0x" +
//                        Integer.toHexString(info.previewImage) + " for provider: " + info.provider)
      }
    }

    var previewWidth: Int
    var previewHeight: Int
    var preview: Bitmap? = null
    val widgetPreviewExists = drawable != null

    if (widgetPreviewExists) {
      previewWidth = drawable!!.intrinsicWidth
      previewHeight = drawable.intrinsicHeight

      // If a bitmap is passed in, we use it; otherwise, we create a bitmap of the right size
      if (preview == null) {
        preview = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
      }

      // Draw the scaled preview into the final bitmap
      val x = (preview!!.width - previewWidth) / 2

      renderDrawableToBitmap(drawable, preview, x, 0, previewWidth,
        previewHeight)

      var bitmap = mManager.getBadgeBitmap(info, preview)

      image.setImageBitmap(bitmap)
    }


  }

  private fun renderDrawableToBitmap(d: Drawable?, bitmap: Bitmap?, x: Int, y: Int, w: Int, h: Int) {
    if (bitmap != null) {
      val c = Canvas(bitmap)
      val oldBounds = d!!.copyBounds()
      d.setBounds(x, y, x + w, y + h)
      d.draw(c)
      d.bounds = oldBounds // Restore the bounds
      c.setBitmap(null)
    }
  }

  fun applyFromResolveInfo(
    pm: PackageManager, info: ResolveInfo) {
    mIsAppWidget = false
    mInfo = info
    val label = info.loadLabel(pm)
    val name = findViewById<View>(R.id.widget_name) as TextView
    name.text = label
    val dims = findViewById<View>(R.id.widget_dims) as TextView
    if (dims != null) {
      dims.text = String.format(mDimensionsFormatString, 1, 1)
    }
  }

  internal fun applyPreview(preview: FastBitmapDrawable?, index: Int) {
    val image = findViewById<View>(R.id.widget_preview) as AppWidgetImageView
    if (preview != null) {
      image.mAllowRequestLayout = false
//            Glide.with(context)
//                    .asBitmap()
//                    .load(preview)
//                    .into(image)


      image.setImageDrawable(preview)
      if (mIsAppWidget) {
        // center horizontally
        val imageSize = previewSize
        val centerAmount = (imageSize[0] - preview.intrinsicWidth) / 2
//                image.setPadding(mOriginalImagePadding.left + centerAmount,
//                        mOriginalImagePadding.top,
//                        mOriginalImagePadding.right,
//                        mOriginalImagePadding.bottom)
      }
      image.alpha = 1f
      image.mAllowRequestLayout = true
    }
  }

  internal fun setShortPressListener(listener: ShortPressListener) {
    mShortPressListener = listener
  }

  internal interface ShortPressListener {
    fun onShortPress(v: View)
    fun cleanUpShortPress(v: View)
  }

  internal inner class CheckForShortPress : Runnable {
    override fun run() {
      if (sShortpressTarget != null) {
        return
      }
      if (mShortPressListener != null) {
        mShortPressListener!!.onShortPress(this@AppWidgetPreview)
        sShortpressTarget = this@AppWidgetPreview
      }
      mShortPressTriggered = true
    }
  }

  private fun checkForShortPress() {
    if (sShortpressTarget != null) {
      return
    }
    if (mPendingCheckForShortPress == null) {
      mPendingCheckForShortPress = CheckForShortPress()
    }
    postDelayed(mPendingCheckForShortPress, 120)
  }

  /**
   * Remove the longpress detection timer.
   */
  private fun removeShortPressCallback() {
    if (mPendingCheckForShortPress != null) {
      removeCallbacks(mPendingCheckForShortPress)
    }
  }

  private fun cleanUpShortPress() {
    removeShortPressCallback()
    if (mShortPressTriggered) {
      if (mShortPressListener != null) {
        mShortPressListener!!.cleanUpShortPress(this@AppWidgetPreview)
      }
      mShortPressTriggered = false
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    super.onTouchEvent(event)

    when (event.action) {
      MotionEvent.ACTION_UP -> cleanUpShortPress()
      MotionEvent.ACTION_DOWN -> checkForShortPress()
      MotionEvent.ACTION_CANCEL -> cleanUpShortPress()
      MotionEvent.ACTION_MOVE -> {
      }
    }

    // We eat up the touch events here, since the PagedView (which uses the same swiping
    // touch code as Workspace previously) uses onInterceptTouchEvent() to determine when
    // the user is scrolling between pages.  This means that if the pages themselves don't
    // handle touch events, it gets forwarded up to PagedView itself, and it's own
    // onTouchEvent() handling will prevent further intercept touch events from being called
    // (it's the same view in that case).  This is not ideal, but to prevent more changes,
    // we just always mark the touch event as handled.
    return true
  }

  companion object {
    internal val TAG = "PagedViewWidgetLayout"

    private var sDeletePreviewsWhenDetachedFromWindow = true
    private var sRecyclePreviewsWhenDetachedFromWindow = true
    internal var sShortpressTarget: AppWidgetPreview? = null

    fun setDeletePreviewsWhenDetachedFromWindow(value: Boolean) {
      sDeletePreviewsWhenDetachedFromWindow = value
    }

    fun setRecyclePreviewsWhenDetachedFromWindow(value: Boolean) {
      sRecyclePreviewsWhenDetachedFromWindow = value
    }

    internal fun resetShortPressTarget() {
      sShortpressTarget = null
    }
  }
}
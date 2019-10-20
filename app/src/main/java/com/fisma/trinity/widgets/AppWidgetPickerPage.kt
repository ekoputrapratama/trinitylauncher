package com.fisma.trinity.widgets

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.R
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.ItemViewFactory
import java.util.ArrayList
import com.fisma.trinity.compat.AppWidgetManagerCompat
import android.appwidget.AppWidgetProviderInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.interfaces.AppUpdateListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.App
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.util.DynamicGrid
import com.fisma.trinity.util.ImageUtil


class AppWidgetPickerPage : ViewPager {
  var _widgets: ArrayList<AppWidget> = ArrayList()

  var _pages: ArrayList<ViewGroup> = ArrayList()

  private var _pageCount = 0
  private var _task: AsyncTask<*, *, *>? = null
  private var mWidgetPickerGrid: DynamicGrid? = null

  constructor(context: Context, attr: AttributeSet) : super(context, attr) {
    init(context)
  }

  constructor(context: Context) : super(context) {
    init(context)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    if (_widgets == null) {
      super.onConfigurationChanged(newConfig)
      return
    }

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      setLandscapeValue()
      calculatePage()
      adapter = Adapter()
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      setPortraitValue()
      calculatePage()
      adapter = Adapter()
    }
    super.onConfigurationChanged(newConfig)
  }

  private fun setPortraitValue() {
    _columnCellCount = 2
    _rowCellCount = 2
  }

  private fun setLandscapeValue() {
    _columnCellCount = 1
    _rowCellCount = 4
  }

  private fun calculatePage() {
    Log.d("AppWidgetPickerPage", "calculatePages()")
    _pageCount = 0
    var widgetsSize = _widgets.size
    var size = widgetsSize - _rowCellCount * _columnCellCount
    while (size >= _rowCellCount * _columnCellCount) {
      _pageCount++
      size -= (_rowCellCount * _columnCellCount)
    }
  }

  private fun init(c: Context) {
    val margin = Tool.dp2px(14f)
    mWidgetPickerGrid = HomeActivity.mWorkspaceGrid!!.clone("AppWidgetPicker")
      .setMargin(DynamicGrid.Margin(margin, margin, margin, margin))
    overScrollMode = View.OVER_SCROLL_NEVER

    val mPortrait = c.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    if (mPortrait) {
      setPortraitValue()
    } else {
      setLandscapeValue()
    }

    Settings.appLoader().addUpdateListener(object : AppUpdateListener {
      override fun onAppUpdated(apps: List<App>): Boolean {
        if (_task == null || _task!!.status == AsyncTask.Status.FINISHED)
          _task = AsyncGetWidgets().execute()
        else if (_task!!.status == AsyncTask.Status.RUNNING) {
          _task!!.cancel(false)
          _task = AsyncGetWidgets().execute()
        }
        return false
      }
    })
    val grid = HomeActivity.mWorkspaceGrid!!
    grid.addGridChangeListener(object : DynamicGrid.DynamicGridChangeListener {
      override fun onGridChange(width: Int, height: Int, cellWidth: Int, cellHeight: Int) {
        if (_task != null && _task!!.status != AsyncTask.Status.FINISHED) {
          _task!!.cancel(true)
        }
        _task = AsyncGetWidgets().execute()
      }
    })
    _task = AsyncGetWidgets().execute()
  }

  fun getWidgetPreview(info: AppWidgetProviderInfo): Bitmap? {
    var drawable: Drawable? = null
    val mManager = HomeActivity.mAppWidgetManager
    var preview: Bitmap? = null

    if (info.previewImage != 0) {
      drawable = mManager.loadPreview(info)
      if (drawable != null) {
        drawable = drawable.mutate()
      } else {
        Log.w("AppWidgetPickerPage", "Can't load widget preview drawable 0x" +
          Integer.toHexString(info.previewImage) + " for provider: " + info.provider)
      }
    } else if (info.icon != 0 && Build.VERSION.SDK_INT >= 21) {
      drawable = info.loadIcon(context, resources.displayMetrics.density.toInt())
      if (drawable != null)
        drawable = drawable.mutate()
    }


    val widgetPreviewExists = drawable != null
    val maxPreviewWidth = mWidgetPickerGrid!!.getParams().width / 2

    if (widgetPreviewExists) {
      var previewWidth = drawable!!.intrinsicWidth
      var previewHeight = drawable.intrinsicHeight

      var scale = 1f
      if (previewWidth > maxPreviewWidth) {
        scale = maxPreviewWidth.toFloat() / previewWidth.toFloat()
      }

      if (scale != 1f) {
        previewWidth = (scale * previewWidth).toInt()
        previewHeight = (scale * previewHeight).toInt()
      }
      // If a bitmap is passed in, we use it; otherwise, we create a bitmap of the right size
      if (preview == null) {
        preview = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
      }

      // Draw the scaled preview into the final bitmap
      val x = (preview!!.width - previewWidth) / 2

      ImageUtil.renderDrawableToBitmap(drawable, preview, x, 0, previewWidth,
        previewHeight)

      return mManager.getBadgeBitmap(info, preview)
    }

    return null
  }

  fun getWidgetProjectionImage(info: AppWidgetProviderInfo, minWidth: Int, minHeight: Int): Bitmap? {
    var drawable: Drawable? = null
    val mManager = HomeActivity.mAppWidgetManager

    if (info.previewImage != 0) {
      drawable = mManager.loadPreview(info)
      if (drawable != null) {
        drawable = drawable.mutate()
      } else {
        Log.w("AppWidgetPickerPage", "Can't load widget preview drawable 0x" +
          Integer.toHexString(info.previewImage) + " for provider: " + info.provider)
      }
    }

    val widgetPreviewExists = drawable != null
    var originalBitmap: Bitmap? = null
    val maxPreviewWidth = minWidth

    if (widgetPreviewExists) {
      var previewWidth = drawable!!.intrinsicWidth
      var previewHeight = drawable.intrinsicHeight

      val scale = maxPreviewWidth.toFloat() / previewWidth.toFloat()

      if (scale != 1f) {
        previewWidth = (scale * previewWidth).toInt()
        previewHeight = (scale * previewHeight).toInt()
      }
      // Draw the scaled preview into the final bitmap
      originalBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)

      val x = (originalBitmap!!.width - previewWidth) / 2
      val y = (originalBitmap.height - previewHeight) / 2
      ImageUtil.renderDrawableToBitmap(drawable, originalBitmap, x, y, previewWidth, previewHeight, 75)

      // if scaled preview height is less than minimum height add top and bottom padding so the image is centered vertically
      if (previewHeight < minHeight) {
        val paddingY = (minHeight - previewHeight)
        originalBitmap = ImageUtil.addPadding(originalBitmap, 0f, paddingY.toFloat())
      } else if (previewHeight > minHeight) {
        originalBitmap = ImageUtil.resizeImage(resources, originalBitmap, previewWidth, minHeight)
      }

      return originalBitmap
    }

    originalBitmap = Bitmap.createBitmap(minWidth, minHeight, Bitmap.Config.ARGB_8888)

    val c = Canvas(originalBitmap!!)
    val paint = Paint()
    paint.strokeWidth = 15f
    paint.color = Color.WHITE
    paint.alpha = 150

    c.drawLine(0f, 0f, minWidth.toFloat(), 0f, paint)
    c.drawLine(minWidth.toFloat(), 0f, minWidth.toFloat(), minHeight.toFloat(), paint)
    c.drawLine(minWidth.toFloat(), minHeight.toFloat(), 0f, minHeight.toFloat(), paint)
    c.drawLine(0f, minHeight.toFloat(), 0f, 0f, paint)
    c.drawBitmap(originalBitmap, 0f, 0f, null)
    c.setBitmap(null)
    return originalBitmap
  }

  inner class Adapter : PagerAdapter() {

    private fun getItemView(page: Int, x: Int, y: Int): View? {
      val pagePos = y * _columnCellCount + x
      val pos = _rowCellCount * _columnCellCount * page + pagePos

      if (pos >= _widgets.size)
        return null

      val item = _widgets[pos]
      val view = ItemViewFactory.getItemView(context, null, DragAction.Action.WIDGET_PREVIEW, item) as AppWidgetPreview

      view.applyFromItem(item)
      view.tag = item
      return view
    }

    init {
      _pages.clear()
      for (i in 0 until _pageCount) {
        val layout = LayoutInflater.from(context).inflate(R.layout.view_widget_picker_page_inner, null) as ViewGroup
        (layout.getChildAt(0) as CardView).setCardBackgroundColor(Color.parseColor("#FF3C3F41"))
        (layout.getChildAt(0) as CardView).cardElevation = Tool.dp2px(4f).toFloat()
        val cc = layout.findViewById<CellContainer>(R.id.group)
        cc.setGridSize(_columnCellCount, _rowCellCount)

        for (x in 0 until _columnCellCount) {
          for (y in 0 until _rowCellCount) {
            val view = getItemView(i, x, y)
            if (view != null) {
              val lp = CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1)
              view.layoutParams = lp
              cc.addViewToGrid(view)
            }
          }
        }
        _pages.add(layout)
      }
    }

    override fun getCount(): Int {
      return _pageCount
    }

    override fun isViewFromObject(p1: View, p2: Any): Boolean {
      return p1 === p2
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
      container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
      val index = _pages.indexOf(`object`)
      return if (index == -1)
        POSITION_NONE
      else
        index
    }

    override fun instantiateItem(container: ViewGroup, pos: Int): Any {
      val layout = _pages[pos]
      container.addView(layout)
      return layout
    }
  }

  private inner class AsyncGetWidgets : AsyncTask<Any, Any, Any>() {
    private var newWidgets: ArrayList<AppWidget>? = null

    override fun onPreExecute() {
      _widgets.clear()
      newWidgets = ArrayList()
      super.onPreExecute()
    }

    override fun onCancelled() {
      newWidgets = null
      super.onCancelled()
    }

    override fun doInBackground(vararg p0: Any?): Any? {
      val mAppWidgetManager = HomeActivity.mAppWidgetManager
      val providers = ArrayList<AppWidgetProviderInfo>()
      providers.addAll(mAppWidgetManager.allProviders)
      val grid = HomeActivity.mWorkspaceGrid!!
      for (info in providers) {

        val widget = AppWidget()
        widget.type = Item.Type.APPWIDGET
        widget.label = info.label
        widget.packageName = info.provider.packageName
        widget.className = info.provider.className
        widget.widgetInfo = info

        val spanXY = grid.getSpanForWidget(context, info)
        val minSpanXY = grid.getMinSpanForWidget(context, info)
        val minWidth = grid.getMinWidthForWidget(context, info)
        val minHeight = grid.getMinHeightForWidget(context, info)

        widget.minWidth = minWidth
        widget.minHeight = minHeight
        widget.spanX = spanXY[0]
        widget.spanY = spanXY[1]
        widget.minSpanX = minSpanXY[0]
        widget.minSpanY = minSpanXY[1]
        widget.projectionImage = getWidgetProjectionImage(info, minWidth, minHeight)
        widget.previewImage = getWidgetPreview(info)

        newWidgets!!.add(widget)
      }
      return null
    }

    override fun onPostExecute(result: Any?) {
      HomeActivity.launcher.runOnUiThread {
        if (newWidgets != null && newWidgets!!.isNotEmpty()) {
          newWidgets!!.sortBy { s -> s.label }
          _widgets = newWidgets!!
          this@AppWidgetPickerPage.calculatePage()
          adapter = Adapter()
        }
      }
      super.onPostExecute(result)
    }
  }

  companion object {
    private var _columnCellCount: Int = 0
    private var _rowCellCount: Int = 0
  }
}

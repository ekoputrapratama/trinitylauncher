package com.fisma.trinity.util

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.graphics.Rect
import com.fisma.trinity.manager.Settings
import java.util.ArrayList
import android.graphics.Point


class DynamicGrid {
  companion object {
    const val TAG = "DynamicGrid"
    @SuppressLint("StaticFieldLeak")
    var mInstances: ArrayList<DynamicGrid> = ArrayList()
    var mScreenWidth: Int = 0
    var mScreenHeight: Int = 0
    var navBarHeight: Int = 0
      set(value) {
        if (value != field) {
          field = value
          for (instance in mInstances) {
            instance.update()
          }

        }
      }
    var statusBarHeight: Int = 0
      set(value) {
        if (value != field) {
          field = value
          for (instance in mInstances) {
            instance.update()
          }
        }
      }
  }

  private var mGridChangeListeners: ArrayList<DynamicGridChangeListener> = ArrayList()
  private var mContext: Activity? = null
  var cellWidth: Int = 0
  var cellHeight: Int = 0

  var y: Float = 0f
  var x: Float = 0f

  var width: Int = 0
  var height: Int = 0
  var isOrientationChanged = false
  private var childs: Array<Array<Rect?>?> = arrayOfNulls(0)
  private var _orientation: GridOrientation = GridOrientation.PORTRAIT
  private var _params: DynamicGridParams
  private var _cellProfile: CellProfile
  private var mGridName: String

  enum class GridOrientation {
    PORTRAIT,
    LANDSCAPE
  }

  class DynamicGridParams {
    var shouldFillHeight: Boolean = false
    var shouldFillWidth: Boolean = false
    var shouldIgnoreMargin: Boolean = false
    var rowProfile: GridOrientationSpec? = null
    var columnProfile: GridOrientationSpec? = null
    private var grid: DynamicGrid? = null

    var margin: Margin = Margin(0, 0, 0, 0)
      set(value) {
        if (value != field) {
          field = value
          if (grid != null) {
            grid!!.update()
          }
        }
      }
    // we assume that if x or y is set this grid position will be absolute and 
    // doesn't depend on parent so it should ignore margin
    var x: Float = 0f
      set(value) {
        if (value != field && value > 0f) {
          field = value
          shouldIgnoreMargin = true
        }
      }
    var y: Float = 0f
      set(value) {
        if (value != field && value > 0f) {
          field = value
          shouldIgnoreMargin = true
        }
      }

    // we assume that it should fill available width or height if width or height is set to -1
    var width: Int = 0
      set(value) {
        if (value != field) {
          if (value == -1) {
            shouldFillWidth = true
          } else {
            field = value
          }
          if (grid != null) {
            grid!!.update()
          }
        }
      }
    var height: Int = 0
      set(value) {
        if (value != field) {
          if (value == -1) {
            shouldFillHeight = true
          } else {
            field = value
          }
          if (grid != null) {
            grid!!.update()
          }
        }
      }

    constructor()

    constructor(params: DynamicGridParams) {
      this.rowProfile = params.rowProfile!!.clone()
      this.columnProfile = params.columnProfile!!.clone()
      this.margin = params.margin.clone()
      this.x = params.x
      this.y = params.y
    }

    fun clone(): DynamicGridParams {
      return DynamicGridParams(
        this
      )
    }

    fun setGrid(grid: DynamicGrid) {
      this.grid = grid
    }
  }

  class GridOrientationSpec(val portrait: Int, val landscape: Int) {
    fun clone(): GridOrientationSpec {
      return GridOrientationSpec(portrait, landscape)
    }
  }

  class CellProfile(val margin: Margin? = Margin(0, 0, 0, 0)) {
    fun clone(): CellProfile {
      return CellProfile(margin?.clone())
    }
  }

  class Margin(val left: Int, val right: Int, val top: Int, val bottom: Int) {
    fun clone(): Margin {
      return Margin(left, right, top, bottom)
    }

    override fun toString(): String {
      return "Margin: right=$right left=$left top=$top bottom=$bottom"
    }
  }

  constructor(context: Activity?, params: DynamicGridParams, cellInfo: CellProfile, name: String = "") {
    mContext = context
    mInstances.add(this)
    mGridName = name
    if ((mScreenHeight == 0 || mScreenWidth == 0) && mContext != null) {
      val display = mContext!!.windowManager.defaultDisplay
      val size = Point()
      display.getRealSize(size)
      val width = size.x
      val height = size.y
      mScreenWidth = width
      mScreenHeight = height
    }

    _params = params
    _params.setGrid(this)
    _cellProfile = cellInfo


    if (!params.shouldIgnoreMargin) {
      x = params.margin.left.toFloat()
      y = params.margin.top.toFloat()
    } else {
      x = params.x
      y = params.y
    }

    calculateGridSize()
    calculateGridItems()
  }


  fun setOrientation(orientation: GridOrientation): DynamicGrid {
    if (orientation != _orientation) {
      isOrientationChanged = true
      _orientation = orientation
      update()
    }
    return this
  }

  fun getOrientation(): GridOrientation {
    return _orientation
  }

  fun setParams(params: DynamicGridParams): DynamicGrid {
    if (_params != params) {
      _params = params
    }
    return this
  }

  fun getParams(): DynamicGridParams {
    return _params
  }

  fun setCellProfile(cellProfile: CellProfile): DynamicGrid {
    if (_cellProfile != cellProfile) {
      _cellProfile = cellProfile
    }
    return this
  }

  fun getCellProfile(): CellProfile {
    return _cellProfile
  }

  fun setMargin(margin: Margin = Margin(0, 0, 0, 0)): DynamicGrid {
    this._params.margin = margin
    return this
  }

  fun update() {
    calculateGridSize()
    calculateGridItems()
    dispatchChanges()
  }

  private fun calculateGridSize() {
    var margin = if (!_params.shouldIgnoreMargin)
      _params.margin.left + _params.margin.right
    else 0

    if (_params.shouldFillWidth) {
      _params.width = mScreenWidth - margin
    } else {
      // if the width is set to not fill available space, try to set the margin first
      // if the available width is different after setting the margin then change the width.
      val availableWidth = mScreenWidth - margin
      if (_params.width != availableWidth) {
        _params.width = availableWidth
      }
    }

    margin = if (!_params.shouldIgnoreMargin)
      _params.margin.top + _params.margin.bottom
    else 0
    if (_params.shouldFillHeight) {
      _params.height = mScreenHeight - margin - navBarHeight - statusBarHeight
    } else {
      // if the height is set to not fill available space, try to set the margin first
      // if the available height is different after setting the margin then change the height.
      val availableHeight = mScreenHeight - margin - navBarHeight - statusBarHeight
      if (_params.height != availableHeight) {
        _params.height = availableHeight
      }
    }
  }

  private fun calculateGridItems() {

    if (isOrientationChanged) {
      val tmp = width
      width = height
      height = tmp
    }

    val rowProfile = _params.rowProfile!!
    val columnProfile = _params.columnProfile!!

    val columns = if (_orientation == GridOrientation.PORTRAIT) columnProfile.portrait else columnProfile.landscape
    val rows = if (_orientation == GridOrientation.PORTRAIT) rowProfile.portrait else rowProfile.landscape

    cellWidth = _params.width / columns
    cellHeight = _params.height / rows

    if (_orientation == GridOrientation.PORTRAIT) {
      childs = Array(columnProfile.portrait) { arrayOfNulls<Rect>(rowProfile.portrait) }
      for (c in 0 until columnProfile.portrait) {
        for (r in 0 until rowProfile.portrait) {
          val cx = cellWidth * c
          val cy = (cellHeight * r)
          val right = cx + cellWidth
          val bottom = cy + cellHeight

          val rect = Rect()
          rect.set(cx, cy, right, bottom)
          childs[c]!![r] = rect
        }
      }
    } else {
      childs = Array(columnProfile.landscape) { arrayOfNulls<Rect>(rowProfile.landscape) }
      for (c in 0 until columnProfile.landscape) {
        for (r in 0 until rowProfile.landscape) {
          val cx = cellWidth * c
          val cy = cellHeight * r
          val right = cx + cellWidth
          val bottom = cy + cellHeight

          val rect = Rect()
          rect.set(cx, cy, right, bottom)
          childs[c]!![r] = rect
        }
      }
    }
    isOrientationChanged = false

  }

  fun getChildAt(x: Int, y: Int): Rect {
    return childs[x]!![y]!!
  }

  fun getMinWidthForWidget(context: Context, info: AppWidgetProviderInfo): Int {
    val padding = AppWidgetHostView.getDefaultPaddingForWidget(context, info.provider, null)
    var minWidth = info.minWidth + padding.left + padding.right
    val columnCount = Settings.appSettings().desktopColumnCount

    // get grid item width by dividing workspace width by column count
    val itemWidth = cellWidth

    // get the real minWidth, which is different based on user configuration for the desktop
    if (minWidth <= itemWidth) {
      return itemWidth
    }

    for (i in 1 until columnCount + 1) {
      val currentWidth = itemWidth * i
      if (minWidth <= currentWidth) {
        minWidth = currentWidth
        break
      }
    }
    if (minWidth > _params.width) {
      minWidth = _params.width
    }
    return minWidth
  }

  fun getMinHeightForWidget(context: Context, info: AppWidgetProviderInfo): Int {
    val padding = AppWidgetHostView.getDefaultPaddingForWidget(context, info.provider, null)
    var minHeight = info.minHeight + padding.top + padding.bottom
    val rowCount = Settings.appSettings().desktopRowCount

    // get grid item height by dividing workspace height by row count
    val itemHeight = cellHeight

    // get the real minHeight, which is different based on user configuration for the desktop
    if (minHeight <= itemHeight) {
      return itemHeight
    }

    for (i in 1 until rowCount + 1) {
      val currentHeight = itemHeight * i
      if (minHeight <= currentHeight) {
        minHeight = currentHeight
        break
      }
    }
    if (minHeight > _params.height) {
      minHeight = _params.height
    }
    return minHeight
  }

  fun getMinSpanForWidget(context: Context, info: AppWidgetProviderInfo): ArrayList<Int> {
    return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight)
  }

  fun getSpanForWidget(context: Context, info: AppWidgetProviderInfo): ArrayList<Int> {
    return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight)
  }

  fun getSpanForWidget(context: Context, componentName: ComponentName, minWidth: Int, minHeight: Int): ArrayList<Int> {
    val padding = AppWidgetHostView.getDefaultPaddingForWidget(context, componentName, null)
    val spanXY = arrayListOf(0, 0)

    val columnCount = Settings.appSettings().desktopColumnCount
    val rowCount = Settings.appSettings().desktopRowCount

    val itemWidth = cellWidth
    val itemHeight = cellHeight
    val halfWidth = itemWidth / 2
    val halfHeight = itemHeight / 2

    val widgetMinHeight = minHeight + padding.top + padding.bottom
    val widgetMinWidth = minWidth + padding.left + padding.right

    if (widgetMinWidth <= itemWidth && widgetMinHeight <= itemHeight) {
      spanXY[0] = 1
      spanXY[1] = 1
    }

    if (spanXY[0] == 0 && spanXY[1] == 0) {
      var spanX = 0
      var spanY = 0
      for (i in 1 until columnCount + 1) {
        val currentWidth = itemWidth * i
        if (widgetMinWidth <= currentWidth) {
          spanX++
          break
        } else {
          spanX++
        }
      }
      spanXY[0] = spanX

      for (i in 1 until rowCount + 1) {
        val currentHeight = itemHeight * i
        if (widgetMinHeight <= currentHeight) {
          spanY++
          break
        } else {
          spanY++
        }
      }
      spanXY[1] = spanY
    }
    return spanXY
  }

  fun getScreenWidth(): Int {
    return mScreenWidth
  }

  fun getScreenHeight(): Int {
    return mScreenHeight
  }

  fun clone(name: String = ""): DynamicGrid {
    return DynamicGrid(
      mContext!!, _params.clone(), _cellProfile.clone(), name
    )
  }

  private fun dispatchChanges() {
    for (listener in mGridChangeListeners) {
      listener.onGridChange(_params.width, _params.height, cellWidth, cellHeight)
    }
  }

  fun addGridChangeListener(listener: DynamicGridChangeListener) {
    mGridChangeListeners.add(listener)
  }

  fun removeGridChangeListener(listener: DynamicGridChangeListener) {
    mGridChangeListeners.remove(listener)
  }

  interface DynamicGridChangeListener {
    fun onGridChange(width: Int, height: Int, cellWidth: Int, cellHeight: Int)
  }
}


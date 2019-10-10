package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fisma.trinity.Constants
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.ItemViewFactory
import com.fisma.trinity.viewutil.WorkspaceCallback


class Dock(context: Context, attr: AttributeSet) : CellContainer(context, attr), WorkspaceCallback {
  private var _homeActivity: HomeActivity? = null
  private val _coordinate = Point()
  private val _previousDragPoint = Point()

  private var _previousItem: Item? = null
  private var _previousItemView: View? = null

  // open app drawer on slide up gesture
  private var _startPosX: Float = 0.toFloat()
  private var _startPosY: Float = 0.toFloat()

  fun initDock() {
    val columns = Settings.appSettings().dockColumnCount
    val rows = Settings.appSettings().dockRowCount
    setGridSize(columns, rows)
    val dockItems = HomeActivity._db.dock
    removeAllViews()
    for (item in dockItems) {
      if (item.x + item.spanX <= columns && item.y + item.spanY <= rows) {
        addItemToPage(item, 0)
      }
    }

    // call onMeasure to set the height
    measure(measuredWidth, measuredHeight)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    detectSwipe(ev)
    super.dispatchTouchEvent(ev)
    return true
  }

  private fun detectSwipe(ev: MotionEvent) {
    when (ev.action) {
      0 -> {
        _startPosX = ev.x
        _startPosY = ev.y
      }
      1 -> if (_startPosY - ev.y > 150.0f && Settings.appSettings().gestureDockSwipeUp) {
        var point = Point(ev.x.toInt(), ev.y.toInt())
        point = Tool.convertPoint(point, this, _homeActivity!!.appDrawerController)
        if (Settings.appSettings().gestureFeedback) {
          Tool.vibrate(this)
        }
        _homeActivity!!.openAppDrawer(this, point.x, point.y)
        return
      }
      else -> {
      }
    }
  }

  fun updateIconProjection(x: Int, y: Int) {
    val launcher = _homeActivity
    val dragNDropView = launcher!!.dragLayer
    val state = peekItemAndSwap(x, y, _coordinate)
    if (_coordinate != _previousDragPoint) {
      dragNDropView.cancelFolderPreview()
    }
    _previousDragPoint.set(_coordinate.x, _coordinate.y)
    when (state) {
      DragState.CurrentNotOccupied -> projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap)
      DragState.OutOffRange, DragState.ItemViewNotFound -> {
      }
      DragState.CurrentOccupied -> {
        val type = dragNDropView.dragItem!!.type
        clearCachedOutlineBitmap()
        if (type != Item.Type.WIDGET && type != Item.Type.APPWIDGET && coordinateToChildView(_coordinate) is AppItemView) {
          dragNDropView.showFolderPreviewAt(this, cellWidth * (_coordinate.x + 0.5f), cellHeight * (_coordinate.y + 0.5f) - if (Settings.appSettings().dockShowLabel) Tool.dp2px(7f) else 0)
        }
      }
      else -> {
      }
    }
  }

  override fun setLastItem(item: Item, view: View) {
    _previousItemView = view
    _previousItem = item
    removeView(view)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    if (!isInEditMode) {
      // set the height for the dock based on the number of rows and the show label preference
      val iconSize = Settings.appSettings().dockIconSize
      var height = Tool.dp2px(((iconSize + 20) * cellSpanV).toFloat())
      if (Settings.appSettings().dockShowLabel) height += Tool.dp2px(20f)
      layoutParams.height = height
      setMeasuredDimension(View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), height)
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
  }

  override fun consumeLastItem() {
    _previousItem = null
    _previousItemView = null
  }

  override fun revertLastItem() {
    if (_previousItemView != null && _previousItem != null) {
      addViewToGrid(_previousItemView!!)
      _previousItem = null
      _previousItemView = null
    }
  }

  override fun addItemToPage(item: Item, page: Int): Boolean {
    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item)
      ?: // TODO see if this fixes SD card bug
      //HomeActivity._db.deleteItem(item, true);
      return false
    item._location = Constants.ItemPosition.Dock
    if (!Settings.appSettings().dockShowLabel) {
      (itemView as AppItemView).showLabel = false
      itemView.invalidate()
    }
    addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
    return true
  }


  override fun addItemToPoint(item: Item, x: Int, y: Int): Boolean {
    val positionToLayoutPrams = coordinateToLayoutParams(x, y, item.spanX, item.spanY)
      ?: return false
    item._location = Constants.ItemPosition.Dock
    item.x = positionToLayoutPrams.x
    item.y = positionToLayoutPrams.y
    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item)
    if (!Settings.appSettings().dockShowLabel) {
      (itemView as AppItemView).showLabel = false
      itemView.invalidate()
    }
    if (itemView != null) {
      itemView.layoutParams = positionToLayoutPrams
      addView(itemView)
    }
    return true
  }

  override fun addItemToCell(item: Item, x: Int, y: Int): Boolean {
    item._location = Constants.ItemPosition.Dock
    item.x = x
    item.y = y

    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item)
      ?: return false
    if (!Settings.appSettings().dockShowLabel) {
      (itemView as AppItemView).showLabel = false
      itemView.invalidate()
    }
    addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
    return true
  }

  override fun removeItem(view: View, animate: Boolean) {
    if (animate) {
      view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction {
        if (view.parent == this@Dock) {
          removeView(view)
        }
      }
    } else if (this == view.parent) {
      removeView(view)
    }
  }

  fun setHome(homeActivity: HomeActivity) {
    _homeActivity = homeActivity
  }
}

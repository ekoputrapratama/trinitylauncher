package com.fisma.trinity.viewutil

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.fisma.trinity.R
import com.fisma.trinity.TrinityApplication
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.widgets.*


object ItemViewFactory {
  fun getItemView(context: Context, callback: WorkspaceCallback?, type: DragAction.Action, item: Item): View? {
    var view: View? = null
    if (item.type == Item.Type.APPWIDGET && type == DragAction.Action.DESKTOP) {
      view = getWidgetView(context, callback, type, item)
    } else if (item.type == Item.Type.APPWIDGET && DragAction.Action.WIDGET_PREVIEW == type) {
      var li = LayoutInflater.from(context)
      view = li.inflate(R.layout.view_widget_preview, null, false) as AppWidgetPreview
      view.setOnLongClickListener(View.OnLongClickListener { view ->

        if (Settings.appSettings().gestureFeedback) {
          Tool.vibrate(view)
        }
        DragHandler.startDrag(view, item, DragAction.Action.APPWIDGET, null)
        true
      })
    } else {
      val builder = AppItemView.Builder(context)
      builder.setIconSize(Settings.appSettings().iconSize)
      builder.vibrateWhenLongPress(Settings.appSettings().gestureFeedback)

      if (item.type != Item.Type.ACTION && item.actionValue != 8)
        builder.withOnLongClick(item, type, callback)

      when (type) {
        DragAction.Action.DRAWER -> {
          builder.setLabelVisibility(Settings.appSettings().drawerShowLabel)
          builder.setTextColor(Settings.appSettings().drawerLabelColor)
        }
        DragAction.Action.DESKTOP -> {
          builder.setLabelVisibility(Settings.appSettings().desktopShowLabel)
          builder.setTextColor(Color.WHITE)
        }
        else -> {
          builder.setLabelVisibility(Settings.appSettings().desktopShowLabel)
          builder.setTextColor(Color.WHITE)
        }
      }
      when (item.type) {
        Item.Type.APP -> {
          val app = Settings.appLoader().findItemApp(item) ?: return null
          view = builder.setAppItem(item).view
        }
        Item.Type.SHORTCUT -> view = builder.setShortcutItem(item).view
        Item.Type.GROUP -> {
          view = builder.setGroupItem(context, callback!!, item).view
          view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        Item.Type.ACTION -> view = builder.setActionItem(item).view
      }
    }

    // TODO find out why tag is set here
    if (view != null) {
      view.tag = item
    }
    return view
  }

  fun getWidgetView(context: Context, callback: WorkspaceCallback?, type: DragAction.Action, item: Item): View? {
    if (HomeActivity._WidgetHost == null) return null
    val widgetHost = HomeActivity._WidgetHost
    val info = HomeActivity.mAppWidgetManager.getAppWidgetInfo(item.widgetValue)
    val widgetView = widgetHost.createView(TrinityApplication.get(), item.widgetValue, info)
    widgetView.setAppWidget(item.widgetValue, info)
    widgetView.visibility = View.VISIBLE


    val widgetContainer = LayoutInflater.from(context).inflate(R.layout.view_widget_container, null) as FrameLayout
    widgetContainer.addView(widgetView)

    // TODO move this to standard DragHandler.getLongClick() method
    // needs to be set on widgetView but use widgetContainer inside
    widgetView.setOnLongClickListener(View.OnLongClickListener { view ->
      if (!Workspace.isInEditMode()) {
        AppWidgetResizeFrame.hideResizeFrame()
        if (Settings.appSettings().desktopLock) {
          return@OnLongClickListener false
        }
        if (Settings.appSettings().gestureFeedback) {
          Tool.vibrate(view)
        }
        DragHandler.startDrag(widgetContainer, item, DragAction.Action.DESKTOP, callback)
      }
      true
    })

    return widgetContainer
  }

  private fun scaleWidget(view: View, item: Item) {
    item.spanX = (Math.min(item.spanX, HomeActivity.launcher.workspace.currentPage.cellSpanH))
    item.spanX = (Math.max(item.spanX, 1))
    item.spanY = (Math.min(item.spanY, HomeActivity.launcher.workspace.currentPage.cellSpanV))
    item.spanY = (Math.max(item.spanY, 1))

    HomeActivity.launcher.workspace.currentPage.setOccupied(false, view.layoutParams as CellContainer.LayoutParams)
    if (!HomeActivity.launcher.workspace.currentPage.checkOccupied(Point(item.x, item.y), item.spanX, item.spanY)) {
      val newWidgetLayoutParams = CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.x, item.y, item.spanX, item.spanY)

      // update occupied array
      HomeActivity.launcher.workspace.currentPage.setOccupied(true, newWidgetLayoutParams)

      // update the view
      view.layoutParams = newWidgetLayoutParams
      updateWidgetOption(item)

      // update the widget size in the database
      HomeActivity._db.saveItem(item)
    } else {
      Toast.makeText(HomeActivity.launcher.workspace.context, R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show()

      // add the old layout params to the occupied array
      HomeActivity.launcher.workspace.currentPage.setOccupied(true, view.layoutParams as CellContainer.LayoutParams)
    }
  }

  private fun updateWidgetOption(item: Item) {
    val workspace = HomeActivity.launcher.workspace
    val newOps = Bundle()
    newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.spanX * workspace.currentPage.cellWidth)
    newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.spanX * workspace.currentPage.cellWidth)
    newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.spanY * workspace.currentPage.cellHeight)
    newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.spanY * workspace.currentPage.cellHeight)
    HomeActivity.mAppWidgetManager.updateAppWidgetOptions(item.widgetValue, newOps)
  }
}

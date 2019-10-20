package com.fisma.trinity.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.model.Item
import com.fisma.trinity.viewutil.WorkspaceCallback
import com.fisma.trinity.widgets.AppItemView
import com.fisma.trinity.widgets.CellContainer
import com.fisma.trinity.widgets.Workspace


object DragHandler {
  const val TAG = "DragHandler"
  var _cachedDragBitmap: Bitmap? = null

  fun startDrag(view: View, item: Item, action: DragAction.Action, workspaceCallback: WorkspaceCallback?) {
    var v = view
    if (action == DragAction.Action.APPWIDGET) {
      v = view.findViewById(R.id.widget_preview)
      v.tag = view.tag
    }

    _cachedDragBitmap = loadBitmapFromView(v)
    if (item.type == Item.Type.APPWIDGET && action == DragAction.Action.DESKTOP) {
      (view as FrameLayout).removeAllViews()
      val p = view.parent as CellContainer
      p.removeView(view)
    }

    HomeActivity._launcher?.dragLayer?.startDragNDropOverlay(v, item, action)
    workspaceCallback?.setLastItem(item, v)

  }

  fun getLongClick(item: Item, action: DragAction.Action, workspaceCallback: WorkspaceCallback?): View.OnLongClickListener {
    return View.OnLongClickListener { view ->
      if (!Workspace.isInEditMode()) {
        if (Settings.appSettings().desktopLock) {
          return@OnLongClickListener false
        }
        if (Settings.appSettings().gestureFeedback) {
          Tool.vibrate(view)
        }
        startDrag(view, item, action, workspaceCallback)
      }
      true
    }
  }

  private fun loadBitmapFromView(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    var tempLabel: String? = null
    if (view is AppItemView) {
      tempLabel = view.label
      view.label = " "
    }
    view.layout(0, 0, view.width, view.height)
    view.draw(canvas)
    if (view is AppItemView) {
      view.label = tempLabel
    }
    view.parent.requestLayout()
    return bitmap
  }
}
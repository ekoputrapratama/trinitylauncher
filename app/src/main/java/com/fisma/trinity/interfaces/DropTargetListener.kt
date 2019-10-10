package com.fisma.trinity.interfaces

import android.graphics.PointF
import android.view.View
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction


interface DropTargetListener {
  val view: View

  fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean

  fun onStartDrag(action: DragAction.Action, location: PointF)

  fun onDrop(action: DragAction.Action, location: PointF, item: Item)

  fun onMove(view: View, action: DragAction.Action, location: PointF)

  fun onEnter(action: DragAction.Action, location: PointF)

  fun onExit(action: DragAction.Action, location: PointF)

  fun onEnd()
}
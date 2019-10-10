package com.fisma.trinity.interfaces

import android.graphics.PointF
import android.view.View
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction


interface DialogListener {

  interface OnActionDialogListener {
    fun onAdd(type: Int)
  }

  interface OnEditDialogListener {
    fun onRename(name: String)
  }
}

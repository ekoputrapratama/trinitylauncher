package com.fisma.trinity.viewutil

import android.view.View
import com.fisma.trinity.interfaces.ItemHistory
import com.fisma.trinity.model.Item

interface WorkspaceCallback : ItemHistory {
  fun addItemToPoint(item: Item, x: Int, y: Int): Boolean

  fun addItemToPage(item: Item, page: Int): Boolean

  fun addItemToCell(item: Item, x: Int, y: Int): Boolean

  fun removeItem(view: View, animate: Boolean)
}

package com.fisma.trinity.interfaces

import android.view.View
import com.fisma.trinity.model.Item

interface ItemHistory {
  fun setLastItem(item: Item, view: View)

  fun revertLastItem()

  fun consumeLastItem()
}

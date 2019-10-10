package com.fisma.trinity.widgets

import android.content.Context
import android.util.AttributeSet

class Desktop : CellContainer {
  var mContext: Context? = null

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    this.mContext = context
  }
}
package com.fisma.trinity.widgets

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets

class NavBarView(context: Context, attr: AttributeSet) : View(context, attr) {

  public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    // scale the view to pad the home layout for the missing status and navigation bars
    // TODO move home layout to class so this can be done within the view itself
  }

//  override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//      val inset = insets.systemWindowInsetBottom
//      if (inset != 0) {
//        val layoutParams = layoutParams
//        layoutParams.height = inset
//        setLayoutParams(layoutParams)
//        visibility = View.VISIBLE
//      }
//    }
//    return insets
//  }
}
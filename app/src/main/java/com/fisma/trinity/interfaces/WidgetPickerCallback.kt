package com.fisma.trinity.interfaces

import android.content.Intent
import android.graphics.PointF
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.model.AppWidget

interface WidgetPickerCallback {
  fun onCreateWidget(launcher: HomeActivity, widget: AppWidget, location: PointF)
  fun onWidgetBinded(launcher: HomeActivity, data: Intent)
}

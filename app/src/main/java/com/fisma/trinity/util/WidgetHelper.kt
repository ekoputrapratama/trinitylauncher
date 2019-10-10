package com.fisma.trinity.util

import android.R.attr.bottom
import android.appwidget.AppWidgetManager
import android.R.attr.right
import android.R.attr.top
import android.R.attr.left
import android.os.Bundle
import android.appwidget.AppWidgetHostView
import android.os.Build
import com.fisma.trinity.model.PendingAddWidgetInfo


object WidgetHelper {
//    fun getDefaultOptionsForWidget(launcher: Launcher, info: PendingAddWidgetInfo): Bundle? {
//        var options: Bundle? = null
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            AppWidgetResizeFrame.getWidgetSizeRanges(launcher, info.spanX, info.spanY, sTmpRect)
//            val padding = AppWidgetHostView.getDefaultPaddingForWidget(launcher,
//                    info.componentName, null)
//
//            val density = launcher.getResources().getDisplayMetrics().density
//            val xPaddingDips = ((padding.left + padding.right) / density) as Int
//            val yPaddingDips = ((padding.top + padding.bottom) / density) as Int
//
//            options = Bundle()
//            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
//                    sTmpRect.left - xPaddingDips)
//            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
//                    sTmpRect.top - yPaddingDips)
//            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
//                    sTmpRect.right - xPaddingDips)
//            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
//                    sTmpRect.bottom - yPaddingDips)
//        }
//        return options
//    }
}
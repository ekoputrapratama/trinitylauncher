package com.fisma.trinity.widgets

import android.animation.Animator
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.interfaces.WidgetPickerCallback
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.util.DynamicGrid
import com.fisma.trinity.util.Tool
import io.codetail.animation.ViewAnimationUtils
import io.codetail.widget.RevealFrameLayout
import java.util.*
import net.gsantner.opoc.util.Callback

class AppWidgetPicker : RevealFrameLayout, WidgetPickerCallback {
  var pickerPage: AppWidgetPickerPage? = null

  var isOpen = false
  private var widgetPickerCallback: Callback.a2<Boolean, Boolean>? = null
  private var widgetPickerAnimator: Animator? = null
  private var widgetPickerAnimationTime: Int = 0
  val widgetPicker: View
    get() = pickerPage!!

  private var currentWidgetDrop: AppWidget? = null
  private var currentDropLocation: PointF? = null

  companion object {
    const val TAG = "AppWidgetPicker"
    private var mInstance: AppWidgetPicker? = null
    var grid: DynamicGrid? = null

    fun getInstance(): AppWidgetPicker? {
      return mInstance
    }
  }


  constructor(context: Context) : super(context) {
    mInstance = this
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    mInstance = this
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    mInstance = this
    val height = context.resources.displayMetrics.heightPixels - Tool.dp2px(24f) - Tool.dp2px(48f)
    val width = context.resources.displayMetrics.widthPixels
    measure(height, width)
  }

  fun setCallBack(callBack: Callback.a2<Boolean, Boolean>) {
    widgetPickerCallback = callBack

  }

  fun open(cx: Int, cy: Int) {
    if (isOpen) return
    Log.d("AppWidgetPicker", "open widget picker")
    isOpen = true

    widgetPickerAnimationTime = Settings.appSettings().animationSpeed * 10
    widgetPickerAnimator = ViewAnimationUtils.createCircularReveal(pickerPage, cx, cy, 0f, Math.max(width, height).toFloat())
    widgetPickerAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    widgetPickerAnimator!!.duration = widgetPickerAnimationTime.toLong()
    widgetPickerAnimator!!.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(p1: Animator) {
        visibility = View.VISIBLE
        widgetPickerCallback!!.callback(true, true)
        Log.d("AppWidgetPicker", "widget picker animation started")
      }

      override fun onAnimationEnd(p1: Animator) {
        widgetPickerCallback!!.callback(true, false)
        Log.d("AppWidgetPicker", "widget picker animation ended")
      }

      override fun onAnimationCancel(p1: Animator) {}

      override fun onAnimationRepeat(p1: Animator) {}
    })

    widgetPickerAnimator!!.start()
  }

  fun close(cx: Int, cy: Int) {
    if (!isOpen) return
    Log.d("AppWidgetPicker", "close widget picker")
    isOpen = false

    widgetPickerAnimationTime = Settings.appSettings().animationSpeed * 10
    widgetPickerAnimator = ViewAnimationUtils.createCircularReveal(widgetPicker, cx, cy, Math.max(width, height).toFloat(), 0f)
    widgetPickerAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    widgetPickerAnimator!!.duration = widgetPickerAnimationTime.toLong()
    widgetPickerAnimator!!.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(p1: Animator) {
        Log.d(TAG, "widget picker animation started")
        widgetPickerCallback!!.callback(false, true)
      }

      override fun onAnimationEnd(p1: Animator) {
        Log.d("AppWidgetPicker", "widget picker animation ended")
        widgetPickerCallback!!.callback(false, false)
        visibility = View.GONE
      }

      override fun onAnimationCancel(p1: Animator) {}

      override fun onAnimationRepeat(p1: Animator) {}
    })

    widgetPickerAnimator!!.start()
  }

  fun reset() {
    pickerPage!!.setCurrentItem(0, false)
  }

  fun init() {
    if (isInEditMode) return
    val layoutInflater = LayoutInflater.from(context)
    visibility = View.GONE

    pickerPage = layoutInflater.inflate(R.layout.view_widget_picker_page, this, false) as AppWidgetPickerPage
    addView(pickerPage, 0)
    pickerPage!!.visibility = View.VISIBLE
  }

  fun getDefaultOptionsForWidget(widget: AppWidget): Bundle? {
    var options: Bundle? = null
    val launcher: HomeActivity = HomeActivity.launcher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      val density = launcher.resources.displayMetrics.density
      // val xPaddingDips = 

      options = Bundle()
      options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widget.minHeight)
      options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widget.minWidth)
      options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, widget.minHeight)
      options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widget.minWidth)
    }
    return options
  }

  override fun onWidgetBinded(launcher: HomeActivity, data: Intent) {
    Log.d(TAG, "onWidgetBinded")
    if (currentDropLocation != null && currentWidgetDrop != null) {
      val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
      val widget = currentWidgetDrop!!
      widget.appWidgetId = appWidgetId

      val onCompleteRunnable = Runnable {
        val workspace = launcher.workspace
        workspace.addItemToPage(widget, workspace.currentItem)
        currentWidgetDrop = null
        currentDropLocation = null
      }
      onCompleteRunnable.run()
    }
  }

  override fun onCreateWidget(launcher: HomeActivity, widget: AppWidget, location: PointF) {
    Log.d(TAG, "onCreateWidget")
    val widgetHost = HomeActivity._appWidgetHost
    val mAppWidgetManager = HomeActivity.mAppWidgetManager
    var appWidgetId = 0
    val options = getDefaultOptionsForWidget(widget)
    var appWidgetBounded = false

    val mBindWidgetRunnable = Runnable {
      Log.d(TAG, "binding appwidget")
      appWidgetId = widgetHost.allocateAppWidgetId()
      if (mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, widget.widgetInfo!!, options)) {
        Log.d(TAG, "appwidget binded")
        appWidgetBounded = true
      }
    }
    post(mBindWidgetRunnable)

    val mInflateWidgetRunnable = Runnable {
      val workspace = launcher.workspace
      val pos = Point()
      workspace.currentPage.touchPosToCoordinate(pos, location.x.toInt(), location.y.toInt(), widget.spanX, widget.spanY, false)
      widget.x = pos.x
      widget.y = pos.y
      currentDropLocation = location
      // if user already grant permission to bind widget, we immediately create the view and add it to workspace
      if (appWidgetBounded) {
        Log.d(TAG, "inflating appwidget view")
        widget.appWidgetId = appWidgetId
        workspace.addItemToPage(widget, workspace.currentItem)
      } else {
        Log.d(TAG, "Request appwidget binding")
        currentWidgetDrop = widget
        val info = widget.widgetInfo

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info!!.provider)

        if (Build.VERSION.SDK_INT >= 21)
          mAppWidgetManager.getUser(info)
            .addToIntent(intent, AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE)
        // TODO: we need to make sure that this accounts for the options bundle.
        // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
        launcher.startActivityForResult(intent, HomeActivity.REQUEST_BIND_APPWIDGET)
      }
    }
    post(mInflateWidgetRunnable)
  }

  // override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
  //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
  //     setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
  //     return insets
  //   }
  //   return insets
  // }
}


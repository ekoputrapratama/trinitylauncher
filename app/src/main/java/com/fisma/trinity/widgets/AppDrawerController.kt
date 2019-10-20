package com.fisma.trinity.widgets


import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.fisma.trinity.R
import com.fisma.trinity.manager.Settings
import io.codetail.animation.ViewAnimationUtils

import net.gsantner.opoc.util.Callback

import io.codetail.widget.RevealFrameLayout

class AppDrawerController : RevealFrameLayout {
  lateinit var _drawerViewPage: AppDrawerPage
  lateinit var _drawerViewGrid: AppDrawerGrid
  var _drawerMode: Int = 0
  var _isOpen = false
  private var _appDrawerCallback: Callback.a2<Boolean, Boolean>? = null
  private var _appDrawerAnimator: Animator? = null
  private var _drawerAnimationTime: Int = 0

  val drawer: View
    get() {
      when (_drawerMode) {
        Mode.GRID -> return _drawerViewGrid
        Mode.PAGE -> return _drawerViewPage
        else -> return _drawerViewPage
      }
    }

  object Mode {
    val LIST = 0
    val GRID = 1
    val PAGE = 2
  }

  companion object {
    private var mInstance: AppDrawerController? = null

    fun isDrawerOpened(): Boolean {
      return mInstance!!._isOpen
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
  }

  fun setCallBack(callBack: Callback.a2<Boolean, Boolean>) {
    _appDrawerCallback = callBack
  }

  fun open(cx: Int, cy: Int) {
    if (_isOpen) return
    AppWidgetResizeFrame.hideResizeFrame()
    _isOpen = true
    _drawerAnimationTime = Settings.appSettings().animationSpeed * 10
    _appDrawerAnimator = ViewAnimationUtils.createCircularReveal(drawer, cx, cy, 0f, Math.max(width, height).toFloat())
    _appDrawerAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    _appDrawerAnimator!!.duration = _drawerAnimationTime.toLong()
    _appDrawerAnimator!!.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(p1: Animator) {
        visibility = View.VISIBLE
        _appDrawerCallback!!.callback(true, true)
      }

      override fun onAnimationEnd(p1: Animator) {
        _appDrawerCallback!!.callback(true, false)
      }

      override fun onAnimationCancel(p1: Animator) {}

      override fun onAnimationRepeat(p1: Animator) {}
    })

    _appDrawerAnimator!!.start()
  }

  fun close(cx: Int, cy: Int) {
    if (!_isOpen) return
    _isOpen = false

    _drawerAnimationTime = Settings.appSettings().animationSpeed * 10
    _appDrawerAnimator = ViewAnimationUtils.createCircularReveal(drawer, cx, cy, Math.max(width, height).toFloat(), 0f)
    _appDrawerAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    _appDrawerAnimator!!.duration = _drawerAnimationTime.toLong()
    _appDrawerAnimator!!.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(p1: Animator) {
        _appDrawerCallback!!.callback(false, true)
      }

      override fun onAnimationEnd(p1: Animator) {
        _appDrawerCallback!!.callback(false, false)
        visibility = View.GONE
      }

      override fun onAnimationCancel(p1: Animator) {}

      override fun onAnimationRepeat(p1: Animator) {}
    })

    _appDrawerAnimator!!.start()
  }

  fun reset() {
    when (_drawerMode) {
      Mode.GRID -> _drawerViewGrid._recyclerView.scrollToPosition(0)
      Mode.PAGE -> _drawerViewPage.setCurrentItem(0, false)
      else -> _drawerViewPage.setCurrentItem(0, false)
    }
  }

  fun init() {
    if (isInEditMode) return
    val layoutInflater = LayoutInflater.from(context)
    _drawerMode = Settings.appSettings().drawerStyle
    visibility = View.GONE
    setBackgroundColor(Color.parseColor("#CD000000"))
    when (_drawerMode) {
      Mode.GRID -> {
        _drawerViewGrid = AppDrawerGrid(context)
        _drawerViewGrid.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(_drawerViewGrid)
      }
      else -> {
        _drawerViewPage = layoutInflater.inflate(R.layout.view_app_drawer_page, this, false) as AppDrawerPage
        addView(_drawerViewPage)
        val indicator = layoutInflater.inflate(R.layout.view_drawer_indicator, this, false) as PagerIndicator
        addView(indicator)
        _drawerViewPage.withHome(indicator)
      }
    }
  }
}

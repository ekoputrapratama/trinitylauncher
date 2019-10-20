package com.fisma.trinity.widgets

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import java.util.ArrayList
import androidx.core.view.ViewCompat.setAlpha
import kotlin.math.abs


class ContentView : ViewPager {

  companion object {
    const val TAG = "ContentView"
    @SuppressLint("StaticFieldLeak")
    var mInstance: ContentView? = null

    fun isDashboardOpened(): Boolean {
      return mInstance!!.currentItem == 0
    }
  }

  private val _pages = ArrayList<View>()
  val pages: MutableList<View>
    get() = _pages

  private var _adapter: PageAdapter? = null
  internal var _dashboardView: DashboardView? = null
  internal var _content: View? = null

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
    mInstance = this
  }

  init {
    _dashboardView = DashboardView(context)
    val inflater = LayoutInflater.from(context)
    val view = inflater.inflate(R.layout.view_home, this, false)
    _content = view
    _adapter = PageAdapter(this)
    adapter = _adapter

    currentItem = 1

    addOnPageChangeListener(object : OnPageChangeListener {
      var dragging = false
      var settling = false
      var lastItem: Int = 1
      var lastOffset: Float = 0f
      override fun onPageScrollStateChanged(state: Int) {
        if (state != SCROLL_STATE_SETTLING && state != SCROLL_STATE_IDLE) lastItem = currentItem
        dragging = SCROLL_STATE_DRAGGING == state
        settling = SCROLL_STATE_SETTLING == state
        val background = HomeActivity.launcher.background

        // onPageSelected would not be called if the range doesn't reach threshold
        // so we need to handle it here
        if (state != SCROLL_STATE_IDLE) {
          if (lastItem == 1) {
            if (settling && lastOffset <= 0.4f)
              background.animate().alpha(1f)
            else if (settling && lastOffset > 0.4f)
              background.animate().alpha(0f)
          } else {
            if (settling && lastOffset <= 0.6f)
              background.animate().alpha(1f)
            else if (settling && lastOffset > 0.6f)
              background.animate().alpha(0f)
          }
        }
      }

      override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        val background = HomeActivity.launcher.background
        if (dragging && offset != 0f) {
          lastOffset = offset
          background.alpha = 1 - offset
        }
      }

      override fun onPageSelected(position: Int) {
        val background = HomeActivity.launcher.background
        if (settling) {
          if (position == 1) {
            background.animate().alpha(0f)
          } else {
            background.animate().alpha(1f)
          }
        }
      }
    })
  }

  fun getContentView(): View {
    return _content!!
  }

  fun getDashboardView(): DashboardView {
    return _dashboardView!!
  }

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    if (Workspace.isAtFirstPage() && !Workspace.isInEditMode() && !AppWidgetPicker.getInstance()!!.isOpen &&
      !AppDrawerController.isDrawerOpened() && !DragLayer.isDragging() && !DragLayer.isResizing() && super.onInterceptTouchEvent(ev)) {
      Workspace.blockTouch()
      return true
    }
    Workspace.unblockTouch()
    return false
  }

  inner class PageAdapter(val container: ContentView) : PagerAdapter() {
    private val dashboardView: DashboardView
      get() {
        return _dashboardView!!
      }

    private val contentView: View
      get() = _content!!

    init {
      container.pages.clear()
      container.pages.add(0, dashboardView)
      container.pages.add(1, contentView)
    }

    override fun getItemPosition(`object`: Any): Int {
      return POSITION_NONE
    }

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
      val view = container.pages[position]
      parent.addView(view)
      return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
      container.removeView(`object` as View)
    }

    override fun isViewFromObject(p1: View, p2: Any): Boolean {
      return p1 === p2
    }

    override fun getCount(): Int {
      return container.pages.size
    }
  }
}


package com.fisma.trinity.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import java.util.ArrayList

class ContentView : ViewPager {

  companion object {
    const val TAG = "ContentView"
    var mInstance: ContentView? = null
    fun isDashboardOpened(): Boolean {
      return mInstance!!.currentItem == 0
    }
  }

  private val _pages = ArrayList<View>()
  val pages: MutableList<View>
    get() = _pages

  private var _adapter: PageAdapter? = null
  val currentPage: View
    get() = _pages[currentItem]

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
      override fun onPageScrollStateChanged(state: Int) {
        Log.d(TAG, "onPageScrollStateChanged state=$state")
      }

      override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        Log.d(TAG, "onPageScrolled offset=$offset position=$position offsetPixels=$offsetPixels")
      }

      override fun onPageSelected(position: Int) {
        Log.d(TAG, "onPageSelected position=$position")
        val background = HomeActivity.launcher.background
        if (position == 1) {
          background.alpha = 0.0f
        } else {
          background.alpha = 1.0f
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
      return PagerAdapter.POSITION_NONE
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


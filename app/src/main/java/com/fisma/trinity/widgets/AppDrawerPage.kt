package com.fisma.trinity.widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.R
import com.fisma.trinity.interfaces.AppUpdateListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.App
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.ItemViewFactory
import java.util.ArrayList


class AppDrawerPage : ViewPager {
  private var _apps: List<App>? = null

  var _pages: MutableList<ViewGroup> = ArrayList()

  private var _appDrawerIndicator: PagerIndicator? = null

  private var _pageCount = 0

  constructor(context: Context, attr: AttributeSet) : super(context, attr) {
    init(context)
  }

  constructor(context: Context) : super(context) {
    init(context)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    if (_apps == null) {
      super.onConfigurationChanged(newConfig)
      return
    }

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      setLandscapeValue()
      calculatePage()
      adapter = Adapter()
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      setPortraitValue()
      calculatePage()
      adapter = Adapter()
    }
    super.onConfigurationChanged(newConfig)
  }

  private fun setPortraitValue() {
    _columnCellCount = Settings.appSettings().drawerColumnCount
    _rowCellCount = Settings.appSettings().drawerRowCount
  }

  private fun setLandscapeValue() {
    _columnCellCount = Settings.appSettings().drawerRowCount
    _rowCellCount = Settings.appSettings().drawerColumnCount
  }

  private fun calculatePage() {
    _pageCount = 0
    var appsSize = _apps!!.size
    var size = appsSize - _rowCellCount * _columnCellCount
    while (size >= _rowCellCount * _columnCellCount) {
      Log.d("AppDrawerPage", "calculatePages() ")
      _pageCount++
      size -= (_rowCellCount * _columnCellCount)
    }
  }

  private fun init(c: Context) {
    if (isInEditMode) return

    overScrollMode = View.OVER_SCROLL_NEVER

    val mPortrait = c.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    if (mPortrait) {
      setPortraitValue()
    } else {
      setLandscapeValue()
    }

    val allApps = Settings.appLoader().getAllApps(c, false)
    if (allApps.isNotEmpty()) {
      this@AppDrawerPage._apps = allApps
      calculatePage()
      adapter = Adapter()
      if (_appDrawerIndicator != null)
        _appDrawerIndicator!!.setViewPager(this@AppDrawerPage)
    }
    Settings.appLoader().addUpdateListener(object : AppUpdateListener {
      override fun onAppUpdated(apps: List<App>): Boolean {
        this@AppDrawerPage._apps = apps
        calculatePage()
        adapter = Adapter()
        if (_appDrawerIndicator != null)
          _appDrawerIndicator!!.setViewPager(this@AppDrawerPage)

        return false
      }
    })
  }

  fun withHome(appDrawerIndicator: PagerIndicator) {
    _appDrawerIndicator = appDrawerIndicator
    appDrawerIndicator.setMode(PagerIndicator.Mode.DOTS)
    if (adapter != null)
      appDrawerIndicator.setViewPager(this@AppDrawerPage)
  }

  inner class Adapter : PagerAdapter() {

    private fun getItemView(page: Int, x: Int, y: Int): View? {
      val pagePos = y * _columnCellCount + x
      val pos = _rowCellCount * _columnCellCount * page + pagePos

      if (pos >= _apps!!.size)
        return null

      val app = _apps!![pos]
      val item = Item.newAppItem(app)
      val callback = DragHandler.getLongClick(item, DragAction.Action.DRAWER, null)
      return ItemViewFactory.getItemView(context, null, DragAction.Action.DRAWER, Item.newAppItem(app))
    }

    init {
      _pages.clear()
      for (i in 0 until _pageCount) {
        val layout = LayoutInflater.from(context).inflate(R.layout.view_app_drawer_page_inner, null) as ViewGroup
        if (!Settings.appSettings().drawerShowCardView) {
          (layout.getChildAt(0) as CardView).setCardBackgroundColor(Color.TRANSPARENT)
          (layout.getChildAt(0) as CardView).cardElevation = 0f
        } else {
          (layout.getChildAt(0) as CardView).setCardBackgroundColor(Settings.appSettings().drawerCardColor)
          (layout.getChildAt(0) as CardView).cardElevation = Tool.dp2px(4f).toFloat()
        }
        val cc = layout.findViewById<CellContainer>(R.id.group)
        cc.setGridSize(_columnCellCount, _rowCellCount)

        for (x in 0 until _columnCellCount) {
          for (y in 0 until _rowCellCount) {
            val view = getItemView(i, x, y)
            if (view != null) {
              val lp = CellContainer.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, x, y, 1, 1)
              view.layoutParams = lp
              cc.addViewToGrid(view)
            }
          }
        }
        _pages.add(layout)
      }
    }

    override fun getCount(): Int {
      return _pageCount
    }

    override fun isViewFromObject(p1: View, p2: Any): Boolean {
      return p1 === p2
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
      container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
      val index = _pages.indexOf(`object`)
      return if (index == -1)
        PagerAdapter.POSITION_NONE
      else
        index
    }

    override fun instantiateItem(container: ViewGroup, pos: Int): Any {
      val layout = _pages[pos]
      container.addView(layout)
      return layout
    }
  }

  companion object {

    private var _columnCellCount: Int = 0
    private var _rowCellCount: Int = 0
  }
}

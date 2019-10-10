package com.fisma.trinity.widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.fisma.trinity.R
import com.fisma.trinity.interfaces.AppUpdateListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.App
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.IconLabelItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.turingtechnologies.materialscrollbar.AlphabetIndicator
import com.turingtechnologies.materialscrollbar.DragScrollBar
import com.turingtechnologies.materialscrollbar.INameableAdapter
import java.util.ArrayList


class AppDrawerGrid(context: Context) : FrameLayout(context) {

  var _recyclerView: RecyclerView
  var _gridDrawerAdapter: AppDrawerGridAdapter? = null
  var _scrollBar: DragScrollBar
  private val _layoutManager: GridLayoutManager?
  private var cardView: CardView

  init {
    val layoutInflater = LayoutInflater.from(getContext())
    val view = layoutInflater.inflate(R.layout.view_app_drawer_grid, this@AppDrawerGrid, false)
    addView(view)
    cardView = view.findViewById(R.id.drawer_card)
    _recyclerView = findViewById(R.id.recycler_view)
    _scrollBar = findViewById(R.id.scroll_bar)
    _layoutManager = GridLayoutManager(getContext(), Settings.appSettings().drawerColumnCount)

    init()
  }

  private fun init() {
    if (!Settings.appSettings().drawerShowIndicator) _scrollBar.visibility = View.GONE

    val indicator = AlphabetIndicator(cardView.context)
    // TODO: find alternative to make the bubble on top of the cardview on lower api than api 21
    if (Build.VERSION.SDK_INT >= 21) {
      indicator.translationZ = 100f
    } else {
      indicator.bringToFront()
    }

    _scrollBar.setIndicator(indicator, true)
    _scrollBar.clipToPadding = true
    _scrollBar.setDraggableFromAnywhere(true)
    _scrollBar.setHandleColor(Settings.appSettings().drawerFastScrollColor)

    _gridDrawerAdapter = AppDrawerGridAdapter()

    if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      setPortraitValue()
    } else {
      setLandscapeValue()
    }
    _recyclerView.adapter = _gridDrawerAdapter
    _recyclerView.layoutManager = _layoutManager
    _recyclerView.isDrawingCacheEnabled = true

    val card = findViewById<CardView>(R.id.drawer_card)
    if (!Settings.appSettings().drawerShowCardView) {
      card.setCardBackgroundColor(Color.TRANSPARENT)
      card.cardElevation = 0f
    } else {
      card.setCardBackgroundColor(Settings.appSettings().drawerCardColor)
      card.cardElevation = Tool.dp2px(4f).toFloat()
    }

    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        _itemWidth = width / _layoutManager!!.spanCount
        _itemHeightPadding = Tool.dp2px(20f)
        updateAdapter(Settings.appLoader().getAllApps(context, false))
        Settings.appLoader().addUpdateListener(object : AppUpdateListener {
          override fun onAppUpdated(apps: List<App>): Boolean {
            updateAdapter(apps)
            return false
          }
        })
      }
    })
  }

  fun updateAdapter(apps: List<App>) {
    _apps = apps
    val items = ArrayList<IconLabelItem>()
    val labelColor = Settings.appSettings().drawerLabelColor
    for (i in apps.indices) {
      val app = apps[i]
      items.add(IconLabelItem(app.icon, app.label)
        .withIconSize(context, Settings.appSettings().iconSize)
        .withTextColor(labelColor)
        .withTextVisibility(Settings.appSettings().drawerShowLabel)
        .withIconPadding(context, 8)
        .withTextGravity(Gravity.CENTER)
        .withIconGravity(Gravity.TOP)
        .withOnClickAnimate(false)
        .withIsAppLauncher(true)
        .withOnClickListener(OnClickListener { v ->
          Tool.startApp(v.context, app, null)
        })
        .withOnLongClickListener(DragHandler.getLongClick(Item.newAppItem(app), DragAction.Action.DRAWER, null)))
    }
    _gridDrawerAdapter!!.set(items)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    if (_apps == null || _layoutManager == null) {
      super.onConfigurationChanged(newConfig)
      return
    }

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      setLandscapeValue()
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      setPortraitValue()
    }
    super.onConfigurationChanged(newConfig)
  }

  private fun setPortraitValue() {
    _layoutManager!!.spanCount = Settings.appSettings().drawerColumnCount
    _gridDrawerAdapter!!.notifyAdapterDataSetChanged()
  }

  private fun setLandscapeValue() {
    _layoutManager!!.spanCount = Settings.appSettings().drawerRowCount
    _gridDrawerAdapter!!.notifyAdapterDataSetChanged()
  }

  class AppDrawerGridAdapter : FastItemAdapter<IconLabelItem>(), INameableAdapter {

    override fun getCharacterForElement(element: Int): Char? {
      return if (_apps != null && element < _apps!!.size && _apps!![element] != null && _apps!![element].label.length > 0)
        _apps!![element].label[0]
      else
        '#'
    }
  }

  companion object {

    var _itemWidth: Int = 0
    var _itemHeightPadding: Int = 0

    private var _apps: List<App>? = null
  }
}

package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.R
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.IconLabelItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import java.util.ArrayList

class WorkspaceOptionView : FrameLayout {

  private val _actionRecyclerViews = arrayOfNulls<RecyclerView>(2)
  private val _actionAdapters = arrayOfNulls<FastItemAdapter<IconLabelItem>>(2)
  private var _desktopOptionViewListener: DesktopOptionViewListener? = null

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init()
  }

  fun setDesktopOptionViewListener(desktopOptionViewListener: DesktopOptionViewListener) {
    _desktopOptionViewListener = desktopOptionViewListener
  }

  fun updateHomeIcon(home: Boolean) {
    post {
      if (home) {
        _actionAdapters[0]!!.getAdapterItem(1)._icon = context.resources.getDrawable(R.drawable.ic_star_white_36dp)
      } else {
        _actionAdapters[0]!!.getAdapterItem(1)._icon = context.resources.getDrawable(R.drawable.ic_star_border_white_36dp)
      }
      _actionAdapters[0]!!.notifyAdapterItemChanged(1)
    }
  }

  fun updateLockIcon(lock: Boolean) {
    if (_actionAdapters.size == 0) return
    if (_actionAdapters[0]!!.getAdapterItemCount() == 0) return
    post {
      if (lock) {
        _actionAdapters[0]!!.getAdapterItem(2)._icon = context.resources.getDrawable(R.drawable.ic_lock_white_36dp)
      } else {
        _actionAdapters[0]!!.getAdapterItem(2)._icon = context.resources.getDrawable(R.drawable.ic_lock_open_white_36dp)
      }
      _actionAdapters[0]!!.notifyAdapterItemChanged(2)
    }
  }

  // override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
  //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
  //     setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
  //     return insets
  //   }
  //   return insets
  // }

  private fun init() {
    if (isInEditMode) {
      return
    }

    val paddingHorizontal = Tool.dp2px(42f)
    val typeface = Typeface.createFromAsset(context.assets, "RobotoCondensed-Regular.ttf")

    _actionAdapters[0] = FastItemAdapter<IconLabelItem>()
    _actionAdapters[1] = FastItemAdapter<IconLabelItem>()

    _actionRecyclerViews[0] = createRecyclerView(_actionAdapters[0]!!, Gravity.TOP or Gravity.CENTER_HORIZONTAL, paddingHorizontal)
    _actionRecyclerViews[1] = createRecyclerView(_actionAdapters[1]!!, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, paddingHorizontal)

    val clickListener = com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem> { v, adapter, item, position ->
      if (_desktopOptionViewListener != null) {
        val id = item.identifier.toInt()
        if (id == R.string.home) {
          updateHomeIcon(true)
          _desktopOptionViewListener!!.onSetPageAsHome()
        } else if (id == R.string.remove) {
          if (!Settings.appSettings().desktopLock) {
            _desktopOptionViewListener!!.onRemovePage()
          } else {
            Tool.toast(context, "CellContainer is locked.")
          }
        } else if (id == R.string.widget) {
          if (!Settings.appSettings().desktopLock) {
            _desktopOptionViewListener!!.onPickWidget(v)
          } else {
            Tool.toast(context, "CellContainer is locked.")
          }
        } else if (id == R.string.action) {
          if (!Settings.appSettings().desktopLock) {
            _desktopOptionViewListener!!.onPickDesktopAction()
          } else {
            Tool.toast(context, "CellContainer is locked.")
          }
        } else if (id == R.string.lock) {
          Settings.appSettings().desktopLock = !Settings.appSettings().desktopLock
          updateLockIcon(Settings.appSettings().desktopLock)
        } else if (id == R.string.pref_title__settings) {
          _desktopOptionViewListener!!.onLaunchSettings()
        } else {
          return@OnClickListener false
        }
        return@OnClickListener true
      }
      false
    }

    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        val itemWidth = (width - 2 * paddingHorizontal) / 3
        initItems(typeface, clickListener, itemWidth)
      }
    })
  }

  private fun initItems(typeface: Typeface, clickListener: com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem>, itemWidth: Int) {
    val itemsTop = ArrayList<IconLabelItem>()
    itemsTop.add(createItem(R.drawable.ic_delete_white_36dp, R.string.remove, typeface, itemWidth))
    itemsTop.add(createItem(R.drawable.ic_star_white_36dp, R.string.home, typeface, itemWidth))
    itemsTop.add(createItem(R.drawable.ic_lock_open_white_36dp, R.string.lock, typeface, itemWidth))
    _actionAdapters[0]!!.set(itemsTop)
    _actionAdapters[0]!!.withOnClickListener(clickListener)

    val itemsBottom = ArrayList<IconLabelItem>()
    itemsBottom.add(createItem(R.drawable.ic_dashboard_white_36dp, R.string.widget, typeface, itemWidth))
    itemsBottom.add(createItem(R.drawable.ic_launch_white_36dp, R.string.action, typeface, itemWidth))
    itemsBottom.add(createItem(R.drawable.ic_settings_launcher_white_36dp, R.string.pref_title__settings, typeface, itemWidth))
    _actionAdapters[1]!!.set(itemsBottom)
    _actionAdapters[1]!!.withOnClickListener(clickListener)

    ((_actionRecyclerViews[0]!!.parent as View).layoutParams as ViewGroup.MarginLayoutParams).topMargin = Tool.dp2px(4f)
  }

  private fun createRecyclerView(adapter: FastAdapter<IconLabelItem>, gravity: Int, paddingHorizontal: Int): RecyclerView {
    val actionRecyclerView = RecyclerView(context)
    val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    actionRecyclerView.clipToPadding = false
    actionRecyclerView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
    actionRecyclerView.layoutManager = linearLayoutManager
    actionRecyclerView.adapter = adapter
    actionRecyclerView.overScrollMode = View.OVER_SCROLL_ALWAYS
    val actionRecyclerViewLP = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    actionRecyclerViewLP.gravity = gravity

    addView(actionRecyclerView, actionRecyclerViewLP)
    return actionRecyclerView
  }

  private fun createItem(icon: Int, label: Int, typeface: Typeface, width: Int): IconLabelItem {
    return IconLabelItem(context, icon, label)
      .withIdentifier(label.toLong())
      .withOnClickListener(null)
      .withTextColor(Color.WHITE)
      .withIconPadding(context, 4)
      .withIconGravity(Gravity.TOP)
      .withWidth(width)
      .withTextGravity(Gravity.CENTER)
  }

  interface DesktopOptionViewListener {
    fun onRemovePage()

    fun onSetPageAsHome()

    fun onLaunchSettings()

    fun onPickDesktopAction()

    fun onPickWidget(view: View?)
  }
}

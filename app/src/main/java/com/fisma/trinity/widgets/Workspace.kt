package com.fisma.trinity.widgets

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.Constants
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.ItemViewFactory
import com.fisma.trinity.viewutil.WorkspaceCallback
import com.fisma.trinity.viewutil.WorkspaceGestureListener
import java.util.ArrayList

class Workspace : ViewPager, WorkspaceCallback {
  var desktopEditListener: OnDesktopEditListener? = null
  var inEditMode: Boolean = false
  private var _pageIndicator: PagerIndicator? = null

  private val _pages = ArrayList<CellContainer>()
  private val _previousDragPoint = Point()

  private val _coordinate = Point(-1, -1)
  private var _adapter: DesktopAdapter? = null
  private var _previousItem: Item? = null
  private var _previousItemView: View? = null
  private var _previousPage: Int = 0

  val pages: MutableList<CellContainer>
    get() = _pages

  val isCurrentPageEmpty: Boolean
    get() = currentPage.childCount == 0

  val currentPage: CellContainer
    get() = _pages[currentItem]

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
    mInstance = this
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    alreadyMeasured = true
  }

  inner class DesktopAdapter(private val _desktop: Workspace) : PagerAdapter() {

    private val gestureListener: SimpleFingerGestures.OnFingerGestureListener
      get() = WorkspaceGestureListener(_desktop, Settings.desktopGestureCallback())

    private val itemLayout: CellContainer
      get() {
        val context = _desktop.context
        val layout = CellContainer(context)
        val mySfg = SimpleFingerGestures()
        mySfg.setOnFingerGestureListener(gestureListener)
        layout.setGestures(mySfg)
        layout.setGridSize(Settings.appSettings().desktopColumnCount, Settings.appSettings().desktopRowCount)
        layout.setOnClickListener { exitDesktopEditMode() }
        layout.setOnLongClickListener {
          enterDesktopEditMode()
          true
        }

        return layout
      }

    init {

      _desktop.pages.clear()
      var count = HomeActivity._db.desktop.size
      if (count == 0) count++
      for (i in 0 until count) {
        _desktop.pages.add(itemLayout)
      }
    }

    fun addPageLeft() {
      _desktop.pages.add(0, itemLayout)
      notifyDataSetChanged()
    }

    fun addPageRight() {
      _desktop.pages.add(itemLayout)
      notifyDataSetChanged()
    }

    fun removePage(position: Int, deleteItems: Boolean) {
      if (deleteItems) {
        if (position == 0) return
        val page = _desktop.pages[position]
        val cells = page.allCells
        for (view in cells) {
          val item = view.tag
          if (item is Item) {
            HomeActivity._db.deleteItem(item, true)
          }
        }
      }
      _desktop.pages.removeAt(position)
      notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any): Int {
      return POSITION_NONE
    }

    override fun getCount(): Int {
      return _desktop.pages.size
    }

    override fun isViewFromObject(p1: View, p2: Any): Boolean {
      return p1 === p2
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
      container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
      val layout = _desktop.pages[position]
      container.addView(layout)
      return layout
    }

    private fun enterDesktopEditMode() {
      val scaleFactor = 0.8f
      val translateFactor = Tool.dp2px(40f).toFloat()
      for (v in _desktop.pages) {
        v.setBlockTouch(true)
        v.animateBackgroundShow()
        val animation = v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor)
        animation.interpolator = AccelerateDecelerateInterpolator()
      }
      _desktop.inEditMode = true
      if (_desktop.desktopEditListener != null) {
        val desktopEditListener = _desktop.desktopEditListener
        desktopEditListener!!.onDesktopEdit()
      }
    }

    fun exitDesktopEditMode() {
      val scaleFactor = 1.0f
      val translateFactor = 0.0f
      for (v in _desktop.pages) {
        v.setBlockTouch(false)
        v.animateBackgroundHide()
        val animation = v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor)
        animation.interpolator = AccelerateDecelerateInterpolator()
      }
      _desktop.inEditMode = false
      if (_desktop.desktopEditListener != null) {
        val desktopEditListener = _desktop.desktopEditListener
        desktopEditListener!!.onFinishDesktopEdit()
      }
    }
  }

  fun setBlockTouch(shouldBlock: Boolean) {
    for (v in pages) {
      v.setBlockTouch(shouldBlock)
    }
  }

  fun setPageIndicator(pageIndicator: PagerIndicator) {
    _pageIndicator = pageIndicator
  }

  fun initWorkspace() {
    _adapter = DesktopAdapter(this)
    adapter = _adapter

    currentItem = Settings.appSettings().desktopPageCurrent

    if (Settings.appSettings().desktopShowIndicator && _pageIndicator != null) {
      _pageIndicator!!.setViewPager(this)
    }

    val columns = Settings.appSettings().desktopColumnCount
    val rows = Settings.appSettings().desktopRowCount
    val desktopItems = HomeActivity._db.desktop
    for (pageCount in desktopItems.indices) {
      val page = desktopItems[pageCount]
      _pages[pageCount].removeAllViews()
      for (itemCount in page.indices) {
        val item = page[itemCount]
        if (item.x + item.spanX <= columns && item.y + item.spanY <= rows) {
          if (item.type == Item.Type.APPWIDGET) {
            addWidgetToPage(item, pageCount)
          } else {
            addItemToPage(item, pageCount)
          }
        }
      }
    }
  }

  fun addPageRight(showGrid: Boolean) {
    val previousPage = currentItem
    _adapter!!.addPageRight()
    currentItem = previousPage + 1
    if (Settings.appSettings().desktopShowGrid) {
      for (cellContainer in _pages) {
        cellContainer.setHideGrid(!showGrid)
      }
    }
    _pageIndicator!!.invalidate()
  }

  fun addPageLeft(showGrid: Boolean) {
    val previousPage = currentItem
    _adapter!!.addPageLeft()
    setCurrentItem(previousPage + 1, false)
    currentItem = previousPage - 1
    if (Settings.appSettings().desktopShowGrid) {
      for (cellContainer in _pages) {
        cellContainer.setHideGrid(!showGrid)
      }
    }
    _pageIndicator!!.invalidate()
  }

  fun removePage(position: Int) {
    _adapter!!.removePage(position, true)
    if (_pages.size == 0)
      addPageRight(false)
  }

  fun removeCurrentPage() {
    val previousPage = currentItem
    _adapter!!.removePage(currentItem, true)
    if (_pages.size == 0) {
      addPageRight(false)
      _adapter!!.exitDesktopEditMode()
    } else {
      setCurrentItem(previousPage, true)
      _pageIndicator!!.invalidate()
    }
  }

  fun updateWidgetPreviewProjection(x: Int, y: Int, widgetInfo: Item) {
    Log.d(TAG, "updateWIdgetPreviewProjection()")
    val launcher = HomeActivity.launcher
    val dragNDropView = launcher.dragLayer
    val state = currentPage.peekItemAndSwap(x, y, _coordinate, widgetInfo.spanX, widgetInfo.spanY)
    if (_coordinate != _previousDragPoint) {
      dragNDropView.cancelFolderPreview()
    }
    var projectionImage: Bitmap? = DragHandler._cachedDragBitmap
    if (widgetInfo is AppWidget) {
      projectionImage = widgetInfo.projectionImage
    }

    _previousDragPoint.set(_coordinate.x, _coordinate.y)
    when (state) {
      CellContainer.DragState.CurrentNotOccupied -> currentPage.projectImageOutlineAt(_coordinate, projectionImage, widgetInfo.spanX, widgetInfo.spanY, Item.Type.APPWIDGET)
      CellContainer.DragState.OutOffRange, CellContainer.DragState.ItemViewNotFound -> {
      }
      CellContainer.DragState.CurrentOccupied -> {
        val type = dragNDropView.dragItem!!.type
        for (page in _pages) {
          page.clearCachedOutlineBitmap()
        }
        val child = currentPage.coordinateToChildView(_coordinate)
        if (type != Item.Type.WIDGET && type != Item.Type.APPWIDGET && child is AppItemView) {
          dragNDropView.showFolderPreviewAt(this, currentPage.cellWidth * (_coordinate.x + 0.5f), currentPage.cellHeight * (_coordinate.y + 0.5f))
        }
      }
      else -> {
      }
    }
  }

  fun updateIconProjection(x: Int, y: Int) {
    Log.d(TAG, "updateIconProjection()")
    val launcher = HomeActivity.launcher
    val dragNDropView = launcher.dragLayer
    val state = currentPage.peekItemAndSwap(x, y, _coordinate)
    if (_coordinate != _previousDragPoint) {
      dragNDropView.cancelFolderPreview()
    }
    _previousDragPoint.set(_coordinate.x, _coordinate.y)
    when (state) {
      CellContainer.DragState.CurrentNotOccupied -> currentPage.projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap)
      CellContainer.DragState.OutOffRange, CellContainer.DragState.ItemViewNotFound -> {
      }
      CellContainer.DragState.CurrentOccupied -> {
        val type = dragNDropView.dragItem!!.type
        for (page in _pages) {
          page.clearCachedOutlineBitmap()
        }
        if (type != Item.Type.WIDGET && currentPage.coordinateToChildView(_coordinate) is AppItemView) {
          dragNDropView.showFolderPreviewAt(this, currentPage.cellWidth * (_coordinate.x + 0.5f), currentPage.cellHeight * (_coordinate.y + 0.5f))
        }
      }
      else -> {
      }
    }
  }

  override fun setLastItem(item: Item, view: View) {
    _previousPage = currentItem
    _previousItemView = view
    _previousItem = item
    currentPage.removeView(view)
  }

  override fun revertLastItem() {
    if (_previousItemView != null) {
      if (_adapter!!.count >= _previousPage && _previousPage > -1) {
        val cellContainer = _pages[_previousPage]
        cellContainer.addViewToGrid(_previousItemView!!)
        _previousItem = null
        _previousItemView = null
        _previousPage = -1
      }
    }
  }

  override fun consumeLastItem() {
    _previousItem = null
    _previousItemView = null
    _previousPage = -1
  }

  private fun addWidgetToPage(item: Item, page: Int): Boolean {
    val itemView = ItemViewFactory.getWidgetView(context, this, DragAction.Action.DESKTOP, item) // TODO see if this fixes SD card bug
    // apps that are located on SD card disappear on reboot
    // might be from this line of code so comment out for now
    // HomeActivity._db.deleteItem(item, true);
      ?: return false
    item._location = Constants.ItemPosition.Desktop

    _pages[page].addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
    return true
  }

  override fun addItemToPage(item: Item, page: Int): Boolean {
    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item) // TODO see if this fixes SD card bug
    // apps that are located on SD card disappear on reboot
    // might be from this line of code so comment out for now
    // HomeActivity._db.deleteItem(item, true);
      ?: return false
    item._location = Constants.ItemPosition.Desktop

    _pages[page].addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)

    if (item.type == Item.Type.APPWIDGET) {
      AppWidgetResizeFrame.updateResizedWidget(itemView as FrameLayout, item)
      AppWidgetResizeFrame.showResizeFrame()
    }
    return true
  }

  override fun addItemToPoint(item: Item, x: Int, y: Int): Boolean {
    val positionToLayoutPrams = currentPage.coordinateToLayoutParams(x, y, item.spanX, item.spanY)
      ?: return false
    item._location = Constants.ItemPosition.Desktop
    item.x = positionToLayoutPrams.x
    item.y = positionToLayoutPrams.y
    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item)
    if (itemView != null) {
      itemView.layoutParams = positionToLayoutPrams
      currentPage.addView(itemView)
    }

    if (item.type == Item.Type.APPWIDGET) {
      AppWidgetResizeFrame.updateResizedWidget(itemView as FrameLayout, item)
      AppWidgetResizeFrame.showResizeFrame()
    }
    return true
  }

  override fun addItemToCell(item: Item, x: Int, y: Int): Boolean {
    item._location = Constants.ItemPosition.Desktop
    item.x = x
    item.y = y
    val itemView = ItemViewFactory.getItemView(context, this, DragAction.Action.DESKTOP, item)
      ?: return false
    currentPage.addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
    return true
  }

  override fun removeItem(view: View, animate: Boolean) {
    if (animate) {
      view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction {
        if (currentPage == view.parent) {
          currentPage.removeView(view)
        }
      }
    } else if (currentPage == view.parent) {
      currentPage.removeView(view)
    }
  }

  interface OnDesktopEditListener {
    fun onDesktopEdit()

    fun onFinishDesktopEdit()
  }

  companion object {
    const val TAG = "Workspace"
    @SuppressLint("StaticFieldLeak")
    var mInstance: Workspace? = null
    var alreadyMeasured: Boolean = false

    fun isInEditMode(): Boolean {
      return mInstance!!.inEditMode
    }

    fun isAtFirstPage(): Boolean {
      return mInstance!!.currentItem == 0
    }

    fun blockTouch() {
      mInstance!!.setBlockTouch(true)
    }

    fun unblockTouch() {
      mInstance!!.setBlockTouch(false)
    }

    fun handleOnDropOver(dropItem: Item?, item: Item?, itemView: View, parent: CellContainer, page: Int, itemPosition: Constants.ItemPosition, callback: WorkspaceCallback): Boolean {
      if (item != null) {
        if (dropItem != null) {
          val type = item.type
          if (type != null) {
            Log.d(TAG, "handleOnDropOver")
            when (type) {
              Item.Type.APP, Item.Type.SHORTCUT -> {
                if (Item.Type.APP == dropItem.type || Item.Type.SHORTCUT == dropItem.type) {
                  parent.removeView(itemView)
                  val group = Item.newGroupItem()
                  group.groupItems.add(item)
                  group.groupItems.add(dropItem)
                  group.x = item.x
                  group.y = item.y
                  HomeActivity._db.saveItem(dropItem, page, itemPosition)
                  HomeActivity._db.saveItem(item, Constants.ItemState.Hidden)
                  HomeActivity._db.saveItem(dropItem, Constants.ItemState.Hidden)
                  HomeActivity._db.saveItem(group, page, itemPosition)
                  callback.addItemToPage(group, page)
                  val launcher = HomeActivity.launcher
                  if (launcher != null) {
                    launcher.workspace.consumeLastItem()
                    launcher.dock.consumeLastItem()
                  }
                  return true
                }
                if ((Item.Type.APP == dropItem.type || Item.Type.SHORTCUT == dropItem.type) && item.groupItems.size < GroupPopupView.GroupDef._maxItem) {
                  parent.removeView(itemView)
                  item.groupItems.add(dropItem)
                  HomeActivity._db.saveItem(dropItem, page, itemPosition)
                  HomeActivity._db.saveItem(dropItem, Constants.ItemState.Hidden)
                  HomeActivity._db.saveItem(item, page, itemPosition)
                  callback.addItemToPage(item, page)
                  val launcher = HomeActivity.launcher
                  if (launcher != null) {
                    launcher.workspace.consumeLastItem()
                    launcher.dock.consumeLastItem()
                  }
                  return true
                }
              }
              Item.Type.GROUP -> if ((Item.Type.APP == dropItem.type || Item.Type.SHORTCUT == dropItem.type) && item.groupItems.size < GroupPopupView.GroupDef._maxItem) {
                parent.removeView(itemView)
                item.groupItems.add(dropItem)
                HomeActivity._db.saveItem(dropItem, page, itemPosition)
                HomeActivity._db.saveItem(dropItem, Constants.ItemState.Hidden)
                HomeActivity._db.saveItem(item, page, itemPosition)
                callback.addItemToPage(item, page)
                val launcher = HomeActivity.launcher
                if (launcher != null) {
                  launcher.workspace.consumeLastItem()
                  launcher.dock.consumeLastItem()
                }
                return true
              }
              else -> {
              }
            }
          }
          return false
        }
      }
      return false
    }
  }
}

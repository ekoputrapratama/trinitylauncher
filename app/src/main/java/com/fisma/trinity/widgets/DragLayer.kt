package com.fisma.trinity.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.interfaces.DropTargetListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.PopupIconLabelItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import java.util.ArrayList
import java.util.HashMap


class DragLayer(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
  companion object {
    const val TAG: String = "DragLayer"
    const val DRAG_THRESHOLD: Float = 20.0f
    var dragging: Boolean = false
      private set
    var resizing: Boolean = false

    fun isDragging(): Boolean {
      return dragging
    }

    fun isResizing(): Boolean {
      return resizing
    }
  }


  var dragAction: DragAction.Action? = null
    private set
  var dragExceedThreshold: Boolean = false
    private set
  var dragItem: Item? = null
    private set
  val dragLocation: PointF
  private val _dragLocationConverted: PointF
  private val _dragLocationStart: PointF
  private var _dragView: View? = null

  private var _folderPreviewScale: Float = 0.toFloat()
  private var _overlayIconScale: Float = 0.toFloat()
  private val _overlayPopup: RecyclerView
  private val _overlayPopupAdapter: FastItemAdapter<PopupIconLabelItem>
  private var _overlayPopupShowing: Boolean = false
  private val _overlayView: OverlayView
  private val _paint: Paint
  private val _previewLocation: PointF
  private val _registeredDropTargetEntries: HashMap<DropTargetListener, DragFlag>
  private var _showFolderPreview: Boolean = false
  private val _slideInLeftAnimator: SlideInLeftAnimator
  private val _slideInRightAnimator: SlideInRightAnimator
  private val _tempArrayOfInt2: IntArray

  private val uninstallItemIdentifier = 83
  private val infoItemIdentifier = 84
  private val editItemIdentifier = 85
  private val removeItemIdentifier = 86

  private val uninstallItem = PopupIconLabelItem(R.string.uninstall, R.drawable.ic_delete_dark_24dp).withIdentifier(uninstallItemIdentifier.toLong())
  private val infoItem = PopupIconLabelItem(R.string.info, R.drawable.ic_info_outline_dark_24dp).withIdentifier(infoItemIdentifier.toLong())
  private val editItem = PopupIconLabelItem(R.string.edit, R.drawable.ic_edit_black_24dp).withIdentifier(editItemIdentifier.toLong())
  private val removeItem = PopupIconLabelItem(R.string.remove, R.drawable.ic_close_dark_24dp).withIdentifier(removeItemIdentifier.toLong())

  private val mResizeFrame: AppWidgetResizeFrame

  init {
    _paint = Paint(1)
    _registeredDropTargetEntries = HashMap()
    _tempArrayOfInt2 = IntArray(2)
    dragLocation = PointF()
    _dragLocationStart = PointF()
    _dragLocationConverted = PointF()
    _overlayIconScale = 1.0f
    _overlayPopupAdapter = FastItemAdapter()
    _previewLocation = PointF()
    _slideInLeftAnimator = SlideInLeftAnimator(AccelerateDecelerateInterpolator())
    _slideInRightAnimator = SlideInRightAnimator(AccelerateDecelerateInterpolator())
    _paint.isFilterBitmap = true
    _paint.color = -1
    _overlayView = OverlayView()
    _overlayPopup = RecyclerView(context)
    _overlayPopup.visibility = View.INVISIBLE
    _overlayPopup.alpha = 0.0f
    _overlayPopup.overScrollMode = 2
    _overlayPopup.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    _overlayPopup.itemAnimator = _slideInLeftAnimator
    _overlayPopup.adapter = _overlayPopupAdapter
    mResizeFrame = AppWidgetResizeFrame(context, this)
    mResizeFrame.visibility = View.INVISIBLE
    mResizeFrame.alpha = 0f
    addView(_overlayView, LayoutParams(-1, -1))
    addView(_overlayPopup, LayoutParams(-2, -2))
    addView(mResizeFrame, LayoutParams(-1, -1))
    setWillNotDraw(false)
  }

  class DragFlag {
    var previousOutside = true
    var shouldIgnore: Boolean = false
  }

  @SuppressLint("ResourceType")
  inner class OverlayView : View(this@DragLayer.context) {
    init {
      setWillNotDraw(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
      if (event == null || event.actionMasked != 0 || dragging || !this@DragLayer._overlayPopupShowing) {
        return super.onTouchEvent(event)
      }
      this@DragLayer.collapse()
      return true
    }

    override fun onDraw(canvas: Canvas?) {
      super.onDraw(canvas)
      if (canvas == null || DragHandler._cachedDragBitmap == null || dragLocation.equals(-1f, -1f))
        return

      val x = dragLocation.x
      val y = dragLocation.y

      if (dragging) {
        canvas.save()
        _overlayIconScale = Tool.clampFloat(_overlayIconScale + 0.05f, 1f, 1.1f)
        canvas.scale(_overlayIconScale, _overlayIconScale, x + DragHandler._cachedDragBitmap!!.width / 2, y + DragHandler._cachedDragBitmap!!.height / 2)
        canvas.drawBitmap(DragHandler._cachedDragBitmap!!, x - DragHandler._cachedDragBitmap!!.width / 2, y - DragHandler._cachedDragBitmap!!.height / 2, _paint)
        canvas.restore()
      }

      if (dragging)
        invalidate()
    }
  }


  fun showFolderPreviewAt(fromView: View, x: Float, y: Float) {
    if (!_showFolderPreview) {
      _showFolderPreview = true
      convertPoint(fromView, this, x, y)
      _folderPreviewScale = 0.0f
      invalidate()
    }
  }

  fun convertPoint(fromView: View, toView: View, x: Float, y: Float) {
    val fromCoordinate = IntArray(2)
    val toCoordinate = IntArray(2)
    fromView.getLocationOnScreen(fromCoordinate)
    toView.getLocationOnScreen(toCoordinate)
    _previewLocation.set(fromCoordinate[0] - toCoordinate[0] + x, fromCoordinate[1] - toCoordinate[1] + y)
  }

  fun cancelFolderPreview() {
    _showFolderPreview = false
    _previewLocation.set(-1.0f, -1.0f)
    invalidate()
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    if (canvas != null && _showFolderPreview && !_previewLocation.equals(-1.0f, -1.0f)) {
      _folderPreviewScale += 0.08f
      _folderPreviewScale = Tool.clampFloat(_folderPreviewScale, 0.5f, 1.0f)
      canvas.drawCircle(_previewLocation.x, _previewLocation.y, Tool.dp2px((Settings.appSettings().desktopIconSize / 2 + 10).toFloat()).toFloat() * _folderPreviewScale, _paint)
    }
    if (_showFolderPreview) {
      invalidate()
    }
  }

  override fun onViewAdded(child: View?) {
    super.onViewAdded(child)
    _overlayView.bringToFront()
    _overlayPopup.bringToFront()
    mResizeFrame.bringToFront()
  }

  fun showPopupMenuForItem(x: Float, y: Float, popupItem: List<PopupIconLabelItem>, listener: com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem>) {
    if (!_overlayPopupShowing) {
      _overlayPopupShowing = true
      _overlayPopup.visibility = View.VISIBLE
      _overlayPopup.translationX = x
      _overlayPopup.translationY = y
      _overlayPopup.alpha = 1.0f
      _overlayPopupAdapter.add(popupItem)
      _overlayPopupAdapter.withOnClickListener(listener)
    }
  }

  fun setPopupMenuShowDirection(left: Boolean) {
    if (left) {
      _overlayPopup.itemAnimator = _slideInLeftAnimator
    } else {
      _overlayPopup.itemAnimator = _slideInRightAnimator
    }
  }

  fun collapse() {
    if (_overlayPopupShowing) {
      _overlayPopupShowing = false
      _overlayPopup.animate().alpha(0.0f).withEndAction {
        _overlayPopup.visibility = View.INVISIBLE
        _overlayPopupAdapter.clear()
      }
      if (!dragging) {
        _dragView = null
        dragItem = null
        dragAction = null
      }
    }
  }

  fun startDragNDropOverlay(view: View, item: Item, action: DragAction.Action) {
    dragging = true
    dragExceedThreshold = false
    _overlayIconScale = 0.0f
    _dragView = view
    dragItem = item
    dragAction = action
    _dragLocationStart.set(dragLocation)
    for ((key, value) in _registeredDropTargetEntries) {
      convertPoint(key.view)
      value.shouldIgnore = !key.onStart(dragAction!!, _dragLocationConverted, isViewContains(key.view, dragLocation.x.toInt(), dragLocation.y.toInt()))
    }
    _overlayView.invalidate()
  }

  override fun onDetachedFromWindow() {
    cancelAllDragNDrop()
    super.onDetachedFromWindow()
  }

  fun cancelAllDragNDrop() {
    dragging = false
    if (!_overlayPopupShowing) {
      _dragView = null
      dragItem = null
      dragAction = null
    }
    for ((key) in _registeredDropTargetEntries) {
      key.onEnd()
    }
  }

  fun registerDropTarget(targetListener: DropTargetListener) {
    _registeredDropTargetEntries[targetListener] = DragFlag()
  }

  override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
    if (event != null && event.actionMasked == 1 && dragging) {
      handleDragFinished()
    }
    if (dragging) {
      return true
    }
    if (event != null) {
      dragLocation.set(event.x, event.y)
    }
    if(mResizeFrame.shouldInterceptTouchEvent(event)) {
      resizing = true
      return true
    }
    return super.onInterceptTouchEvent(event)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    if (event != null) {
      if (dragging) {
        Log.d(TAG, "set drag location x=${event.x} y=${event.y}")
        dragLocation.set(event.x, event.y)
        when (event.actionMasked) {
          1 -> handleDragFinished()
          2 -> handleMovement()
          else -> {
          }
        }
        return if (dragging) {
          true
        } else super.onTouchEvent(event)
      } else if(resizing) {
        when(event.actionMasked) {
          1 -> {
            resizing = false
            mResizeFrame.onTouchEnd()
          }
          2 -> mResizeFrame.onTouchMove(event)
        }
        return true
      }
    }
    return super.onTouchEvent(event)
  }

  fun showItemPopup(homeActivity: HomeActivity) {
    val itemList = ArrayList<PopupIconLabelItem>()
    when (dragItem!!.type) {
      Item.Type.APP, Item.Type.SHORTCUT, Item.Type.GROUP -> {
        if (dragAction == DragAction.Action.DRAWER) {
          itemList.add(uninstallItem)
          itemList.add(infoItem)
        } else {
          itemList.add(editItem)
          itemList.add(removeItem)
          itemList.add(infoItem)
        }
      }
      Item.Type.ACTION -> {
        itemList.add(editItem)
        itemList.add(removeItem)
      }
      Item.Type.WIDGET -> {
        itemList.add(removeItem)
      }
      else -> {
      }
    }

    var x = dragLocation.x - HomeActivity._itemTouchX + Tool.dp2px(10f)
    var y = dragLocation.y - HomeActivity._itemTouchY - Tool.dp2px((46 * itemList.size).toFloat()).toFloat()

    if (x + Tool.dp2px(200f) > width) {
      setPopupMenuShowDirection(false)
      x = dragLocation.x - HomeActivity._itemTouchX + homeActivity.workspace.currentPage.cellWidth - Tool.dp2px(200f).toFloat() - Tool.dp2px(10f).toFloat()
    } else {
      setPopupMenuShowDirection(true)
    }

    if (y < 0)
      y = dragLocation.y - HomeActivity._itemTouchY + homeActivity.workspace.currentPage.cellHeight.toFloat() + Tool.dp2px(4f).toFloat()
    else
      y -= Tool.dp2px(4f).toFloat()

    showPopupMenuForItem(x, y, itemList, com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem> { v, adapter, item, position ->
      val dragItem: Item? = this.dragItem
      if (dragItem != null) {
        when (item.identifier.toInt()) {
          uninstallItemIdentifier -> {
            homeActivity.onUninstallItem(dragItem)
          }
          editItemIdentifier -> {
            HomeActivity.HomeAppEditApplier(homeActivity).onEditItem(dragItem)
          }
          removeItemIdentifier -> {
            homeActivity.onRemoveItem(dragItem)
          }
          infoItemIdentifier -> {
            homeActivity.onInfoItem(dragItem)
          }
        }
      }
      collapse()
      true
    })
  }

  private fun handleMovement() {
    if (!dragExceedThreshold && (Math.abs(_dragLocationStart.x - dragLocation.x) > DRAG_THRESHOLD || Math.abs(_dragLocationStart.y - dragLocation.y) > DRAG_THRESHOLD)) {
      dragExceedThreshold = true
      for ((key, value) in _registeredDropTargetEntries) {
        if (!value.shouldIgnore) {
          convertPoint(key.view)

          key.onStartDrag(dragAction!!, _dragLocationConverted)
        }
      }
    }
    if (dragExceedThreshold) {
      collapse()
    }
    for (dropTarget2 in _registeredDropTargetEntries.entries) {
      var dropTargetListener = dropTarget2.key
      if (!dropTarget2.value.shouldIgnore) {
        convertPoint(dropTarget2.key.view)
        if (isViewContains(dropTarget2.key.view, dragLocation.x.toInt(), dragLocation.y.toInt())) {

          dropTargetListener.onMove(_dragView!!, dragAction!!, _dragLocationConverted)
          if (dropTarget2.value.previousOutside) {
            dropTarget2.value.previousOutside = false
            dropTargetListener = dropTarget2.key
            dropTargetListener.onEnter(dragAction!!, _dragLocationConverted)
          }
        } else if (!dropTarget2.value.previousOutside) {
          dropTarget2.value.previousOutside = true
          dropTargetListener = dropTarget2.key
          dropTargetListener.onExit(dragAction!!, _dragLocationConverted)
        }
      }
    }
  }

  private fun handleDragFinished() {
    dragging = false
    for ((key, value) in _registeredDropTargetEntries) {
      if (!value.shouldIgnore) {
        if (isViewContains(key.view, dragLocation.x.toInt(), dragLocation.y.toInt())) {
          convertPoint(key.view)
          val dropTargetListener = key
          dropTargetListener.onDrop(dragAction!!, _dragLocationConverted, dragItem!!)
        }
      }
    }
    for ((key) in _registeredDropTargetEntries) {
      key.onEnd()
    }
    cancelFolderPreview()
  }

  fun convertPoint(toView: View) {
    val fromCoordinate = IntArray(2)
    val toCoordinate = IntArray(2)
    getLocationOnScreen(fromCoordinate)
    toView.getLocationOnScreen(toCoordinate)
    _dragLocationConverted.set((fromCoordinate[0] - toCoordinate[0]).toFloat() + dragLocation.x, (fromCoordinate[1] - toCoordinate[1]).toFloat() + dragLocation.y)
  }

  private fun isViewContains(view: View, rx: Int, ry: Int): Boolean {
    view.getLocationOnScreen(_tempArrayOfInt2)
    val x = _tempArrayOfInt2[0]
    val y = _tempArrayOfInt2[1]
    val w = view.width
    val h = view.height
    return !(rx < x || rx > x + w || ry < y || ry > y + h)
  }
}
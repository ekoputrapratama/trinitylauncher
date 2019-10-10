package com.fisma.trinity.widgets

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.PopupWindow
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.Animation
import com.fisma.trinity.util.DragAction
import com.fisma.trinity.util.DragHandler
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.GroupIconDrawable
import com.fisma.trinity.viewutil.ItemViewFactory
import com.fisma.trinity.viewutil.WorkspaceCallback
import io.codetail.animation.ViewAnimationUtils
import io.codetail.widget.RevealFrameLayout
import net.gsantner.opoc.util.ContextUtils


class GroupPopupView : RevealFrameLayout {
  private var _isShowing: Boolean = false
  private var _popupCard: CardView? = null
  private var _cellContainer: CellContainer? = null
  private var _dismissListener: PopupWindow.OnDismissListener? = null
  private var _folderAnimator: Animator? = null
  private var _cx: Int = 0
  private var _cy: Int = 0
  private var _textViewGroupName: TextView? = null

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
  }

  private fun init() {
    if (isInEditMode) {
      return
    }
    _popupCard = LayoutInflater.from(context).inflate(R.layout.view_group_popup, this, false) as CardView
    // set the CardView color
    val color = Settings.appSettings().desktopFolderColor
    val alpha = Color.alpha(color)
    _popupCard!!.setCardBackgroundColor(color)
    // remove elevation if CardView's background is transparent to avoid weird shadows because CardView does not support transparent backgrounds
    if (alpha == 0) {
      _popupCard!!.cardElevation = 0f
    }
    _cellContainer = _popupCard!!.findViewById(R.id.group)

    bringToFront()

    setOnClickListener {
      if (_dismissListener != null) {
        _dismissListener!!.onDismiss()
      }
      collapse()
    }

    addView(_popupCard)
    _popupCard!!.visibility = View.INVISIBLE
    visibility = View.INVISIBLE

    _textViewGroupName = _popupCard!!.findViewById(R.id.group_popup_label)
  }


  fun showPopup(item: Item, itemView: View, callback: WorkspaceCallback): Boolean {
    if (_isShowing || visibility == View.VISIBLE) return false
    _isShowing = true

    val cu = ContextUtils(_textViewGroupName!!.context)
    val label = item.label
    _textViewGroupName!!.visibility = if (label.isEmpty()) View.GONE else View.VISIBLE
    _textViewGroupName!!.setText(label)
    _textViewGroupName!!.setTextColor(if (cu.shouldColorOnTopBeLight(Settings.appSettings().desktopFolderColor)) Color.WHITE else Color.BLACK)
    _textViewGroupName!!.setTypeface(null, Typeface.BOLD)
    cu.freeContextRef()

    val context = itemView.context
    val cellSize = GroupPopupView.GroupDef.getCellSize(item.groupItems.size)
    _cellContainer!!.setGridSize(cellSize[0], cellSize[1])

    val iconSize = Tool.dp2px(Settings.appSettings().desktopIconSize.toFloat())
    val textSize = Tool.dp2px(22f)
    val contentPadding = Tool.dp2px(6f)

    for (x2 in 0 until cellSize[0]) {
      for (y2 in 0 until cellSize[1]) {
        if (y2 * cellSize[0] + x2 > item.groupItems.size - 1) {
          continue
        }
        val groupItem = item.groupItems.get(y2 * cellSize[0] + x2) ?: continue
        val view = ItemViewFactory.getItemView(getContext(), callback, DragAction.Action.DESKTOP, groupItem)
        view!!.setOnLongClickListener(OnLongClickListener {
          if (Settings.appSettings().desktopLock) return@OnLongClickListener false

          removeItem(context, item, groupItem, itemView as AppItemView)

          // start the drag action
          DragHandler.startDrag(view, groupItem, DragAction.Action.DESKTOP, null)

          collapse()

          // convert group item into app item if there is only one item left
          updateItem(callback, item, itemView)
          true
        })
        val app = Settings.appLoader().findItemApp(groupItem)
        if (app == null) {
          removeItem(context, item, groupItem, itemView as AppItemView)
        } else {
          view!!.setOnClickListener(OnClickListener {
            Animation.createScaleInScaleOutAnim(view!!, Runnable {
              collapse()
              visibility = View.INVISIBLE
              view!!.getContext().startActivity(groupItem.intent)
            })
          })
        }
        _cellContainer!!.addViewToGrid(view!!, x2, y2, 1, 1)
      }
    }

    _dismissListener = PopupWindow.OnDismissListener {
      if ((itemView as AppItemView).icon != null) {
        (itemView.icon as GroupIconDrawable).popBack()
      }
    }

    val popupWidth = contentPadding * 8 + _popupCard!!.contentPaddingLeft + _popupCard!!.contentPaddingRight + iconSize * cellSize[0]
    _popupCard!!.layoutParams.width = popupWidth

    val popupHeight = contentPadding * 2 + _popupCard!!.contentPaddingTop + _popupCard!!.contentPaddingBottom + Tool.dp2px(30f) + (iconSize + textSize) * cellSize[1]
    _popupCard!!.layoutParams.height = popupHeight

    _cx = popupWidth / 2
    _cy = popupHeight / 2 - if (Settings.appSettings().desktopShowLabel) Tool.dp2px(10f) else 0

    val coordinates = IntArray(2)
    itemView.getLocationInWindow(coordinates)

    coordinates[0] += itemView.width / 2
    coordinates[1] += itemView.height / 2

    coordinates[0] -= popupWidth / 2
    coordinates[1] -= popupHeight / 2

    val width = width
    val height = height

    if (coordinates[0] + popupWidth > width) {
      val v = width - (coordinates[0] + popupWidth)
      coordinates[0] += v
      coordinates[0] -= contentPadding
      _cx -= v
      _cx += contentPadding
    }
    if (coordinates[1] + popupHeight > height) {
      coordinates[1] += height - (coordinates[1] + popupHeight)
    }
    if (coordinates[0] < 0) {
      coordinates[0] -= itemView.width / 2
      coordinates[0] += popupWidth / 2
      coordinates[0] += contentPadding
      _cx += itemView.width / 2
      _cx -= popupWidth / 2
      _cx -= contentPadding
    }
    if (coordinates[1] < 0) {
      coordinates[1] -= itemView.height / 2
      coordinates[1] += popupHeight / 2
    }

    if (item._location == Constants.ItemPosition.Dock) {
      coordinates[1] -= iconSize / 2
      _cy += iconSize / 2 + if (Settings.appSettings().dockShowLabel) 0 else Tool.dp2px(10f)
    }

    val x = coordinates[0]
    val y = coordinates[1]

    _popupCard!!.pivotX = 0f
    _popupCard!!.pivotX = 0f
    _popupCard!!.x = x.toFloat()
    _popupCard!!.y = y.toFloat()

    visibility = View.VISIBLE
    _popupCard!!.visibility = View.VISIBLE
    expand()

    return true
  }

  private fun expand() {
    _cellContainer!!.setAlpha(0f)

    val finalRadius = Math.max(_popupCard!!.width, _popupCard!!.height)
    val startRadius = Tool.dp2px((Settings.appSettings().desktopIconSize / 2).toFloat())

    val animDuration = (Settings.appSettings().animationSpeed * 10).toLong()
    _folderAnimator = ViewAnimationUtils.createCircularReveal(_popupCard!!, _cx, _cy, startRadius.toFloat(), finalRadius.toFloat())
    _folderAnimator!!.startDelay = 0
    _folderAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    _folderAnimator!!.duration = animDuration
    _folderAnimator!!.start()
    Animation.fadeIn(animDuration, _cellContainer!!)
  }

  fun collapse() {
    if (!_isShowing) return
    if (_folderAnimator == null || _folderAnimator!!.isRunning)
      return

    val animDuration = (Settings.appSettings().animationSpeed * 10).toLong()
    Animation.fadeOut(animDuration, _cellContainer!!)

    val startRadius = Tool.dp2px((Settings.appSettings().desktopIconSize / 2).toFloat())
    val finalRadius = Math.max(_popupCard!!.width, _popupCard!!.height)
    _folderAnimator = ViewAnimationUtils.createCircularReveal(_popupCard!!, _cx, _cy, finalRadius.toFloat(), startRadius.toFloat())
    _folderAnimator!!.startDelay = 1 + animDuration / 2
    _folderAnimator!!.interpolator = AccelerateDecelerateInterpolator()
    _folderAnimator!!.duration = animDuration
    _folderAnimator!!.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(p1: Animator) {}

      override fun onAnimationEnd(p1: Animator) {
        _popupCard!!.visibility = View.INVISIBLE
        _isShowing = false

        if (_dismissListener != null) {
          _dismissListener!!.onDismiss()
        }

        _cellContainer!!.removeAllViews()
        visibility = View.INVISIBLE
      }

      override fun onAnimationCancel(p1: Animator) {}

      override fun onAnimationRepeat(p1: Animator) {}
    })
    _folderAnimator!!.start()
  }

  private fun removeItem(context: Context, currentItem: Item, dragOutItem: Item?, currentView: AppItemView) {
    currentItem.groupItems.remove(dragOutItem)

    HomeActivity._db.saveItem(dragOutItem!!, Constants.ItemState.Visible)
    HomeActivity._db.saveItem(currentItem)

    currentView.icon = GroupIconDrawable(context, currentItem, Settings.appSettings().desktopIconSize)
  }

  fun updateItem(callback: WorkspaceCallback, currentItem: Item, currentView: View) {
    if (currentItem.groupItems.size == 1) {
      val app = Settings.appLoader().findItemApp(currentItem.groupItems.get(0))
      if (app != null) {
        val item = HomeActivity._db.getItem(currentItem.groupItems.get(0).id!!)
        item!!.x = currentItem.x
        item.y = currentItem.y

        // update db
        HomeActivity._db.saveItem(item)
        HomeActivity._db.saveItem(item, Constants.ItemState.Visible)
        HomeActivity._db.deleteItem(currentItem, true)

        // update launcher
        callback.removeItem(currentView, false)
        callback.addItemToCell(item, item.x, item.y)
      }
    }
  }

  internal object GroupDef {
    var _maxItem = 12

    fun getCellSize(count: Int): IntArray {
      if (count <= 1)
        return intArrayOf(1, 1)
      if (count <= 2)
        return intArrayOf(2, 1)
      if (count <= 4)
        return intArrayOf(2, 2)
      if (count <= 6)
        return intArrayOf(3, 2)
      if (count <= 9)
        return intArrayOf(3, 3)
      return if (count <= 12) intArrayOf(4, 3) else intArrayOf(0, 0)
    }
  }
}

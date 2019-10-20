package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.*
import com.fisma.trinity.viewutil.GroupIconDrawable
import com.fisma.trinity.viewutil.WorkspaceCallback


class AppItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), Drawable.Callback {

  var icon: Drawable? = null
  var label: String? = null
  private val _textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val _textContainer = Rect()
  private val testTextContainer = Rect()
  var iconSize: Float = 0.toFloat()
  var showLabel = true
  private var _vibrateWhenLongPress: Boolean = false
  private val _labelHeight: Float = Tool.dp2px(14f).toFloat()
  private var _targetedWidth: Int = 0
  private var _targetedHeightPadding: Int = 0
  var drawIconTop: Float = 0.toFloat()
    private set

  val drawIconLeft: Float
    get() = (width - iconSize) / 2

  init {

    _textPaint.textSize = Tool.sp2px(12f).toFloat()
    _textPaint.color = Color.WHITE
  }

  fun setTargetedWidth(width: Int) {
    _targetedWidth = width
  }

  fun setTargetedHeightPadding(padding: Int) {
    _targetedHeightPadding = padding
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var mWidth = iconSize
    val mHeight = iconSize + if (showLabel) _labelHeight else 0f
    if (_targetedWidth != 0) {
      mWidth = _targetedWidth.toFloat()
    }
    setMeasuredDimension(Math.ceil(mWidth.toDouble()).toInt(), Math.ceil(mHeight.toInt().toDouble()).toInt() + Tool.dp2px(2f) + _targetedHeightPadding * 2)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    drawIconTop = (height.toFloat() - iconSize - if (showLabel) _labelHeight else 0f) / 2f

    if (label != null && showLabel) {
      _textPaint.getTextBounds(label, 0, label!!.length, _textContainer)
      val maxTextWidth = width - MIN_ICON_TEXT_MARGIN * 2

      // use ellipsis if the label is too long
      if (_textContainer.width() > maxTextWidth) {
        val testLabel = label!! + ELLIPSIS
        _textPaint.getTextBounds(testLabel, 0, testLabel.length, testTextContainer)

        //Premeditate to be faster
        val characterSize = (testTextContainer.width() / testLabel.length).toFloat()
        val charsToTruncate = ((testTextContainer.width() - maxTextWidth) / characterSize).toInt()

        canvas.drawText(label!!.substring(0, label!!.length - charsToTruncate) + ELLIPSIS,
          MIN_ICON_TEXT_MARGIN.toFloat(), height - drawIconTop, _textPaint)
      } else {
        canvas.drawText(label!!, (width - _textContainer.width()) / 2f, height - drawIconTop, _textPaint)
      }
    }

    // center the _icon
    if (icon != null) {
      canvas.save()
      canvas.translate((width - iconSize) / 2, drawIconTop)
      icon!!.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
      icon!!.draw(canvas)
      canvas.restore()
    }
  }

  class Builder {
    // TODO accept any view and just add click and long click listeners
    // this class isn't necessary
    // remove in favor of using ItemViewFactory
    var view: AppItemView
      internal set

    constructor(context: Context) {
      view = AppItemView(context)
    }

    constructor(view: AppItemView) {
      this.view = view
    }

    fun setAppItem(item: Item): Builder {
      view.label = item.label
      view.icon = item.icon
      view.setOnClickListener {
        Animation.createScaleInScaleOutAnim(view, Runnable { Tool.startApp(view.context, AppManager.getInstance(view.context)!!.findApp(item.intent)!!, view) })
      }
      return this
    }

    fun setShortcutItem(item: Item): Builder {
      view.label = item.label
      view.icon = item.icon
      view.setOnClickListener { Animation.createScaleInScaleOutAnim(view, Runnable { view.context.startActivity(item.intent) }) }
      return this
    }

    fun setGroupItem(context: Context, callback: WorkspaceCallback, item: Item): Builder {
      view.label = item.label
      view.icon = GroupIconDrawable(context, item, Settings.appSettings().iconSize)
      view.setOnClickListener { v ->
        if (HomeActivity.launcher != null && HomeActivity.launcher.groupPopup.showPopup(item, v, callback)) {
          ((v as AppItemView).icon as GroupIconDrawable).popUp()
        }
      }
      return this
    }

    fun setActionItem(item: Item): Builder {
      view.label = item.label
      view.icon = ContextCompat.getDrawable(Settings.appContext(), R.drawable.ic_app_menu)
      view.setOnClickListener { view ->
        Animation.createScaleInScaleOutAnim(view, Runnable {
          HomeActivity.launcher.openAppDrawer(view, 0, 0)
        })
      }
      return this
    }

    fun withOnLongClick(item: Item, action: DragAction.Action, desktopCallback: WorkspaceCallback?): Builder {
      view.setOnLongClickListener(DragHandler.getLongClick(item, action, desktopCallback))
      return this
    }

    fun setTextColor(color: Int): Builder {
      view._textPaint.color = color
      return this
    }

    fun setTextSize(size: Float): Builder {
      view._textPaint.textSize = size
      return this
    }


    fun setIconSize(iconSize: Int): Builder {
      view.iconSize = Tool.dp2px(iconSize.toFloat()).toFloat()
      return this
    }

    fun setLabelVisibility(visible: Boolean): Builder {
      view.showLabel = visible
      return this
    }

    fun vibrateWhenLongPress(vibrate: Boolean): Builder {
      view._vibrateWhenLongPress = vibrate
      return this
    }
  }

  companion object {

    private val MIN_ICON_TEXT_MARGIN = 8
    private val ELLIPSIS = '…'
  }
}

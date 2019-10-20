package com.fisma.trinity.viewutil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.util.Tool
import com.mikepenz.fastadapter.items.AbstractItem
import com.fisma.trinity.R
import com.fisma.trinity.util.ImageUtil

class IconLabelItem : AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {
  private var _width = Integer.MAX_VALUE
  private val _height = Integer.MAX_VALUE

  var _icon: Drawable
  private var _iconSize = Integer.MAX_VALUE
  private var _iconGravity: Int = 0
  private var _iconPadding: Int = 0

  var _label: String? = null
  private var _textGravity = Gravity.CENTER_VERTICAL
  private var _textColor = Integer.MAX_VALUE
  private var _textVisibility = true
  private var _isAppLauncher = false
  private var _textSize = 12f

  private var _onClickAnimate = true
  private var _onClickListener: View.OnClickListener? = null
  private var _onLongClickListener: View.OnLongClickListener? = null
  private var mViewHolder: IconLabelItem.ViewHolder? = null

  constructor(context: Context, icon: Int, label: Int) {
    _label = context.getString(label)
    _icon = context.resources.getDrawable(icon)
  }

  constructor(icon: Drawable, label: String) {
    _label = label
    _icon = icon.mutate()
  }

  fun withWidth(width: Int): IconLabelItem {
    _width = width
    return this
  }

  fun withIconSize(context: Context, iconSize: Int): IconLabelItem {
    _iconSize = Tool.dp2px(iconSize.toFloat())
    return this
  }

  fun withIconGravity(iconGravity: Int): IconLabelItem {
    _iconGravity = iconGravity
    return this
  }

  fun withIconPadding(context: Context, iconPadding: Int): IconLabelItem {
    _iconPadding = Tool.dp2px(iconPadding.toFloat())
    return this
  }

  fun withTextSize(textSize: Float): IconLabelItem {
    _textSize = textSize
    return this
  }

  fun withTextGravity(textGravity: Int): IconLabelItem {
    _textGravity = textGravity
    return this
  }

  fun withTextColor(textColor: Int): IconLabelItem {
    _textColor = textColor
    return this
  }

  fun withTextVisibility(visibility: Boolean): IconLabelItem {
    _textVisibility = visibility
    return this
  }

  fun withOnClickAnimate(background: Boolean): IconLabelItem {
    _onClickAnimate = background
    return this
  }

  fun withOnClickListener(listener: View.OnClickListener?): IconLabelItem {
    _onClickListener = listener
    return this
  }

  fun withOnLongClickListener(onLongClickListener: View.OnLongClickListener): IconLabelItem {
    _onLongClickListener = onLongClickListener
    return this
  }

  override fun getViewHolder(v: View): ViewHolder {
    return ViewHolder(v, this)
  }

  override fun getLayoutRes(): Int {
    return R.layout.item_icon_label
  }

  override fun getType(): Int {
    return R.id.id_adapter_icon_label_item
  }

  override fun bindView(holder: IconLabelItem.ViewHolder, payloads: List<*>?) {
    mViewHolder = holder
    if (_width == Integer.MAX_VALUE) {
      holder.itemView.layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
    } else {
      holder.itemView.layoutParams.width = _width
    }

    if (_height == Integer.MAX_VALUE) {
      holder.itemView.layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT
    } else {
      holder.itemView.layoutParams.height = _height
    }

    // only run all this code if a label should be shown
    if (_label != null && _textVisibility) {
      holder.textView.text = _label
      holder.textView.gravity = _textGravity
      holder.textView.maxLines = 1
      holder.textView.ellipsize = TextUtils.TruncateAt.END
      holder.textView.textSize = _textSize
      holder.textView.background = null
      // no default text color since it will be set by the theme
      if (_textColor != Integer.MAX_VALUE)
        holder.textView.setTextColor(_textColor)
    }

    // icon specific padding
    holder.textView.compoundDrawablePadding = _iconPadding
    if (_iconSize != Integer.MAX_VALUE) {
      _icon = BitmapDrawable(Settings.appContext().resources, Bitmap.createScaledBitmap(ImageUtil.drawableToBitmap(_icon), _iconSize, _iconSize, true))
      if (_isAppLauncher) {
        _icon.setBounds(0, 0, _iconSize, _iconSize)
      }
    }

    when (_iconGravity) {
      Gravity.START -> if (_isAppLauncher) {
        holder.textView.setCompoundDrawables(_icon, null, null, null)
      } else {
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(_icon, null, null, null)
      }
      Gravity.END -> if (_isAppLauncher) {
        holder.textView.setCompoundDrawables(null, null, _icon, null)
      } else {
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, _icon, null)
      }
      Gravity.TOP -> if (_isAppLauncher) {
        holder.textView.setCompoundDrawables(null, _icon, null, null)
      } else {
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, _icon, null, null)
      }
      Gravity.BOTTOM -> if (_isAppLauncher) {
        holder.textView.setCompoundDrawables(null, null, null, _icon)
      } else {
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, _icon)
      }
    }

    // most items will not use a long click
    if (!_onClickAnimate)
      holder.itemView.setBackgroundResource(0)
    if (_onClickListener != null)
      holder.itemView.setOnClickListener(_onClickListener)
    if (_onLongClickListener != null)
      holder.itemView.setOnLongClickListener(_onLongClickListener)
    super.bindView(holder, payloads)
  }

  fun withIsAppLauncher(isAppLauncher: Boolean): IconLabelItem {
    _isAppLauncher = isAppLauncher
    return this
  }


  inner class ViewHolder(itemView: View, item: IconLabelItem) : RecyclerView.ViewHolder(itemView) {
    var textView: TextView = itemView as TextView

    init {
      textView.tag = item
    }
  }

  // only used for search bar
  fun setIconGravity(iconGravity: Int) {
    _iconGravity = iconGravity
  }

  fun setTextGravity(textGravity: Int) {
    _textGravity = textGravity
  }
}

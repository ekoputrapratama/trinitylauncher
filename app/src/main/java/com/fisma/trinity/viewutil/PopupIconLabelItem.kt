package com.fisma.trinity.viewutil

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.R
import com.mikepenz.fastadapter.items.AbstractItem


class PopupIconLabelItem(private val _labelRes: Int, private val _iconRes: Int) : AbstractItem<PopupIconLabelItem, PopupIconLabelItem.ViewHolder_PopupIconLabelItem>() {

  class ViewHolder_PopupIconLabelItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val cardView: CardView
    val iconView: ImageView?
    val labelView: TextView?

    init {
      this.cardView = itemView as CardView
      this.labelView = itemView.findViewById(R.id.item_popup_label)
      this.iconView = itemView.findViewById(R.id.item_popup_icon)
      val theme = itemView.context.theme
      val attrs = IntArray(1) { R.attr.popupItemBackground }
      val typedArray = theme.obtainStyledAttributes(attrs)
      val color = typedArray.getColor(0, Color.WHITE)
      cardView.setCardBackgroundColor(color)
    }
  }

  override fun getType(): Int {
    return R.id.id_adapter_popup_icon_label_item
  }

  override fun getLayoutRes(): Int {
    return R.layout.item_popup_icon_label
  }

  override fun bindView(holder: ViewHolder_PopupIconLabelItem?, payloads: List<Any>?) {
    super.bindView(holder!!, payloads)
    if (holder != null) {
      val labelView = holder.labelView
      labelView?.setText(_labelRes)
    }
    if (holder != null) {
      val iconView = holder.iconView
      iconView?.setImageResource(_iconRes)
    }
  }

  override fun unbindView(holder: ViewHolder_PopupIconLabelItem?) {
    super.unbindView(holder)
    if (holder != null) {
      val labelView = holder.labelView
      if (labelView != null) {
        labelView.text = null
      }
    }
    if (holder != null) {
      val iconView = holder.iconView
      iconView?.setImageDrawable(null)
    }
  }

  override fun getViewHolder(v: View): ViewHolder_PopupIconLabelItem {
    return ViewHolder_PopupIconLabelItem(v)
  }
}
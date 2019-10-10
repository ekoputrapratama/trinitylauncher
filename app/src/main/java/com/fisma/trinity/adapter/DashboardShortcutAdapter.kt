package com.fisma.trinity.adapter

import com.fisma.trinity.model.ShortcutItem
import com.woxthebox.draglistview.DragItemAdapter
import android.widget.Toast
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fisma.trinity.R
import android.view.LayoutInflater
import android.widget.ImageView


class DashboardShortcutAdapter(shortcuts: ArrayList<ShortcutItem>, val layoutId: Int, val grabHandleId: Int) :
  DragItemAdapter<ShortcutItem, DashboardShortcutAdapter.ViewHolder>() {

  init {
    itemList = shortcuts
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)
    val text = mItemList[position].label
    val icon = mItemList[position].icon
    holder.mText.text = text
    holder.mImage.setImageBitmap(icon)
    holder.itemView.tag = mItemList[position]
  }

  override fun getUniqueItemId(position: Int): Long {
    return mItemList[position].id.toLong()
  }

  inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, grabHandleId, true) {
    var mText: TextView = itemView.findViewById(R.id.label)
    var mImage: ImageView = itemView.findViewById(R.id.icon)

    override fun onItemClicked(view: View) {
      Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClicked(view: View): Boolean {
      Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show()
      return true
    }
  }
}
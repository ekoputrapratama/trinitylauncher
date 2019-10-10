package com.fisma.trinity.viewutil

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fisma.trinity.R
import com.fisma.trinity.util.LauncherAction


class DashboardAdapter(private val context: Context, private val items: ArrayList<LauncherAction.ActionDisplayItem>) : BaseAdapter() {

  override fun getCount(): Int {
    return items.size
  }

  override fun getItem(arg0: Int): Any? {
    return null
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
    val inflater = LayoutInflater.from(context)
    val view = inflater.inflate(R.layout.item_minibar, parent, false)

    val icon = view.findViewById<ImageView>(R.id.iv)
    val label = view.findViewById<TextView>(R.id.tv)

    icon.setImageDrawable(items[position]._icon)
    label.setText(items[position]._label)
    return view
  }
}
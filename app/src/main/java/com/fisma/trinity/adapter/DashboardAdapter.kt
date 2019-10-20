package com.fisma.trinity.adapter

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.slice.widget.SliceView
import com.fisma.trinity.R
import com.fisma.trinity.model.Plugin
import com.fisma.trinity.util.bind

class DashboardAdapter : ListAdapter<Plugin, DashboardViewHolder>(
  SlicesDiff
) {
  private val TAG = "DashboardAdapter"
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.dashboard_row, parent, false)
    return DashboardViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}


class DashboardViewHolder(
  view: View
) : RecyclerView.ViewHolder(view) {
  private val TAG = "DashboardViewHolder"
  private val context: Context = view.context
  private val sliceView: SliceView = view.findViewById(R.id.slice)

  // Context, LifecycleOwner, onSliceActionListener, OnClickListener, scrollable, OnLongClickListener,
  fun bind(plugin: Plugin) {
    sliceView.bind(
      context = context,
      uri = plugin._uri!!,
      scrollable = false
    )
//    sliceView.isScrollable = true
    sliceView.mode = SliceView.MODE_LARGE
    val theme = context.theme
    val attrs = IntArray(1) { R.attr.sliceItemBackground }
    val typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)
    sliceView.setBackgroundColor(color)
  }
}


object SlicesDiff : DiffUtil.ItemCallback<Plugin>() {
  override fun areItemsTheSame(oldItem: Plugin, newItem: Plugin) = oldItem === newItem

  override fun areContentsTheSame(oldItem: Plugin, newItem: Plugin) = oldItem == newItem
}
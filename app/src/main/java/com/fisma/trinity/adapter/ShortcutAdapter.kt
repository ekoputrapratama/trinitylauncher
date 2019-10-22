package com.fisma.trinity.adapter

import com.fisma.trinity.model.ShortcutItem
import com.woxthebox.draglistview.DragItemAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fisma.trinity.R
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.activity.ShortcutSettings
import com.fisma.trinity.util.Tool


class ShortcutAdapter(shortcuts: ArrayList<ShortcutItem>, val activity: ShortcutSettings, val layoutId: Int, val grabHandleId: Int) :
  DragItemAdapter<ShortcutItem, ShortcutAdapter.ViewHolder>() {
  private val itemClickListeners = ArrayList<(view: View, item: ShortcutItem, position: Int) -> Unit>()

  init {
    itemList = shortcuts
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)
    val item = mItemList[position]
    val text = item.label
    val icon = if (item.iconTheme == null) item.icon else item.iconTheme

    holder.mText.text = text
    holder.mImage.setImageBitmap(icon)
    holder.itemView.tag = item
    holder.bind(itemClickListeners, position)
  }

  override fun getUniqueItemId(position: Int): Long {
    return mItemList[position].id.toLong()
  }

  fun withOnClickListener(listener: (view: View, item: ShortcutItem, position: Int) -> Unit) {
    itemClickListeners.add(listener)
  }

  inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, grabHandleId, true) {
    var mView: View = itemView
    var mText: TextView = itemView.findViewById(R.id.label)
    var mImage: ImageView = itemView.findViewById(R.id.icon)

    fun bind(listeners: ArrayList<(view: View, item: ShortcutItem, position: Int) -> Unit>, position: Int) {
      mView.setOnClickListener { view ->
        listeners.forEach { listener ->
          listener(view, view.tag as ShortcutItem, position)
        }
      }
    }

    override fun onItemClicked(view: View) {

    }

    override fun onItemLongClicked(view: View): Boolean {
      Tool.vibrate(view)
      return true
    }
  }
}

object ShortcutDiff : DiffUtil.ItemCallback<ShortcutItem>() {
  override fun areItemsTheSame(oldItem: ShortcutItem, newItem: ShortcutItem) = oldItem === newItem

  override fun areContentsTheSame(oldItem: ShortcutItem, newItem: ShortcutItem) = oldItem == newItem
}

class AvailableShortcutAdapter : ListAdapter<ShortcutItem, AvailableShortcutAdapter.ViewHolder>(ShortcutDiff) {
  private val itemClickListeners = ArrayList<(view: View, item: ShortcutItem, position: Int) -> Unit>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.shortcut_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = getItem(position)
    val icon = if (item.iconTheme == null) item.icon else item.iconTheme

    holder.mImage.setImageBitmap(icon)
    holder.mText.text = item.label
    holder.mView.tag = item
    holder.bind(itemClickListeners, position)
  }

  fun withOnClickListener(listener: (view: View, item: ShortcutItem, position: Int) -> Unit) {
    itemClickListeners.add(listener)
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mView = itemView
    var mText: TextView = mView.findViewById(R.id.label)
    var mImage: ImageView = mView.findViewById(R.id.icon)

    fun bind(listeners: ArrayList<(view: View, item: ShortcutItem, position: Int) -> Unit>, position: Int) {
      mView.setOnClickListener { view ->
        listeners.forEach { listener ->
          listener(view, view.tag as ShortcutItem, position)
        }
      }
    }
  }
}
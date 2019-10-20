package com.fisma.trinity.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.R
import com.fisma.trinity.adapter.DashboardAdapter
import com.fisma.trinity.manager.PluginManager
import com.fisma.trinity.model.Plugin

class DashboardView : RecyclerView {
  var mAdapter: DashboardAdapter? = null
  var mPluginList: ArrayList<Plugin> = ArrayList()

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    mAdapter = DashboardAdapter()
    adapter = mAdapter

    setPadding(0, 50, 0, 0)
    mAdapter!!.submitList(mPluginList)
    PluginManager.initViews(this)
  }

  fun addPlugin(plugin: Plugin) {
    mPluginList.add(plugin)
    mAdapter!!.submitList(mPluginList)
  }

  fun removePlugin(plugin: Plugin) {
    mPluginList.remove(plugin)
    mAdapter!!.submitList(mPluginList)
  }

  fun removePlugin(label: String) {

    mAdapter!!.submitList(mPluginList)
  }

  fun removePlugin(index: Int) {
    mPluginList.removeAt(index)
    mAdapter!!.submitList(mPluginList)
  }
}
package com.fisma.trinity.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fisma.trinity.R
import com.fisma.trinity.util.AppSettings
import com.fisma.trinity.util.LauncherAction
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import java.util.*


class DashboardEditActivity : ThemeActivity(), ItemTouchCallback {
  internal var _toolbar: Toolbar? = null
  internal var _enableSwitch: SwitchCompat? = null
  internal var _recyclerView: RecyclerView? = null
  private var _adapter: FastItemAdapter<Item>? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_minibar_edit)

    _toolbar = findViewById(R.id.toolbar)
    _enableSwitch = findViewById(R.id.enableSwitch)
    _recyclerView = findViewById(R.id.recyclerView)

    setSupportActionBar(_toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    supportActionBar!!.setDisplayShowHomeEnabled(true)
    setTitle(R.string.minibar)

    _adapter = FastItemAdapter()

    val touchCallback = SimpleDragCallback(this)
    val touchHelper = ItemTouchHelper(touchCallback)
    touchHelper.attachToRecyclerView(_recyclerView)

    _recyclerView!!.layoutManager = LinearLayoutManager(this)
    _recyclerView!!.adapter = _adapter

//        for (item in LauncherAction.actionDisplayItems) {
//            _adapter!!.add(Item(item, minibarArrangement.contains(item)))
//        }

    val minibarEnable = AppSettings.get().dashboardEnable
    _enableSwitch!!.isChecked = minibarEnable
    _enableSwitch!!.setText(if (minibarEnable) R.string.on else R.string.off)
    _enableSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
      buttonView.setText(if (isChecked) R.string.on else R.string.off)
      AppSettings.get().dashboardEnable = isChecked
      if (HomeActivity.launcher != null) {
        HomeActivity.launcher.closeAppDrawer()
//                HomeActivity.launcher.drawerLayout.setDrawerLockMode(if (isChecked) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
      }
    }

    setResult(Activity.RESULT_OK)
  }

  override fun onPause() {
    val minibarArrangement = ArrayList<String>()
    for (item in _adapter!!.adapterItems) {
      if (item.enable) minibarArrangement.add(item.item._action.toString())
    }
//        AppSettings.get().setMinibarArrangement(minibarArrangement)
    super.onPause()
  }

  override fun onStop() {
    if (HomeActivity.launcher != null) {
//            HomeActivity.launcher.initDashboard()
    }
    super.onStop()
  }

  override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
    Collections.swap(_adapter!!.adapterItems, oldPosition, newPosition)
    _adapter!!.notifyAdapterDataSetChanged()
    return false
  }

  override fun itemTouchDropped(i: Int, i1: Int) {}

  class Item(val item: LauncherAction.ActionDisplayItem, var enable: Boolean) : AbstractItem<Item, Item.ViewHolder>() {

    override fun getType(): Int {
      return 0
    }

    override fun getLayoutRes(): Int {
      return R.layout.item_edit_minibar
    }

    override fun getViewHolder(v: View): ViewHolder {
      return ViewHolder(v)
    }

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any?>) {
      holder._label.setText(item._label)
      holder._description.setText(item._description)
      holder._icon.setImageDrawable(item._icon)
      holder._cb.isChecked = enable
      holder._cb.setOnCheckedChangeListener { compoundButton, b -> enable = b }
      super.bindView(holder, payloads)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      internal var _label: TextView
      internal var _description: TextView
      internal var _icon: ImageView
      internal var _cb: CheckBox

      init {
        _label = itemView.findViewById(R.id.tv)
        _description = itemView.findViewById(R.id.tv2)
        _icon = itemView.findViewById(R.id.iv)
        _cb = itemView.findViewById(R.id.cb)
      }
    }
  }
}
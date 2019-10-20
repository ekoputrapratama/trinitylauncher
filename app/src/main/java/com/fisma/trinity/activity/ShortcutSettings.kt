package com.fisma.trinity.activity

import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.TrinityPluginProvider
import com.fisma.trinity.adapter.AvailableShortcutAdapter
import com.fisma.trinity.adapter.ShortcutAdapter
import com.fisma.trinity.model.ShortcutItem
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction
import com.fisma.trinity.util.Tool
import com.woxthebox.draglistview.DragListView

class ShortcutSettings : ThemeActivity() {
  var toolbar: Toolbar? = null
  var mAddedShortcuts: ArrayList<ShortcutItem> = ArrayList()
  var mAvailableShortcuts: ArrayList<ShortcutItem> = ArrayList()

  val mAddedShortcutsGrid: DragListView
    get() = findViewById(R.id.added_shortcuts)
  val mAvailableShortcutsGrid: RecyclerView
    get() = findViewById(R.id.builtin_shortcuts)
  var mAvailableShortcutsAdapter: AvailableShortcutAdapter? = null

  companion object {
    const val TAG = "ShortcutSettings"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_shortcut_settings)

    toolbar = findViewById(R.id.toolbar)
    toolbar!!.title = "Shortcut Settings"
    setSupportActionBar(toolbar)
    mAddedShortcuts = HomeActivity._db.shortcuts

    for (shortcut in mAddedShortcuts) {
      updateIconForTheme(shortcut)
    }

    val adapter = ShortcutAdapter(mAddedShortcuts, this, R.layout.shortcut_item, R.id.item_layout)
    adapter.withOnClickListener { view, item, position ->
      addAvailableShortcut(item)
      removeAddedShortcut(item, position)
    }
    mAddedShortcutsGrid.setAdapter(adapter, false)
    mAddedShortcutsGrid.setDragListListener(object : DragListView.DragListListener {
      override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {

      }

      override fun onItemDragStarted(position: Int) {
        val item = mAddedShortcuts[position]
        Log.d(TAG, "start dragging item ${item.label}")
      }

      override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
        if (fromPosition != toPosition) {
          val item = mAddedShortcuts[toPosition]
          Log.d(TAG, "finish dragging item ${item.label}")
        }
      }
    })

    mAddedShortcutsGrid.setLayoutManager(GridLayoutManager(this, 5))
    mAddedShortcutsGrid.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    mAvailableShortcutsGrid.layoutManager = GridLayoutManager(this, 5)
    mAvailableShortcutsGrid.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    initAvailableShortcuts()
  }

  fun removeAvailableShortcut(position: Int) {
    mAvailableShortcuts.removeAt(position)
  }

  fun addAvailableShortcut(shortcut: ShortcutItem) {
    mAvailableShortcuts.add(shortcut)
    updateAvailableShortcuts()
  }

  fun removeAddedShortcut(shortcut: ShortcutItem, position: Int) {
    mAddedShortcuts.removeAt(position)
    mAddedShortcutsGrid.adapter.notifyDataSetChanged()
    HomeActivity._db.deleteShortcut(shortcut)
  }

  override fun onStop() {
    super.onStop()
    for (i in 0 until mAddedShortcuts.size) {
      val item = mAddedShortcuts[i]
      item.index = i
      HomeActivity._db.saveShortcut(item)
    }
    TrinityPluginProvider.getInstance()?.updateShortcuts()
  }

  fun initAvailableShortcuts() {
    mAvailableShortcuts.clear()
    val shortcuts = HashMap<String, ShortcutItem>()
    for (shortcut in mAddedShortcuts) {
      shortcuts[shortcut.label!!] = shortcut
    }

    var shortcut: ShortcutItem

    val hasBluetoothFeature = BluetoothAdapter.getDefaultAdapter() != null
    if (!shortcuts.containsKey("Bluetooth") && hasBluetoothFeature) {
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.Bluetooth)
        .setLabel("Bluetooth")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    val flashlightAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    if (flashlightAvailable && !shortcuts.containsKey("Flashlight")) {
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.Flashlight)
        .setLabel("Flashlight")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    if (!shortcuts.containsKey("Wallpaper")) {
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.SetWallpaper)
        .setLabel("Wallpaper")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    if (!shortcuts.containsKey("Settings")) {
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.LauncherSettings)
        .setLabel("Settings")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }
    if (!shortcuts.containsKey("Messages")) {
      val info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.MESSAGING)
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.APP)
        .setPackageName(info!!.activityInfo.packageName)
        .setClassName(info.activityInfo.name)
        .setAction(LauncherAction.Action.LauncherSettings)
        .setIcon(info.loadIcon(packageManager))
        .setLabel("Messages")
        .build()
      mAvailableShortcuts.add(shortcut)
    }

    if (!shortcuts.containsKey("Phone")) {
      val info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.PHONE)
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.APP)
        .setPackageName(info!!.activityInfo.packageName)
        .setClassName(info.activityInfo.name)
        .setAction(LauncherAction.Action.LauncherSettings)
        .setIcon(info.loadIcon(packageManager))
        .setLabel("Phone")
        .build()
      mAvailableShortcuts.add(shortcut)
    }

    updateAvailableShortcuts()
  }

  fun updateAvailableShortcuts() {
    if (mAvailableShortcutsAdapter == null) {
      mAvailableShortcutsAdapter = AvailableShortcutAdapter()
      mAvailableShortcutsGrid.adapter = mAvailableShortcutsAdapter
      mAvailableShortcutsAdapter!!.withOnClickListener { v, item, position ->
        if (mAddedShortcuts.size > 5) {
          Toast.makeText(this, "", Toast.LENGTH_LONG)
        }
        val removed = mAvailableShortcuts.removeAt(position)
        mAddedShortcuts.add(removed)
        mAvailableShortcutsAdapter!!.notifyDataSetChanged()
        mAddedShortcutsGrid.adapter.notifyDataSetChanged()
      }
    }

    mAvailableShortcutsAdapter!!.submitList(mAvailableShortcuts)
  }

  fun updateIconForTheme(shortcut: ShortcutItem) {
    val attrs = IntArray(1)
    attrs[0] = R.styleable.DashboardView_iconColor

    val typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)
    when (shortcut.action) {
      LauncherAction.Action.Bluetooth -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.Flashlight -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.LauncherSettings -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_launcher_settings, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.SetWallpaper -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_photo_black_24dp, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
    }
  }

}
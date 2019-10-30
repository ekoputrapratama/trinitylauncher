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
import com.fisma.trinity.model.Shortcut
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction
import com.fisma.trinity.util.Tool
import com.woxthebox.draglistview.DragListView

class ShortcutSettings : ThemeActivity() {
  var toolbar: Toolbar? = null
  var mAddedShortcuts: ArrayList<Shortcut> = ArrayList()
  var mAvailableShortcuts: ArrayList<Shortcut> = ArrayList()

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
    toolbar!!.title = getString(R.string.shortcut_preference)
    setSupportActionBar(toolbar)
    mAddedShortcuts = HomeActivity._db.shortcuts

    for (shortcut in mAddedShortcuts) {
      updateIconForTheme(shortcut)
    }

    val adapter = ShortcutAdapter(mAddedShortcuts, this, R.layout.shortcut_item, R.id.item_layout)
    adapter.withOnClickListener { view, item, position ->
      val removed = mAddedShortcuts.removeAt(position)
      mAvailableShortcuts.add(removed)
      mAvailableShortcutsAdapter!!.notifyDataSetChanged()
      mAddedShortcutsGrid.adapter.notifyDataSetChanged()
      val db = HomeActivity._db
      db.deleteShortcut(removed)
    }
    mAddedShortcutsGrid.setAdapter(adapter, false)
    mAddedShortcutsGrid.setDragListListener(object : DragListView.DragListListener {
      override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {

      }

      override fun onItemDragStarted(position: Int) {
      }

      override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
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

  fun addAvailableShortcut(shortcut: Shortcut) {
    mAvailableShortcuts.add(shortcut)
    updateAvailableShortcuts()
  }

  fun removeAddedShortcut(shortcut: Shortcut, position: Int) {
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

  private fun initAvailableShortcuts() {
    mAvailableShortcuts.clear()
    val shortcuts = HashMap<String, Shortcut>()
    for (shortcut in mAddedShortcuts) {
      shortcuts[shortcut.label!!] = shortcut
    }

    var shortcut: Shortcut

    val hasBluetoothFeature = BluetoothAdapter.getDefaultAdapter() != null
    if (!shortcuts.containsKey("Bluetooth") && hasBluetoothFeature) {
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.ACTION)
        .setAction(LauncherAction.Action.Bluetooth)
        .setLabel("Bluetooth")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    val flashlightAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    if (flashlightAvailable && !shortcuts.containsKey("Flashlight")) {
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.ACTION)
        .setAction(LauncherAction.Action.Flashlight)
        .setLabel("Flashlight")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    if (!shortcuts.containsKey("Wallpaper")) {
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.ACTION)
        .setAction(LauncherAction.Action.SetWallpaper)
        .setLabel("Wallpaper")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }

    if (!shortcuts.containsKey("Settings")) {
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.ACTION)
        .setAction(LauncherAction.Action.LauncherSettings)
        .setLabel("Settings")
        .build()
      updateIconForTheme(shortcut)
      mAvailableShortcuts.add(shortcut)
    }
    if (!shortcuts.containsKey("Messages")) {
      val info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.MESSAGING)
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.APP)
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
      shortcut = Shortcut.Builder()
        .setType(Shortcut.Type.APP)
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
        if (mAddedShortcuts.size == 5) {
          Toast.makeText(this, "cannot add more than 5 shortcuts", Toast.LENGTH_LONG).show()
        } else {
          val removed = mAvailableShortcuts.removeAt(position)
          mAddedShortcuts.add(removed)
          mAvailableShortcutsAdapter!!.notifyDataSetChanged()
          mAddedShortcutsGrid.adapter.notifyDataSetChanged()
          val db = HomeActivity._db
          db.saveShortcut(removed)
        }
      }
    }

    mAvailableShortcutsAdapter!!.submitList(mAvailableShortcuts)
  }

  private fun updateIconForTheme(shortcut: Shortcut) {
    val attrs = IntArray(1)
    attrs[0] = R.styleable.DashboardView_iconColor

    val typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)
    when (shortcut.action) {
      LauncherAction.Action.Bluetooth -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, theme)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable!!.mutate())
        DrawableCompat.setTint(drawable, color)
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.Flashlight -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable.mutate())
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.LauncherSettings -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_launcher_settings, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable.mutate())
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.SetWallpaper -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_photo_black_24dp, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable.mutate())
        shortcut.iconTheme = ImageUtil.drawableToBitmap(drawable)
      }
    }
  }

}
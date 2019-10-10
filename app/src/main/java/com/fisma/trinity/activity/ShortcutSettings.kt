package com.fisma.trinity.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fisma.trinity.R
import com.fisma.trinity.adapter.DashboardShortcutAdapter
import com.fisma.trinity.model.ShortcutItem
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction
import com.woxthebox.draglistview.DragListView

class ShortcutSettings : ThemeActivity() {
  var toolbar: Toolbar? = null
  internal var mAddedShortcuts: ArrayList<ShortcutItem> = ArrayList()

  val mAddedShortcutsGrid: DragListView
    get() = findViewById(R.id.added_shortcuts)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_shortcut_settings)

    toolbar = findViewById(R.id.toolbar)
    toolbar!!.title = "Shortcut Settings"
    setSupportActionBar(toolbar)

//    var item = ShortcutItem()
//    item.type = ShortcutItem.Type.ACTION
//    item.action = LauncherAction.Action.LauncherSettings
//    item.label = "Settings"
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//      item.icon = ImageUtil.drawableToBitmap(resources.getDrawable(R.drawable.ic_settings_launcher_black_24dp, theme))
//    else
//      item.icon = ImageUtil.drawableToBitmap(resources.getDrawable(R.drawable.ic_settings_launcher_black_24dp))
//
//    mAddedShortcuts.add(item)
//
//    item = ShortcutItem()
//    item.action = LauncherAction.Action.SetWallpaper
//    item.type = ShortcutItem.Type.ACTION
//    item.label = "Wallpaper"
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//      item.icon = ImageUtil.drawableToBitmap(resources.getDrawable(R.drawable.ic_photo_black_24dp, theme))
//    else
//      item.icon = ImageUtil.drawableToBitmap(resources.getDrawable(R.drawable.ic_photo_black_24dp))
//
//    mAddedShortcuts.add(item)
//
//    item = ShortcutItem()
//    item.action = LauncherAction.Action.Bluetooth
//    item.type = ShortcutItem.Type.ACTION
//    item.label = "Bluetooth"
//    var drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, theme)
//    DrawableCompat.setTint(drawable!!, Color.parseColor("#FFFFFF"))
//    item.icon = ImageUtil.drawableToBitmap(drawable)
//
//    mAddedShortcuts.add(item)
//
//    item = ShortcutItem()
//    item.action = LauncherAction.Action.Flashlight
//    item.type = ShortcutItem.Type.ACTION
//    item.label = "Flashlight"
//    drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, theme)
//    DrawableCompat.setTint(drawable!!, Color.parseColor("#FFFFFF"))
//    item.icon = ImageUtil.drawableToBitmap(drawable)
//
//    mAddedShortcuts.add(item)

    mAddedShortcuts = HomeActivity._db.shortcuts

    for (shortcut in mAddedShortcuts) {
      updateIconForTheme(shortcut)
    }

    val adapter = DashboardShortcutAdapter(mAddedShortcuts, R.layout.shortcut_item, R.id.item_layout)
    mAddedShortcutsGrid.setAdapter(adapter, false)

    mAddedShortcutsGrid.setDragListListener(object : DragListView.DragListListener {
      override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {

      }

      override fun onItemDragStarted(position: Int) {
        Toast.makeText(this@ShortcutSettings, "Start - position: " + position, Toast.LENGTH_SHORT).show()
      }

      override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
        if (fromPosition != toPosition) {
          Toast.makeText(this@ShortcutSettings, "End - position: " + toPosition, Toast.LENGTH_SHORT).show()
        }
      }
    })

    mAddedShortcutsGrid.setLayoutManager(GridLayoutManager(this, 5))
  }

  fun initAvailableShortcuts() {

  }

  fun updateIconForTheme(shortcut: ShortcutItem) {
    val attrs = IntArray(1)
    attrs[0] = R.attr.icon_color

    val typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)
    when (shortcut.action) {
      LauncherAction.Action.Bluetooth -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.Flashlight -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.LauncherSettings -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_launcher_settings, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.SetWallpaper -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_photo_black_24dp, theme)
        DrawableCompat.setTint(drawable!!, color)
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
    }
  }
}
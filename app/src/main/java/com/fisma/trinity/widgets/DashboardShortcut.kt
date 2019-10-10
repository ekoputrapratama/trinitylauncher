package com.fisma.trinity.widgets

import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.activity.ShortcutSettings
import com.fisma.trinity.compat.TorchCompat

import com.fisma.trinity.model.ShortcutItem
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction
import com.fisma.trinity.util.Tool
import com.fisma.trinity.viewutil.IconLabelItem
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.turingtechnologies.materialscrollbar.INameableAdapter
import java.lang.Exception

class DashboardShortcut : CardView {

  companion object {
    const val TAG = "DashboardShortcut"
  }

  val grid: RecyclerView
    get() = findViewById(R.id.grid)

  val container: LinearLayout
    get() = findViewById(R.id.container)

  val mShortcuts = ArrayList<ShortcutItem>()
  val _shortcuts = ArrayList<IconLabelItem>()
  var mShortcutAdapter: ShortcutAdapter? = null

  constructor(context: Context) : this(context, null) {

  }

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
    init()
  }

  fun init() {
    if (com.fisma.trinity.manager.Settings.appSettings().appFirstLaunch) {
      initShortcuts()
    }
    updateItems()
  }

  fun updateItems() {
    mShortcutAdapter = ShortcutAdapter()

    val db = HomeActivity._db

    val shortcuts = db.shortcuts
    mShortcutAdapter!!.withOnClickListener { view: View?, iAdapter: IAdapter<IconLabelItem>, iconLabelItem: IconLabelItem, i: Int ->
      val shortcut = shortcuts[i]
      if (shortcut.type == ShortcutItem.Type.ACTION) {
        LauncherAction.RunAction(shortcut.action!!, context, mShortcutAdapter!!.getAdapterItem(i))
        mShortcutAdapter!!.notifyAdapterItemChanged(i)
        true
      }
      false
    }
    val items = ArrayList<IconLabelItem>()
    for (shortcut in shortcuts) {
      checkToggleActionIcon(shortcut)
      var item = IconLabelItem(BitmapDrawable(resources, shortcut.icon), shortcut.label!!)
        .withIconSize(context, 32)
        .withIconPadding(context, 8)
        .withTextGravity(Gravity.CENTER)
        .withTextSize(10f)
        .withIconGravity(Gravity.TOP)
        .withIsAppLauncher(false)
      items.add(item)
    }

    mShortcutAdapter!!.set(items)
  }

  private fun checkToggleActionIcon(shortcut: ShortcutItem) {
    when (shortcut.action) {
      LauncherAction.Action.Bluetooth -> {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
          var drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, context.theme)
          if (adapter.isEnabled) {
            DrawableCompat.setTint(drawable!!, Color.parseColor("#5D00FF"))
          } else {
            DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
          }
          shortcut.icon = ImageUtil.drawableToBitmap(drawable)
        }
      }
      LauncherAction.Action.Flashlight -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, context.theme)
        val isEnabled = TorchCompat.getInstance(context).isEnabled()

        if (isEnabled) {
          DrawableCompat.setTint(drawable!!, Color.parseColor("#5D00FF"))
        } else {
          DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
        }
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.LauncherSettings -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_launcher_settings, context.theme)
        DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
      LauncherAction.Action.SetWallpaper -> {
        val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_photo_black_24dp, context.theme)
        DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
        shortcut.icon = ImageUtil.drawableToBitmap(drawable)
      }
    }
  }


  private fun initShortcuts() {
    val googleAssistantAvailable = Tool.isPackageInstalled("com.google.android.googlequicksearchbox", context.packageManager)

    if (googleAssistantAvailable) {
      try {
        val icon = context.packageManager.getApplicationIcon("com.google.android.googlequicksearchbox")
        HomeActivity._db.saveShortcut(ShortcutItem.Builder()
          .setType(ShortcutItem.Type.ACTION)
          .setIndex(0)
          .setAction(LauncherAction.Action.Assist)
          .setIcon(icon)
          .setLabel("Google")
          .build()
        )
      } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
      }
    }

    var drawable: VectorDrawableCompat?
    var shortcut: ShortcutItem?
    val flashlightAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    if (flashlightAvailable) {
      drawable = VectorDrawableCompat.create(resources, R.drawable.ic_flashlight, context.theme)
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.Flashlight)
        .setLabel("Flashlight")
        .setIcon(ImageUtil.drawableToBitmap(drawable!!.mutate())!!)
        .setIndex(1)
        .build()
      HomeActivity._db.saveShortcut(shortcut)
    }


    drawable = VectorDrawableCompat.create(resources, R.drawable.ic_bluetooth, context.theme)
    HomeActivity._db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.Bluetooth)
      .setIcon(ImageUtil.drawableToBitmap(drawable!!.mutate())!!)
      .setLabel("Bluetooth")
      .setIndex(2)
      .build()
    )

    drawable = VectorDrawableCompat.create(resources, R.drawable.ic_photo_black_24dp, context.theme)
    HomeActivity._db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.SetWallpaper)
      .setIcon(ImageUtil.drawableToBitmap(drawable)!!)
      .setLabel("Wallpaper")
      .setIndex(3)
      .build()
    )

    drawable = VectorDrawableCompat.create(resources, R.drawable.ic_settings_launcher_black_24dp, context.theme)
    HomeActivity._db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.LauncherSettings)
      .setIcon(ImageUtil.drawableToBitmap(drawable)!!)
      .setLabel("Settings")
      .setIndex(4)
      .build()
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    grid.layoutManager = GridLayoutManager(context, 5)
    grid.adapter = mShortcutAdapter


    val settingsBtn = findViewById<ImageButton>(R.id.settingsBtn)
    settingsBtn.setOnClickListener { v ->
      val intent = Intent(context, ShortcutSettings::class.java)
      context.startActivity(intent)
    }
  }

  inner class ShortcutAdapter : FastItemAdapter<IconLabelItem>(), INameableAdapter {

    override fun getCharacterForElement(element: Int): Char? {
      return if (mShortcuts != null && element < mShortcuts.size && mShortcuts!![element] != null && mShortcuts[element].label!!.length > 0)
        mShortcuts[element].label!![0]
      else
        '#'
    }
  }
}
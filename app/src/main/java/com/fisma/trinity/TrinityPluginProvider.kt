package com.fisma.trinity

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.*
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.interfaces.SliceBuilder
import com.fisma.trinity.model.ShortcutItem
import com.fisma.trinity.slicebuilders.ShortcutsSliceBuilder
import com.fisma.trinity.util.*


/**
 * Examples of using slice template builders.
 */
class TrinityPluginProvider : SliceProvider() {

  private lateinit var hostNameUrl: String

  private lateinit var shortcutsPath: String
  private lateinit var shortcutsData: ArrayList<ShortcutItem>
  private lateinit var db: DatabaseHelper

  init {
    mInstance = this
  }

  override fun onCreateSliceProvider(): Boolean {
    Log.d(TAG, "onCreateSliceProvider()")

    val contextNonNull = context ?: return false

    db = DatabaseHelper.getInstance(contextNonNull)
    shortcutsData = db.shortcuts
    // Initialize Slice URL and all possible slice paths.
    hostNameUrl = contextNonNull.resources.getString(R.string.host_slice_url)

    shortcutsPath = contextNonNull.resources.getString(R.string.shortcuts_slice_path)

    if (shortcutsData.size == 0) {
      initShortcuts()
      shortcutsData = DatabaseHelper.getInstance(context).shortcuts
    }
    return true
  }

  /*
   * Takes an Intent (as specified by the intent-filter in the manifest) with data
   * ("https://interactivesliceprovider.android.example.com/<your_path>") and returns a content
   * URI ("content://com.example.android.interactivesliceprovider/<your_path>").
   */
  override fun onMapIntentToUri(intent: Intent): Uri {

    val path = intent.data?.path ?: ""
    Log.d(TAG, "onMapIntentToUri: $path")
    return Uri.Builder()
      .scheme(ContentResolver.SCHEME_CONTENT)
      .authority(context?.packageName)
      .path(path)
      .build()
  }

  override fun onBindSlice(sliceUri: Uri?): Slice? {
    Log.d(TAG, "onBindSlice(): $sliceUri")
    if (sliceUri == null || sliceUri.path == null) {
      return null
    }

    return getSliceBuilder(sliceUri)?.buildSlice()
  }

  private fun getSliceBuilder(sliceUri: Uri) = when (sliceUri.path) {
    shortcutsPath -> ShortcutsSliceBuilder(
      context = context!!,
      sliceUri = sliceUri,
      shortcuts = shortcutsData
    )
    else -> {
      Log.e(TAG, "Unknown URI: $sliceUri")
      null
    }
  }

  fun updateShortcuts() {
    shortcutsData = DatabaseHelper.getInstance(context).shortcuts
    val uri = Uri.parse("content://com.fisma.trinity/shortcuts")
    context.contentResolver.notifyChange(uri, null)
  }

  private fun initShortcuts() {
    val googleAssistantAvailable = Tool.isPackageInstalled("com.google.android.googlequicksearchbox", context.packageManager)

    if (googleAssistantAvailable) {
      try {
        val icon = context.packageManager.getApplicationIcon("com.google.android.googlequicksearchbox")
        db.saveShortcut(ShortcutItem.Builder()
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
      drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_flashlight, context.theme)
      shortcut = ShortcutItem.Builder()
        .setType(ShortcutItem.Type.ACTION)
        .setAction(LauncherAction.Action.Flashlight)
        .setLabel("Flashlight")
        .setIcon(ImageUtil.drawableToBitmap(drawable!!.mutate())!!)
        .setIndex(1)
        .build()
      db.saveShortcut(shortcut)
    }


    drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_bluetooth, context.theme)
    db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.Bluetooth)
      .setIcon(ImageUtil.drawableToBitmap(drawable!!.mutate())!!)
      .setLabel("Bluetooth")
      .setIndex(2)
      .build()
    )

    drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_photo_black_24dp, context.theme)
    db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.SetWallpaper)
      .setIcon(ImageUtil.drawableToBitmap(drawable)!!)
      .setLabel("Wallpaper")
      .setIndex(3)
      .build()
    )

    drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_settings_launcher_black_24dp, context.theme)
    db.saveShortcut(ShortcutItem.Builder()
      .setType(ShortcutItem.Type.ACTION)
      .setAction(LauncherAction.Action.LauncherSettings)
      .setIcon(ImageUtil.drawableToBitmap(drawable)!!)
      .setLabel("Settings")
      .setIndex(4)
      .build()
    )
  }

  companion object {
    const val TAG = "SliceProvider"
    var mInstance: TrinityPluginProvider? = null

    const val ACTION_WIFI_CHANGED = "com.example.androidx.slice.action.WIFI_CHANGED"
    const val ACTION_TOAST = "com.example.androidx.slice.action.TOAST"
    const val EXTRA_TOAST_MESSAGE = "com.example.androidx.extra.TOAST_MESSAGE"
    const val ACTION_TOAST_RANGE_VALUE = "com.example.androidx.slice.action.TOAST_RANGE_VALUE"
    const val ACTION_SHORTCUTS_SETTINGS = "com.fisma.trinity.action.SHORTCUT_SETTINGS"
    const val ACTION_ASSIST = "com.fisma.trinity.action.ASSIST"
    const val ACTION_FLASHLIGHT = "com.fisma.trinity.action.FLASHLIGHT"
    const val ACTION_BLUETOOTH = "com.fisma.trinity.action.BLUETOOTH"
    const val ACTION_WALLPAPER = "com.fisma.trinity.action.WALLPAPER"
    const val ACTION_LAUCHER_SETTINGS = "com.fisma.trinity.action.LAUNCHER_SETTINGS"
    const val ACTION_LAUNCH_APP = "com.fisma.trinity.action.LAUNCH_APP"

    fun getPendingIntent(context: Context, action: String): PendingIntent {
      val intent = Intent(action)
      return PendingIntent.getActivity(context, 0, intent, 0)
    }

    fun getInstance(): TrinityPluginProvider? {
      return mInstance
    }
  }
}

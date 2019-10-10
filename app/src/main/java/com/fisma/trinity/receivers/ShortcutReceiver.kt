package com.fisma.trinity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.Log
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.Tool


class ShortcutReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.extras == null) return

    // this will only work before Android Oreo
    // was deprecated in favor of ShortcutManager.pinRequestShortcut()
    val shortcutLabel = intent.extras!!.getString(Intent.EXTRA_SHORTCUT_NAME)
    val shortcutIntent = intent.extras!!.get(Intent.EXTRA_SHORTCUT_INTENT) as Intent
    var shortcutIcon: Drawable? = null

    try {
      val parcelable = intent.extras!!.getParcelable<Parcelable>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
      if (parcelable is Intent.ShortcutIconResource) {
        val resources = context.packageManager.getResourcesForApplication(parcelable.packageName)
        if (resources != null) {
          val id = resources.getIdentifier(parcelable.resourceName, null, null)
          shortcutIcon = resources.getDrawable(id)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      if (shortcutIcon == null)
        shortcutIcon = BitmapDrawable(context.resources, intent.extras!!.getParcelable<Parcelable>(Intent.EXTRA_SHORTCUT_ICON) as Bitmap)
    }

    val app = Settings.appLoader().createApp(shortcutIntent)
    val item: Item
    if (app != null) {
      item = Item.newAppItem(app)
    } else {
      item = Item.newShortcutItem(shortcutIntent, shortcutIcon, shortcutLabel)
    }
    val preferredPos = HomeActivity.launcher.workspace.pages.get(HomeActivity.launcher.workspace.getCurrentItem()).findFreeSpace()
    if (preferredPos == null) {
      Tool.toast(HomeActivity.launcher, R.string.toast_not_enough_space)
    } else {
      item.x = preferredPos.x
      item.y = preferredPos.y
      HomeActivity._db.saveItem(item, HomeActivity.launcher.workspace.getCurrentItem(), Constants.ItemPosition.Desktop)
      HomeActivity.launcher.workspace.addItemToPage(item, HomeActivity.launcher.workspace.getCurrentItem())
      Log.d(this.javaClass.toString(), "shortcut installed")
    }
  }
}

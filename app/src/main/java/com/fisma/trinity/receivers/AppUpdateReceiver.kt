package com.fisma.trinity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fisma.trinity.manager.PluginManager
import com.fisma.trinity.manager.Settings


class AppUpdateReceiver : BroadcastReceiver() {
  companion object {
    const val TAG = "AppUpdateReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    Settings.appLoader().onAppUpdated(context, intent)
    val pm = PluginManager.getInstance()
    when (intent.action) {
      Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_INSTALL, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_CHANGED -> {
        pm.reloadPlugins()
      }
    }
  }
}

package com.fisma.trinity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fisma.trinity.manager.Settings


class AppUpdateReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Settings.appLoader().onAppUpdated(context, intent)
  }
}

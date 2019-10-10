package com.fisma.trinity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DeviceAdminReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Log.d("DeviceAdminReceiver", "DeviceAdmin received")
  }
}

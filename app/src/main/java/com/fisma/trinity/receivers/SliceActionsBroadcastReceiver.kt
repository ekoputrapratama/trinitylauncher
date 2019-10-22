package com.fisma.trinity.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.fisma.trinity.R
import com.fisma.trinity.TrinityPluginProvider
import com.fisma.trinity.activity.ShortcutSettings
import com.fisma.trinity.activity.WeatherSettings
import com.fisma.trinity.util.LauncherAction

class SliceActionsBroadcastReceiver : BroadcastReceiver() {

  @SuppressLint("LongLogTag")
  override fun onReceive(context: Context?, intent: Intent?) {
    when (intent?.action) {
      TrinityPluginProvider.ACTION_TOAST -> {
        val message = intent.extras!!.getString(
          TrinityPluginProvider.EXTRA_TOAST_MESSAGE,
          "no message"
        )
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      }
      TrinityPluginProvider.ACTION_WEATHER_SETTINGS -> {
        val intent = Intent(context, WeatherSettings::class.java)
        context?.startActivity(intent)
      }
      TrinityPluginProvider.ACTION_SHORTCUTS_SETTINGS -> {
        val intent = Intent(context, ShortcutSettings::class.java)
        context?.startActivity(intent)
      }
      TrinityPluginProvider.ACTION_ASSIST -> {
        LauncherAction.RunAction(LauncherAction.Action.Assist, context!!)
      }
      TrinityPluginProvider.ACTION_BLUETOOTH -> {
        LauncherAction.RunAction(LauncherAction.Action.Bluetooth, context!!)
        if (context != null) {
          Log.d(TAG, "notifyChange")
          val h = Handler()
          h.postDelayed({
            val uri = Uri.parse("content://com.fisma.trinity/shortcuts")
            context.contentResolver.notifyChange(uri, null)
          }, 1000)
        }
      }
      TrinityPluginProvider.ACTION_FLASHLIGHT -> {
        LauncherAction.RunAction(LauncherAction.Action.Flashlight, context!!)
        if (context != null) {
          val uri = Uri.parse(context.resources.getString(R.string.shortcuts_slice_uri))
          context.contentResolver.notifyChange(uri, null)
        }
      }
      TrinityPluginProvider.ACTION_LAUCHER_SETTINGS -> {
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context!!)
      }
      TrinityPluginProvider.ACTION_WALLPAPER -> {
        LauncherAction.RunAction(LauncherAction.Action.SetWallpaper, context!!)
      }
      TrinityPluginProvider.ACTION_LAUNCH_APP -> {

      }
    }
  }

  companion object {
    const val TAG = "SliceActionsBroadcastReceiver"
    fun getIntent(context: Context?, action: String, message: String): PendingIntent {
      val intent = Intent(action)
      intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
      // Ensure a new PendingIntent is created for each message.
      var requestCode = 0
      if (message != null) {
        intent.putExtra(TrinityPluginProvider.EXTRA_TOAST_MESSAGE, message)
        requestCode = message.hashCode()
      }
      return PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT
      )
    }
  }
}
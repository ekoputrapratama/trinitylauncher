package com.fisma.trinity.slicebuilders

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.builders.*
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fisma.trinity.R
import com.fisma.trinity.TrinityPluginProvider
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.compat.TorchCompat
import com.fisma.trinity.interfaces.SliceBuilder
import com.fisma.trinity.model.ShortcutItem
import com.fisma.trinity.receivers.SliceActionsBroadcastReceiver
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.LauncherAction

class ShortcutsSliceBuilder(
  val context: Context,
  sliceUri: Uri,
  val shortcuts: ArrayList<ShortcutItem>
) : SliceBuilder(sliceUri) {
  companion object {
    const val TAG = "ShortcutsSliceBuilder"
  }

  override fun buildSlice(): Slice {

    return list(context, sliceUri, ListBuilder.INFINITY) {
      val rowCount = if (shortcuts.size % 5 > 0) {
        (shortcuts.size) / 5 + 1
      } else {
        shortcuts.size / 5
      }
      val theme = context.theme
      val attrs = IntArray(1) {
        R.attr.iconColor
      }

      var typedArray = theme.obtainStyledAttributes(attrs)
      val color = typedArray.getColor(0, Color.WHITE)
      header {
        // Second argument for title/subtitle informs system we are waiting for data to load.
        title = "Shortcuts"
        primaryAction = SliceAction.create(
          SliceActionsBroadcastReceiver.getIntent(
            context,
            TrinityPluginProvider.ACTION_TOAST,
            "Primary Action for Grid Slice"
          ),
          IconCompat.createWithResource(context, R.drawable.ic_settings_black_24dp).setTint(color),
          ListBuilder.ICON_IMAGE,
          "Primary"
        )
      }

      addAction(SliceAction.create(
        SliceActionsBroadcastReceiver.getIntent(context, TrinityPluginProvider.ACTION_SHORTCUTS_SETTINGS, ""),
        IconCompat.createWithResource(context, R.drawable.ic_settings_black_24dp).setTint(color),
        ListBuilder.ICON_IMAGE, ""
      ))
      Log.d(TAG, "row count $rowCount")
      Log.d(TAG, "shortcuts size ${shortcuts.size}")
      for (i in 0 until rowCount) {
        val startIndex = i * 5
        val endIndex = startIndex + 5
        Log.d(TAG, "adding row $i")
        gridRow {

          Log.d(TAG, "startIndex=$startIndex endIndex=$endIndex lastIndex=${shortcuts.lastIndex}")
          for (j in startIndex until endIndex) {
            if (j > shortcuts.lastIndex) break
            val shortcut: ShortcutItem = shortcuts[j]
            Log.d(TAG, "adding cell $j")
            cell {
              addImage(
                createShortcutIcon(shortcut),
                ListBuilder.SMALL_IMAGE
              )
              addTitleText(shortcut.label!!)
              contentIntent = createShortcutTapAction(shortcut)
            }
          }
        }
      }
    }
  }

  private fun createShortcutTapAction(shortcut: ShortcutItem): PendingIntent {
    // Ensure a new PendingIntent is created for each message.
    var requestCode = 0
    when (shortcut.action) {
      LauncherAction.Action.Assist -> {
        val intent = Intent(TrinityPluginProvider.ACTION_ASSIST)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
      LauncherAction.Action.Flashlight -> {
        val intent = Intent(TrinityPluginProvider.ACTION_FLASHLIGHT)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
      LauncherAction.Action.Bluetooth -> {
        val intent = Intent(TrinityPluginProvider.ACTION_BLUETOOTH)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
      LauncherAction.Action.SetWallpaper -> {
        val intent = Intent(TrinityPluginProvider.ACTION_WALLPAPER)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
      LauncherAction.Action.LauncherSettings -> {
        val intent = Intent(TrinityPluginProvider.ACTION_LAUCHER_SETTINGS)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
      else -> {
        val intent = Intent(TrinityPluginProvider.ACTION_LAUNCH_APP)
        intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
        intent.putExtra("packageName", shortcut.packageName)
        requestCode = shortcut.packageName.hashCode()
        return PendingIntent.getBroadcast(
          context, requestCode, intent,
          PendingIntent.FLAG_UPDATE_CURRENT
        )
      }
    }
  }

  private fun createShortcutIcon(shortcut: ShortcutItem): IconCompat {
    var icon: IconCompat = IconCompat.createWithBitmap(shortcut.icon)
    val theme = context.theme
    val attrs = IntArray(1) {
      R.attr.iconColor
    }

    var typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)

    attrs[0] = R.attr.activeIconColor
    typedArray = theme.obtainStyledAttributes(attrs)
    val activeColor = typedArray.getColor(0, Color.parseColor("#5D00FF"))

    when (shortcut.action) {
      LauncherAction.Action.Bluetooth -> {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
          if (adapter.isEnabled) {
            icon.setTint(activeColor)
          } else {
            icon.setTint(color)
          }
        }
      }
      LauncherAction.Action.Flashlight -> {
        val isEnabled = TorchCompat.getInstance(context).isEnabled()

        if (isEnabled) {
          icon.setTint(activeColor)
        } else {
          icon.setTint(color)
        }
      }
      LauncherAction.Action.LauncherSettings, LauncherAction.Action.SetWallpaper -> {
        icon.setTint(color)
      }
    }

    return icon
  }
}
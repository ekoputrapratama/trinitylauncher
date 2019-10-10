package com.fisma.trinity.util


import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

import com.fisma.trinity.R
import com.fisma.trinity.TrinityApplication
import com.fisma.trinity.activity.DashboardEditActivity
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.activity.SettingsActivity
import com.fisma.trinity.compat.TorchCompat
import com.fisma.trinity.viewutil.DialogHelper
import com.fisma.trinity.viewutil.IconLabelItem
import java.util.Arrays

object LauncherAction {

  private var actionDisplayItems: ArrayList<ActionDisplayItem>

  var defaultArrangement = Arrays.asList(
    Action.EditMinibar, Action.SetWallpaper,
    Action.LockScreen, Action.LauncherSettings,
    Action.VolumeDialog, Action.DeviceSettings
  )

  val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

  enum class Action {
    EditMinibar, SetWallpaper, LockScreen, LauncherSettings,
    VolumeDialog, DeviceSettings, AppDrawer, SearchBar,
    MobileNetworkSettings, ShowNotifications, TurnOffScreen,
    Bluetooth, Flashlight, Assist, Phone, Email, Message, Contacts
  }

  init {
    val context = TrinityApplication.get()!!
    actionDisplayItems = arrayListOf(
      ActionDisplayItem(Action.EditMinibar, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__edit_minibar),
        HomeActivity.launcher.resources.getString(R.string.minibar_summary__edit_minibar),
        context.resources.getDrawable(R.drawable.ic_mode_edit_black_24dp), 98),
      ActionDisplayItem(Action.SetWallpaper, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__set_wallpaper),
        HomeActivity.launcher.resources.getString(R.string.minibar_summary__set_wallpaper),
        context.resources.getDrawable(R.drawable.ic_photo_black_24dp), 36),
      ActionDisplayItem(Action.LockScreen, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__lock_screen),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__lock_screen),
        context.resources.getDrawable(R.drawable.ic_lock_black_24dp), 24),
      ActionDisplayItem(Action.LauncherSettings, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__launcher_settings),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__launcher_settings),
        context.resources.getDrawable(R.drawable.ic_settings_launcher_black_24dp), 50),
      ActionDisplayItem(Action.VolumeDialog, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__volume_dialog),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__volume_dialog),
        context.resources.getDrawable(R.drawable.ic_volume_up_black_24dp), 71),
      ActionDisplayItem(Action.DeviceSettings, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__device_settings),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__device_settings),
        context.resources.getDrawable(R.drawable.ic_android_minimal), 25),
      ActionDisplayItem(Action.AppDrawer, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__app_drawer),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__app_drawer),
        context.resources.getDrawable(R.drawable.ic_apps_dark_24dp), 73),
      ActionDisplayItem(Action.SearchBar, HomeActivity._launcher!!.resources.getString(R.string.minibar_title__search_bar),
        HomeActivity._launcher!!.resources.getString(R.string.minibar_summary__search_bar),
        context.resources.getDrawable(R.drawable.ic_search_light_24dp), 89),
      ActionDisplayItem(Action.MobileNetworkSettings, HomeActivity.launcher.resources.getString(R.string.minibar_title__mobile_network),
        HomeActivity.launcher.resources.getString(R.string.minibar_summary__mobile_network),
        context.resources.getDrawable(R.drawable.ic_network_24dp), 46),
      ActionDisplayItem(Action.ShowNotifications, HomeActivity.launcher.resources.getString(R.string.minibar_title__show_notifications),
        HomeActivity.launcher.resources.getString(R.string.minibar_summary__show_notifications),
        context.resources.getDrawable(R.drawable.ic_notifications), 46)
    )

    var drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_flashlight, context.theme)
    actionDisplayItems.add(ActionDisplayItem(Action.Flashlight, "Flashlight", "Toggle Flashlight", drawable!!.mutate(), 67))
    actionDisplayItems.add(ActionDisplayItem(Action.Assist, "Google", "", null, 28))
    if (mBluetoothAdapter != null) {
      drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_bluetooth, context!!.theme)
      if (mBluetoothAdapter.isEnabled) {
        DrawableCompat.setTint(drawable!!, Color.parseColor("#5D00FF"))
      } else {
        DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
      }
      actionDisplayItems.add(ActionDisplayItem(Action.Bluetooth, "Bluetooth", "Toggle Bluetooth", drawable, 76))
    }
  }

  fun RunAction(action: Action, context: Context, item: IconLabelItem? = null) {
    RunAction(getActionItem(action)!!, context, item)
  }

  @SuppressLint("WrongConstant", "PrivateApi")
  fun RunAction(action: ActionDisplayItem, context: Context, item: IconLabelItem? = null) {
    when (action._action) {
      Action.EditMinibar -> context.startActivity(Intent(context, DashboardEditActivity::class.java))
      Action.SetWallpaper -> context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.select_wallpaper)))
      Action.LockScreen -> try {
        (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
      } catch (e: Exception) {
        DialogHelper.alertDialog(context,
          context.getString(R.string.device_admin_title),
          context.getString(R.string.device_admin_summary),
          context.getString(R.string.enable)
        ) {
          Tool.toast(context, context.getString(R.string.toast_device_admin_required))
          val intent = Intent()
          intent.component = ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")
          context.startActivity(intent)
        }
      }

      Action.DeviceSettings -> context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
      Action.LauncherSettings -> context.startActivity(Intent(context, SettingsActivity::class.java))
      Action.VolumeDialog -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        try {
          val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
          audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
          val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
          if (!mNotificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
          }
        }

      } else {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI)
      }
      Action.AppDrawer -> HomeActivity._launcher!!.openAppDrawer()
      Action.SearchBar -> {
      }
      Action.MobileNetworkSettings -> context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
      Action.ShowNotifications -> try {
        val statusBarService = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val statusBarExpand = statusBarManager.getMethod("expandNotificationsPanel")
        statusBarExpand.invoke(statusBarService)
      } catch (e: Exception) {
        e.printStackTrace()
      }
      Action.Flashlight -> {
        val drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_flashlight, context.theme)
        val torch = TorchCompat.getInstance(context)
        val isEnabled = torch.isEnabled()

        if (isEnabled) {
          torch.tornOffFlashlight()
          DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
        } else {
          torch.turnOnFlashLight()
          DrawableCompat.setTint(drawable!!, Color.parseColor("#5D00FF"))
        }

        if (item != null) {
          item._icon = drawable
        }
      }
      Action.Bluetooth -> {
        if (mBluetoothAdapter != null) {
          val drawable = VectorDrawableCompat.create(context.resources, R.drawable.ic_bluetooth, context.theme)
          if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
            DrawableCompat.setTint(drawable!!, Color.parseColor("#616161"))
          } else {
            mBluetoothAdapter.enable()
            DrawableCompat.setTint(drawable!!, Color.parseColor("#5D00FF"))
          }
          if (item != null) {
            item._icon = drawable
          }
        }
      }
      Action.Email -> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_EMAIL)

        context.startActivity(intent)
      }
      Action.Message -> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.type = "vnd.android-dir/mms-sms"

        context.startActivity(intent)
      }
      Action.Phone -> {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        context.startActivity(intent)
      }
      Action.Contacts -> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_CONTACTS)

        context.startActivity(intent)
      }
      Action.Assist -> {
        val intent = Intent("android.intent.action.ASSIST")
        intent.`package` = "com.google.android.googlequicksearchbox"
        context.startActivity(intent)
      }
      Action.TurnOffScreen -> try {
        // still needs to reset screen timeout back to default on activity destroy
        val defaultTurnOffTime = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 60000)
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 1000)
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, defaultTurnOffTime)
      } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + context.packageName)
        context.startActivity(intent)
      }

    }//                HomeActivity._launcher.getSearchBar().getSearchButton().performClick();
  }

  fun getActionItem(position: Int): ActionDisplayItem? {
    // used for pick action dialog
    return getActionItem(Action.values()[position])
  }

  fun getActionItem(action: Action): ActionDisplayItem? {
    return getActionItem(action.toString())
  }

  fun getActionItem(action: String): ActionDisplayItem? {
    for (item in actionDisplayItems) {
      if (item._action.toString() == action) {
        return item
      }
    }
    return null
  }

  class ActionDisplayItem(var _action: Action, var _label: String, var _description: String, var _icon: Drawable?, var _id: Int) {
    fun setIcon(icon: Int) {
      val context = TrinityApplication.get()!!
      this._icon = context.resources.getDrawable(icon)
    }
  }
}

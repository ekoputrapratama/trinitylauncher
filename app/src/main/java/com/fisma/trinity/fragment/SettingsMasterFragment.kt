package com.fisma.trinity.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.activity.HideAppsActivity
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.activity.MoreInfoActivity
import com.fisma.trinity.activity.SettingsActivity
import com.fisma.trinity.model.App
import com.fisma.trinity.util.AppManager
import com.fisma.trinity.util.AppSettings
import com.fisma.trinity.util.IntentUtil
import com.fisma.trinity.util.LauncherAction
import com.fisma.trinity.viewutil.DialogHelper
import com.fisma.trinity.widgets.AppDrawerController
import com.nononsenseapps.filepicker.FilePickerActivity
import net.gsantner.opoc.preference.GsPreferenceFragmentCompat
import net.gsantner.opoc.util.PermissionChecker
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class SettingsMasterFragment : GsPreferenceFragmentCompat<AppSettings>() {
  protected var _as: AppSettings? = null
  private var activityRetVal: Int = 0
  private val iconColor: Int? = null

  override fun getPreferenceResourceForInflation(): Int {
    return R.xml.preferences_master
  }

  override fun getFragmentTag(): String {
    return TAG
  }

  override fun getAppSettings(context: Context): AppSettings {
    if (_as == null) {
      _as = AppSettings(context)
    }
    return _as!!
  }

  override fun onPreferenceScreenChanged(preferenceFragmentCompat: PreferenceFragmentCompat, preferenceScreen: PreferenceScreen) {
    super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen)
    if (!TextUtils.isEmpty(preferenceScreen.title)) {
      val a = activity as SettingsActivity?
      if (a != null) {
        a.toolbar!!.setTitle(preferenceScreen.title)
      }
    }
  }

  override fun doUpdatePreferences() {
    updateSummary(R.string.pref_key__cat_desktop, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as!!.desktopColumnCount, _as!!.desktopRowCount))
    updateSummary(R.string.pref_key__cat_dock, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as!!.dockColumnCount, _as!!.dockRowCount))
    updateSummary(R.string.pref_key__cat_appearance, String.format(Locale.ENGLISH, "Icons: %ddp", _as!!.iconSize))

    when (_as!!.drawerStyle) {
      AppDrawerController.Mode.GRID -> updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.vertical_scroll_drawer)))
      AppDrawerController.Mode.PAGE -> updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)))
      else -> updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)))
    }

    for (resId in ArrayList(Arrays.asList(R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up, R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch, R.string.pref_key__gesture_unpinch))) {
      val preference = findPreference<Preference>(getString(resId))
      val gesture = AppSettings.get().getGesture(resId)
      if (gesture is Intent) {
        updateSummary(resId, String.format(Locale.ENGLISH, "%s: %s", getString(R.string.app), AppManager.getInstance(context!!)!!.findApp(gesture as Intent)!!.label))
      } else if (gesture is LauncherAction.ActionDisplayItem) {
        updateSummary(resId, String.format(Locale.ENGLISH, "%s: %s", getString(R.string.action), (gesture as LauncherAction.ActionDisplayItem)._label))
      } else {
        updateSummary(resId, String.format(Locale.ENGLISH, "%s", getString(R.string.none)))
      }
    }
  }

  override fun onPreferenceChanged(prefs: SharedPreferences, key: String) {
    super.onPreferenceChanged(prefs, key)
    activityRetVal = 1
    if (!noRestart.contains(keyToStringResId(key))) {
      AppSettings.get().appRestartRequired = true
    }
  }


  override fun onPreferenceClicked(preference: Preference, key: String, keyResId: Int): Boolean? {
    val homeActivity = HomeActivity._launcher
    when (keyResId) {
      R.string.pref_key__about -> {
        startActivity(Intent(activity, MoreInfoActivity::class.java))
        return true
      }
      R.string.pref_key__backup -> {
        if (PermissionChecker(activity).doIfExtStoragePermissionGranted()) {
          val i = Intent(activity, FilePickerActivity::class.java)
            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
          activity!!.startActivityForResult(i, Constants.INTENT_BACKUP)
        }
        return true
      }
      R.string.pref_key__restore -> {
        if (PermissionChecker(activity).doIfExtStoragePermissionGranted()) {
          val i = Intent(activity, FilePickerActivity::class.java)
            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
          activity!!.startActivityForResult(i, Constants.INTENT_RESTORE)
        }
        return true
      }
      R.string.pref_key__reset_settings -> {
        DialogHelper.alertDialog(activity as Context, getString(R.string.pref_title__reset_settings), getString(R.string.are_you_sure)) {
          try {
            val p = activity!!.packageManager.getPackageInfo(context!!.packageName, 0)
            val dataDir = p.applicationInfo.dataDir
            File("$dataDir/shared_prefs/app.xml").delete()
            System.exit(0)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
        return true
      }
      R.string.pref_key__reset_database -> {
        DialogHelper.alertDialog(activity as Context, getString(R.string.pref_title__reset_database), getString(R.string.are_you_sure)) {
          val db = HomeActivity._db
          db.onUpgrade(db.writableDatabase, 1, 1)
          AppSettings.get().appFirstLaunch = true
          exitProcess(0)
        }
        return true
      }
      R.string.pref_key__restart -> {
        homeActivity!!.recreate()
        activity!!.finish()
        return true
      }
      R.string.pref_key__hidden_apps -> {
        val intent = Intent(activity, HideAppsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        return true
      }
      R.string.pref_key__minibar -> {
        LauncherAction.RunAction(LauncherAction.Action.EditMinibar, activity as Context)
        return true
      }
      R.string.pref_key__icon_pack -> {
        DialogHelper.startPickIconPackIntent(activity!!)
        return true
      }

      R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up, R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch, R.string.pref_key__gesture_unpinch -> {
        DialogHelper.selectGestureDialog(activity as Context, preference.title.toString()) { _, position, _ ->
          if (position == 1) {
            DialogHelper.selectActionDialog(activity as Context) { _, position, _ -> AppSettings.get().setString(key, LauncherAction.getActionItem(position)!!._action.toString()) }
          } else if (position == 2) {
            DialogHelper.selectAppDialog(activity as Context, object : DialogHelper.OnAppSelectedListener {
              override fun onAppSelected(app: App) {
                AppSettings.get().setString(key, IntentUtil.getIntentAsString(IntentUtil.getIntentFromApp(app)))
              }
            })
          } else {
            AppSettings.get().setString(key, "")
          }
        }
        return true
      }
    }
    return null
  }

  override fun isDividerVisible(): Boolean {
    return true

  }

  companion object {
    val TAG = "com.fisma.trinity.fragment.SettingsMasterFragment"

    private val noRestart = ArrayList(Arrays.asList<Int>(
      R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up,
      R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch,
      R.string.pref_key__gesture_unpinch))
  }
}
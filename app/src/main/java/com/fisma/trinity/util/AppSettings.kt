package com.fisma.trinity.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

import com.fisma.trinity.TrinityApplication
import com.fisma.trinity.R
import com.fisma.trinity.api.WeatherServiceProvider
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.widgets.AppDrawerController
import com.fisma.trinity.widgets.PagerIndicator

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class AppSettings(context: Context) : SharedPreferencesPropertyBackend(context, "app") {
  companion object {

    fun get(): AppSettings {
      return AppSettings(TrinityApplication.get() as Context)
    }
  }

  val desktopColumnCount: Int
    get() = getInt(R.string.pref_key__desktop_columns, 4)

  val desktopRowCount: Int
    get() = getInt(R.string.pref_key__desktop_rows, 6)

  val desktopIndicatorMode: Int
    get() = getIntOfStringPref(R.string.pref_key__desktop_indicator_style, PagerIndicator.Mode.DOTS)

  val desktopOrientationMode: Int
    get() = getIntOfStringPref(R.string.pref_key__desktop_orientation, 0)

  val desktopShowGrid: Boolean
    get() = getBool(R.string.pref_key__desktop_show_grid, true)

  val desktopFullscreen: Boolean
    get() = getBool(R.string.pref_key__desktop_fullscreen, false)

  val desktopShowIndicator: Boolean
    get() = getBool(R.string.pref_key__desktop_show_position_indicator, true)

  val desktopShowLabel: Boolean
    get() = getBool(R.string.pref_key__desktop_show_label, true)

  val searchBarEnable: Boolean
    get() = getBool(R.string.pref_key__search_bar_enable, true)

  val searchBarBaseURI: String
    get() =
      getString(R.string.pref_key__search_bar_base_uri, R.string.pref_default__search_bar_base_uri)

  val searchBarForceBrowser: Boolean
    get() = getBool(R.string.pref_key__search_bar_force_browser, false)

  val searchBarShouldShowHiddenApps: Boolean
    get() = getBool(R.string.pref_key__search_bar_show_hidden_apps, false)

  val userDateFormat: SimpleDateFormat
    @SuppressLint("SimpleDateFormat")
    get() {
      val line1 = getString(R.string.pref_key__date_bar_date_format_custom_1, rstr(R.string.pref_default__date_bar_date_format_custom_1))
      val line2 = getString(R.string.pref_key__date_bar_date_format_custom_2, rstr(R.string.pref_default__date_bar_date_format_custom_2))

      try {
        return SimpleDateFormat("$line1'\n'$line2".replace("''", ""), Locale.getDefault())
      } catch (ex: Exception) {
        return SimpleDateFormat("'Invalid pattern''\n''Invalid Pattern'")
      }

    }

  val desktopDateMode: Int
    get() = getIntOfStringPref(R.string.pref_key__date_bar_date_format_type, 1)

  val desktopDateTextColor: Int
    get() = getInt(R.string.pref_key__date_bar_date_text_color, Color.WHITE)

  val desktopBackgroundColor: Int
    get() = getInt(R.string.pref_key__desktop_background_color, Color.TRANSPARENT)

  val desktopFolderColor: Int
    get() = getInt(R.string.pref_key__desktop_folder_color, Color.parseColor("#ff3d3d3d"))

  val desktopInsetColor: Int
    get() =
      getInt(R.string.pref_key__desktop_inset_color, ContextCompat.getColor(_context, R.color.transparent))

  val minibarBackgroundColor: Int
    get() =
      getInt(R.string.pref_key__minibar_background_color, ContextCompat.getColor(_context, R.color.colorPrimary))

  val desktopIconSize: Int
    get() = iconSize

  val dockEnable: Boolean
    get() = getBool(R.string.pref_key__dock_enable, true)

  val dockColumnCount: Int
    get() = getInt(R.string.pref_key__dock_columns, 5)

  val dockRowCount: Int
    get() = getInt(R.string.pref_key__dock_rows, 1)

  val dockShowLabel: Boolean
    get() = getBool(R.string.pref_key__dock_show_label, false)

  val dockColor: Int
    get() = getInt(R.string.pref_key__dock_background_color, Color.TRANSPARENT)

  val dockIconSize: Int
    get() = iconSize

  val drawerColumnCount: Int
    get() = getInt(R.string.pref_key__drawer_columns, 4)

  val drawerRowCount: Int
    get() = getInt(R.string.pref_key__drawer_rows, 6)

  val drawerStyle: Int
    get() = getIntOfStringPref(R.string.pref_key__drawer_style, AppDrawerController.Mode.GRID)

  val drawerShowCardView: Boolean
    get() = getBool(R.string.pref_key__drawer_show_card_view, true)

  val drawerRememberPosition: Boolean
    get() = getBool(R.string.pref_key__drawer_remember_position, true)

  val drawerShowIndicator: Boolean
    get() = getBool(R.string.pref_key__drawer_show_position_indicator, true)

  val drawerShowLabel: Boolean
    get() = getBool(R.string.pref_key__drawer_show_label, true)

  val drawerBackgroundColor: Int
    get() = getInt(R.string.pref_key__drawer_background_color, rcolor(R.color.darkTransparent))

  val drawerCardColor: Int
    get() = getInt(R.string.pref_key__drawer_card_color, Color.WHITE)

  val drawerLabelColor: Int
    get() = getInt(R.string.pref_key__drawer_label_color, Color.BLACK)

  val drawerFastScrollColor: Int
    get() =
      getInt(R.string.pref_key__drawer_fast_scroll_color, ContextCompat.getColor(Settings.appContext(), R.color.materialRed))

  val gestureFeedback: Boolean
    get() = getBool(R.string.pref_key__gesture_feedback, true)

  val gestureDockSwipeUp: Boolean
    get() = getBool(R.string.pref_key__gesture_quick_swipe, true)

  val gestureDoubleTap: Any?
    get() = getGesture(R.string.pref_key__gesture_double_tap)

  val gestureSwipeUp: Any?
    get() = getGesture(R.string.pref_key__gesture_swipe_up)

  val gestureSwipeDown: Any?
    get() = getGesture(R.string.pref_key__gesture_swipe_down)

  val gesturePinch: Any?
    get() = getGesture(R.string.pref_key__gesture_pinch)

  val gestureUnpinch: Any?
    get() = getGesture(R.string.pref_key__gesture_unpinch)

  val theme: String
    get() = getString(R.string.pref_key__theme, "1")

  val primaryColor: Int
    get() = getInt(R.string.pref_key__primary_color, _context.resources.getColor(R.color.colorPrimary))

  val iconSize: Int
    get() = getInt(R.string.pref_key__icon_size, 52)

  var iconPack: String
    get() = getString(R.string.pref_key__icon_pack, "")
    set(value) = setString(R.string.pref_key__icon_pack, value)

  // invert the value because it is used as a duration
  val animationSpeed: Int
    get() = 100 - getInt(R.string.pref_key__overall_animation_speed_modifier, 84)

  val language: String
    get() = getString(R.string.pref_key__language, "")

  // internal preferences below here
  var dashboardEnable: Boolean
    get() = getBool(R.string.pref_key__minibar_enable, true)
    set(value) = setBool(R.string.pref_key__minibar_enable, value)


  var searchUseGrid: Boolean
    get() = getBool(R.string.pref_key__desktop_search_use_grid, false)
    set(enabled) = setBool(R.string.pref_key__desktop_search_use_grid, enabled)

  var hiddenAppsList: ArrayList<String>
    get() = getStringList(R.string.pref_key__hidden_apps)
    set(value) = setStringList(R.string.pref_key__hidden_apps, value)

  var desktopPageCurrent: Int
    get() = getInt(R.string.pref_key__desktop_current_position, 0)
    set(value) = setInt(R.string.pref_key__desktop_current_position, value)

  var desktopLock: Boolean
    get() = getBool(R.string.pref_key__desktop_lock, false)
    set(value) = setBool(R.string.pref_key__desktop_lock, value)

  // MUST be committed
  var appRestartRequired: Boolean
    get() = getBool(R.string.pref_key__queue_restart, false)
    @SuppressLint("ApplySharedPref")
    set(value) {
      _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__queue_restart), value).commit()
    }

  var enableBlur: Boolean
    get() = getBool("pref_key__enable_blur", true)
    set(value) {
      _prefApp.edit().putBoolean("pref_key__enable_blur", value).commit()
    }

  var blurRadius: Float
    get() = getFloat("pref_key__blur_radius", 1f)
    set(value) {
      _prefApp.edit().putFloat("pref_key__blur_radius", value).commit()
    }
  /**
   * Weather Settings
   * */
  var weatherProvider: WeatherServiceProvider.WeatherProvider
    get() = WeatherServiceProvider.WeatherProvider.valueOf(getString(R.string.pref_key__weather_provider, "OpenWeatherMap"))
    set(value) {
      _prefApp.edit().putString(context.getString(R.string.pref_key__weather_provider), value.toString()).commit()
    }
  var weatherCityName: String
    get() = getString(R.string.pref_key__weather_city, "Balikpapan")
    set(value) {
      _prefApp.edit().putString(context.getString(R.string.pref_key__weather_city), value).commit()
    }

  var weatherLastFetch: Long
    get() = getLong(R.string.pref_key__weather_last_fetch, 0)
    set(value) {
      _prefApp.edit().putLong(context.getString(R.string.pref_key__weather_last_fetch), value).commit()
    }
  var weatherForecastLastFetch: Long
    get() = getLong(R.string.pref_key__weather_forecast_last_fetch, 0)
    set(value) {
      _prefApp.edit().putLong(context.getString(R.string.pref_key__weather_forecast_last_fetch), value).commit()
    }

  var weatherUpdateInterval: Long
    get() = getLong(R.string.pref_key__weather_update_interval, 60000 /*1 minute*/)
    set(value) {
      _prefApp.edit().putLong(context.getString(R.string.pref_key__weather_update_interval), value).commit()
    }
  // MUST be committed
  var appFirstLaunch: Boolean
    get() = getBool(R.string.pref_key__first_start, true)
    @SuppressLint("ApplySharedPref")
    set(value) {
      _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__first_start), value).commit()
    }

  fun getGesture(key: Int): Any? {
    // return either ActionItem or IntentUtil
    val result = getString(key, "")
    var gesture: Any? = LauncherAction.getActionItem(result)
    // no action was found so it must be an intent string
    if (gesture == null) {
      gesture = IntentUtil.getIntentFromString(result)
      if (AppManager.getInstance(_context)!!.findApp(gesture) == null) gesture = null
    }
    // reset the setting if invalid value
    if (gesture == null) {
      setString(key, null)
    }
    return gesture
  }

  @SuppressLint("ApplySharedPref")
  fun setAppShowIntro(value: Boolean) {
    // MUST be committed
    _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__show_intro), value).commit()
  }


}

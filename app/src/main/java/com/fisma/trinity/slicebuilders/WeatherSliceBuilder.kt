package com.fisma.trinity.slicebuilders

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.builders.*
import android.os.Handler
import android.util.Log
import com.fisma.trinity.R
import com.fisma.trinity.TrinityPluginProvider
import com.fisma.trinity.api.*
import com.fisma.trinity.interfaces.SliceBuilder
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.receivers.NetworkStateReceiver
import com.fisma.trinity.receivers.SliceActionsBroadcastReceiver
import com.fisma.trinity.util.runOnMainThread
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import java.util.*


class WeatherSliceBuilder(
  val context: Context,
  sliceUri: Uri
) : SliceBuilder(sliceUri) {

  var weatherProvider: WeatherServiceProvider = WeatherServiceProvider.getInstance(context, Settings.appSettings().weatherProvider)
  var mHandler: Handler = Handler(Looper.getMainLooper())
  val weatherUri: String = context.resources.getString(R.string.weather_slice_uri)


  init {
    fetchForecasts()
    fetchCurrentWeather()
  }

  private fun fetchCurrentWeather() {
    runOnMainThread {
      val lastFetch = Settings.appSettings().weatherLastFetch
      val currentTime = System.currentTimeMillis()
      val updateInterval = Settings.appSettings().weatherUpdateInterval

      if (NetworkStateReceiver.isConnected()) {
        if (lastFetch == 0.toLong() || currentTime - lastFetch > updateInterval || firstWeatherFetch) {
          Settings.appSettings().weatherLastFetch = currentTime

          val cityName = Settings.appSettings().weatherCityName
          weatherProvider.getCurrentWeather(cityName) { weather, t ->
            if (t != null) {
              Log.e(TAG, t.message)
            } else {
              currentWeather = weather
              context.contentResolver.notifyChange(Uri.parse(weatherUri), null)
            }
          }

          firstWeatherFetch = false
          mHandler.postDelayed({
            fetchCurrentWeather()
          }, updateInterval)
        }
      }
    }
  }

  private fun fetchForecasts() {
    runOnMainThread {
      val lastFetch = Settings.appSettings().weatherForecastLastFetch
      val currentTime = System.currentTimeMillis()
      val updateInterval = 10800000.toLong() // 3 hours

      if (NetworkStateReceiver.isConnected()) {
        if (lastFetch == 0.toLong() || currentTime - lastFetch > updateInterval || firstForecastFetch) {
          Settings.appSettings().weatherForecastLastFetch = currentTime
          weatherProvider.getTodayForecasts(Settings.appSettings().weatherCityName) { forecasts, throwable ->
            // TODO: handle when quota exceed limit in AccuWeather provider to show a message
            // and let user use their own api key
            if (throwable == null && !forecasts.isNullOrEmpty()) {
              currentWeathers = forecasts
              val weatherUri = context.resources.getString(R.string.weather_slice_uri)
              context.contentResolver.notifyChange(Uri.parse(weatherUri), null)
            } else {

            }
          }

          mHandler.postDelayed({
            fetchForecasts()
          }, updateInterval)

          if (firstForecastFetch)
            firstForecastFetch = false
        }
      }
    }
  }

  override fun buildSlice(): Slice {

    return list(context, sliceUri, ListBuilder.INFINITY) {
      val action = SliceAction.create(
        SliceActionsBroadcastReceiver.getIntent(
          context,
          TrinityPluginProvider.ACTION_WEATHER_SETTINGS,
          "open weather settings"
        ),
        IconCompat.createWithResource(context, R.mipmap.ic_launcher),
        ListBuilder.ICON_IMAGE,
        "Weather is happening!"
      )
      val cityName = Settings.appSettings().weatherCityName
      val currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
      header {
        primaryAction = action
        title = cityName
        subtitle = currentDate
      }
      if (currentWeather != null) {
        gridRow {
          cell {
            addImage(
              IconCompat.createWithBitmap(currentWeather!!._icon).setTintList(null),
              ListBuilder.ICON_IMAGE
            )
          }
          cell {
            val currentWeatherTxt = currentWeather!!._temperature.toString() + "º in " + cityName
            addTitleText(currentWeatherTxt)
          }

          cell {
            addTitleText(currentWeather!!._condition!!)
          }
        }
      }

      if (!currentWeathers!!.isNullOrEmpty()) {
        gridRow {
          for (forecast in currentWeathers!!) {
            cell {
              if (forecast._icon != null) {
                addImage(
                  IconCompat.createWithBitmap(forecast._icon),
                  ListBuilder.SMALL_IMAGE
                )
              }
              val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
              addTitleText(sdf.format(forecast._date))
              addText(forecast._temperature.toString() + "º")
            }
          }
        }
      }
    }
  }

  companion object {
    const val TAG = "WeatherSliceBuilder"
    var isQuotaExceedLimit = false
    var currentWeathers: ArrayList<Weather>? = null
    var currentWeather: Weather? = null
    var firstForecastFetch = true
    var firstWeatherFetch = true
  }
}
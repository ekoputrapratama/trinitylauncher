package com.fisma.trinity.api

import android.content.Context

abstract class WeatherServiceProvider {
  var mCallbacks: ArrayList<(weathers: ArrayList<Weather>?, t: Throwable?) -> Unit> = ArrayList()
  var isInRequest = false

  abstract fun getTodayForecasts(cityName: String, callback: (weathers: ArrayList<Weather>?, t: Throwable?) -> Unit)
  abstract fun getCurrentWeather(cityName: String, callback: (weather: Weather?, t: Throwable?) -> Unit)

  fun dispatchToCallbacks(weathers: ArrayList<Weather>?, t: Throwable? = null) {
    for (callback in mCallbacks) {
      callback(weathers, t)
    }
    mCallbacks.clear()
  }

  enum class WeatherProvider {
    OpenWeatherMap, AccuWeather
  }

  companion object {
    var mInstance: WeatherServiceProvider? = null
    fun getInstance(context: Context, provider: WeatherProvider): WeatherServiceProvider {
      if (mInstance == null) {
        mInstance = when (provider) {
          WeatherProvider.AccuWeather -> {
            AccuWeatherServiceProvider(context)
          }
          WeatherProvider.OpenWeatherMap -> {
            OpenWeatherMapServiceProvider(context)
          }
        }
      }

      return mInstance!!
    }
  }
}
package com.fisma.trinity.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.format.DateUtils
import android.util.Log
import com.fisma.trinity.slicebuilders.WeatherSliceBuilder
import com.fisma.trinity.util.ImageUtil
import com.fisma.trinity.util.runOnAsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LongLogTag")
class OpenWeatherMapServiceProvider(val context: Context) : WeatherServiceProvider() {

  companion object {
    const val TAG = "OpenWeatherMapServiceProvider"
    const val OPEN_WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/"
  }

  var mService: OpenWeatherMapService


  init {
    val retrofit = Retrofit.Builder()
      .baseUrl(OPEN_WEATHER_BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    mService = retrofit.create(OpenWeatherMapService::class.java)
  }

  override fun getCurrentWeather(cityName: String, callback: (weather: Weather?, t: Throwable?) -> Unit) {
    val call = mService.getCurrentWeather(cityName)

    call.enqueue(object : Callback<OpenWeatherMapWeatherResponse> {
      override fun onFailure(call: Call<OpenWeatherMapWeatherResponse>, t: Throwable) {
        Log.e(TAG, t.message)
        callback(null, t)
      }

      override fun onResponse(call: Call<OpenWeatherMapWeatherResponse>, response: Response<OpenWeatherMapWeatherResponse>) {
        if (response.isSuccessful && response.body() != null) {
          val currentWeather = response.body()!!
          runOnAsyncTask {

            try {

              val client = OkHttpClient()
              val request = Request.Builder()
                .url("http://openweathermap.org/img/w/" + currentWeather.weather!![0].icon + ".png")
                .build()
              val res = client.newCall(request).execute()

              val weather = Weather().build {
                date(currentWeather.date)
                temperature(currentWeather.main!!.temp.toInt())
                minTemperature(currentWeather.main!!.temp_min.toInt())
                maxTemperature(currentWeather.main!!.temp_max.toInt())
                condition(currentWeather.weather!![0].description!!)
                if (res.isSuccessful && res?.body() != null) {
                  val bmp = BitmapFactory.decodeStream(res.body()!!.byteStream())
                  icon(bmp)
                }
              }
              callback(weather, null)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }
        } else {
          callback(null, Throwable(response.message()))
        }
      }

    })
  }

  override fun getTodayForecasts(city: String, callback: (weathers: ArrayList<Weather>?, t: Throwable?) -> Unit) {
    mCallbacks.add(callback)
    if (!isInRequest) {
      isInRequest = true
      // need to run this on async task
      runOnAsyncTask {
        // we need to use city id for better accuracy
        var call: Call<OpenWeatherMapForecastsResponse>
        // first try to get city id by name
        val cityCall = mService.getCityByName(city)
        val cityResponse = cityCall.execute()

        call = if (cityResponse.isSuccessful && cityResponse.body() != null) {
          val list = cityResponse.body()!!.list
          if (list != null && list.isNotEmpty()) {
            // city found, get the id and request to get 5 day forecast by city id
            val cityId = list[0].id
            mService.get5DayForecastsById(cityId)
          } else { // city cannot be found fallback to get weather by city name
            mService.get5DayForecastsByName(city)
          }
        } else {// request failed fallback to get weather by city name
          mService.get5DayForecastsByName(city)
        }

        val forecastResponse = call.execute()

        if (forecastResponse.isSuccessful) {
          val res = forecastResponse.body()
          if (res?.list != null && res.list!!.isNotEmpty()) {
            val forecasts = res.list!!
            val results: ArrayList<Weather> = ArrayList()
            val days: ArrayList<String> = ArrayList()
            // OpenWheaterMap gave us per 3 hours data, but we just need 1 data per day
            for (forecast in forecasts) {
              val sdf = SimpleDateFormat("EEE")
              var key = sdf.format(Date(forecast.date * 1000))
              var forecastDate = forecast.date * 1000

              if (DateUtils.isToday(forecastDate) && results.size <= 5) {
//                days.add(key)
                // get the icon using OkHttp
                val client = OkHttpClient()
                val request = Request.Builder()
                  .url("http://openweathermap.org/img/w/" + forecast.weather!![0].icon + ".png")
                  .build()
                val res = client.newCall(request).execute()

                results.add(Weather().build {
                  date(forecast.date * 1000)
                  temperature(forecast.main!!.temp.toInt())
                  minTemperature(forecast.main!!.temp_min.toInt())
                  maxTemperature(forecast.main!!.temp_max.toInt())
                  if (res.isSuccessful && res?.body() != null) {
                    val bmp = BitmapFactory.decodeStream(res.body()?.byteStream())
                    icon(bmp)
                  }
                })
              }
            }
            isInRequest = false
            dispatchToCallbacks(results, null)
          }
        } else {
          Log.e(WeatherSliceBuilder.TAG, forecastResponse.message())
          isInRequest = false
          dispatchToCallbacks(null, Throwable(forecastResponse.message()))
        }
      }
    }
  }
}
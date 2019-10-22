package com.fisma.trinity.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.fisma.trinity.util.Tool
import com.fisma.trinity.util.runOnAsyncTask
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("LongLogTag")
class AccuWeatherServiceProvider(val context: Context) : WeatherServiceProvider() {
  override fun getCurrentWeather(cityName: String, callback: (weather: Weather?, t: Throwable?) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  companion object {
    const val TAG = "AccuWeatherServiceProvider"
    const val ACCUWEATHER_BASE_URL = "https://dataservice.accuweather.com/"
    const val ACCUWEATHER_ICON_BASE_URL = "https://developer.accuweather.com/"
  }

  var mService: AccuWeatherService

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl(ACCUWEATHER_BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    mService = retrofit.create(AccuWeatherService::class.java)
  }

  override fun getTodayForecasts(cityName: String, callback: (weathers: ArrayList<Weather>?, t: Throwable?) -> Unit) {
    mCallbacks.add(callback)

    if (!isInRequest) {
      val call = mService.getCityByName(cityName)
      call.enqueue(object : Callback<List<AccuWeatherCity>> {

        override fun onFailure(call: Call<List<AccuWeatherCity>>, t: Throwable) {
          Log.e(TAG, t.message)
          isInRequest = false
          dispatchToCallbacks(null, t)
        }

        override fun onResponse(call: Call<List<AccuWeatherCity>>, response: Response<List<AccuWeatherCity>>) {
          val cities = response.body()
          if (cities != null && cities.isNotEmpty()) {
            val city = cities[0]
            Log.d(TAG, "city key=${city.key} localizedName${city.localizedName}")
            fetchForecasts(city.key!!)
          }
        }
      })
    }
  }

  fun fetchForecasts(key: String) {
    val call = mService.get5DayForecasts(key)
    call.enqueue(object : Callback<AccuWeatherForecastsResponse> {
      override fun onFailure(call: Call<AccuWeatherForecastsResponse>, t: Throwable) {
        Log.e(TAG, t.message)
        isInRequest = false
        dispatchToCallbacks(null, t)
      }

      override fun onResponse(call: Call<AccuWeatherForecastsResponse>, response: Response<AccuWeatherForecastsResponse>) {
        Log.d(TAG, "onResponse")
        val body = response.body()
        val forecasts = body?.forecasts
        if (forecasts != null && forecasts.isNotEmpty()) {
          Log.d(TAG, "forecast size ${forecasts.size}")
          val results = ArrayList<Weather>()
          for ((index, forecast) in forecasts.withIndex()) {
            val forecastDate = forecast.date!!
            val minTemperature = forecast.temperature!!.min!!.value
            val maxTemperature = forecast.temperature!!.max!!.value
            val retrofit = Retrofit.Builder()
              .baseUrl(ACCUWEATHER_ICON_BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .build()
            val service = retrofit.create(AccuWeatherService::class.java)
            val iconName = if (forecast.day!!.icon.toString().length == 1) {
              "0" + forecast.day!!.icon.toString() + "-s.png"
            } else forecast.day!!.icon.toString() + "-s.png"

            var call = if (Tool.isDayTime()) {
              service.getWeatherIcon(iconName)
            } else {
              service.getWeatherIcon(iconName)
            }

            runOnAsyncTask {
              Log.d(TAG, "fetching image in async task")
              val forecastIndex = index
              val responseBody = call.execute().body()
              var bmp: Bitmap? = null
              if (responseBody != null) {
                bmp = BitmapFactory.decodeStream(call.execute().body()!!.byteStream())
              }
              results.add(Weather().build {
                date(forecastDate)
                minTemperature(minTemperature)
                maxTemperature(maxTemperature)
                // display the image data in a ImageView or save it
                if (bmp != null)
                  icon(bmp)
              })

              if (forecastIndex == forecasts.size - 1) {
                isInRequest = false
                dispatchToCallbacks(results, null)
              }
            }
//            call.enqueue(object : Callback<ResponseBody> {
//              val forecastIndex = index
//              override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e(TAG, t.message)
//              }
//
//              override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful && response.body() != null) {
//                  val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
//                  results.add(Weather().build {
//                    date(forecastDate)
//                    minTemperature(minTemperature)
//                    maxTemperature(maxTemperature)
//                    // display the image data in a ImageView or save it
//                    icon(bmp)
//                  })
//                } else {
//                  Log.e(TAG, "Error with code ${response.code()} when request to ${call.request().url().toString()}")
//                }
//                if (forecastIndex == forecasts.size - 1) {
//                  isInRequest = false
//                  dispatchToCallbacks(results, null)
//                }
//              }
//            })
            Log.d(TAG, "forecast date: ${forecast.date} minTemperatur=${minTemperature} maxTemperature=$maxTemperature")
          }

        }
      }
    })
  }
}
package com.fisma.trinity.api

import com.fisma.trinity.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface AccuWeatherService {
  @GET("locations/v1/cities/search?")
  fun getCityByName(@Query("q") cityName: String, @Query("metric") metric: Boolean = true,
                    @Query("apikey") key: String = BuildConfig.ACCU_WEATHER_API_KEY): Call<List<AccuWeatherCity>>

  @GET("forecasts/v1/daily/5day/{cityKey}")
  fun get5DayForecasts(@Path("cityKey") cityKey: String, @Query("apikey") key: String = BuildConfig.ACCU_WEATHER_API_KEY): Call<AccuWeatherForecastsResponse>

  @GET("sites/default/files/{iconName}")
  fun getWeatherIcon(@Path("iconName") iconName: String): Call<ResponseBody>
}


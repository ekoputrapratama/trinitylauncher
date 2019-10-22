package com.fisma.trinity.api

import com.fisma.trinity.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {
  @GET("weather?")
  fun getCurrentWeather(@Query("q") cityName: String, @Query("units") units: String = "metric",
                        @Query("apikey") app_id: String = BuildConfig.OPEN_WEATHER_API_KEY): Call<OpenWeatherMapWeatherResponse>

  @GET("weather?")
  fun getCurrentWeatherData(@Query("q") city: String): Call<OpenWeatherMapWeatherResponse>

  @GET("forecast?")
  fun get5DayForecastsByName(@Query("q") city: String, @Query("units") units: String = "metric",
                             @Query("mode") mode: String = "json",
                             @Query("apikey") apiKey: String = BuildConfig.OPEN_WEATHER_API_KEY): Call<OpenWeatherMapForecastsResponse>

  @GET("forecast?")
  fun get5DayForecastsById(@Query("id") city: Int, @Query("units") units: String = "metric",
                           @Query("mode") mode: String = "json",
                           @Query("apikey") apiKey: String = BuildConfig.OPEN_WEATHER_API_KEY): Call<OpenWeatherMapForecastsResponse>

  @GET("/find?")
  fun getCityByName(@Query("q") cityName: String): Call<OpenWeatherMapCityResponse>
}
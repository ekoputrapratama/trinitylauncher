package com.fisma.trinity.api

import com.google.gson.annotations.SerializedName

class OpenWeatherMapWeatherResponse {
  @SerializedName("sys")
  var sys: Sys? = null

  @SerializedName("main")
  var main: Main? = null
  @SerializedName("weather")
  var weather: List<OpenWeatherMapWeather>? = null
  @SerializedName("dt")
  var date: Long = 0
}

class OpenWeatherMapForecast {
  @SerializedName("dt")
  var date: Long = 0
  @SerializedName("main")
  var main: Main? = null
  @SerializedName("sys")
  var sys: Sys? = null
  @SerializedName("weather")
  var weather: List<OpenWeatherMapWeather>? = null
}

class OpenWeatherMapWeather {
  @SerializedName("id")
  var id: Int = 0
  @SerializedName("main")
  var main: String? = null
  @SerializedName("description")
  var description: String? = null
  @SerializedName("icon")
  var icon: String? = null
}

class OpenWeatherMapForecastsResponse {
  @SerializedName("list")
  var list: List<OpenWeatherMapForecast>? = null
}

class OpenWeatherMapCityResponse {
  @SerializedName("list")
  var list: List<OpenWeatherMapCity>? = null
}

class OpenWeatherMapCity {
  @SerializedName("id")
  var id: Int = 0
  @SerializedName("coord")
  var coordinate: OpenWeatherMapCoord? = null
}

class OpenWeatherMapCoord {
  @SerializedName("lat")
  var latitude: Float = 0f
  @SerializedName("lon")
  var longitude: Float = 0f
}

class Main {
  @SerializedName("temp")
  var temp: Float = 0.0f
  @SerializedName("humidity")
  var humidity: Float = 0.0f
  @SerializedName("pressure")
  var pressure: Float = 0.0f
  @SerializedName("temp_min")
  var temp_min: Float = 0.0f
  @SerializedName("temp_max")
  var temp_max: Float = 0.0f
}

class Sys {
  @SerializedName("country")
  var country: String? = null
}
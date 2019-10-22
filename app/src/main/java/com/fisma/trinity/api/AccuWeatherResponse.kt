package com.fisma.trinity.api

import com.google.gson.annotations.SerializedName

class AccuWeatherForecastsResponse {
  @SerializedName("DailyForecasts")
  var forecasts: List<AccuWeatherForecast>? = null
  @SerializedName("Headline")
  var headline: Headline? = null
}

class Headline {
  @SerializedName("EffectiveDate")
  var effectiveDate: String? = null
  @SerializedName("EffectiveEpochDate")
  var effectiveEpochDate: Long = 0
}

class AccuWeatherCity {
  @SerializedName("Key")
  var key: String? = null
  @SerializedName("LocalizedName")
  var localizedName: String? = null
  @SerializedName("EnglishName")
  var EnglishName: String? = null
  @SerializedName("Region")
  var region: AccuWeatherRegion? = null
  @SerializedName("Country")
  var country: AccuWeatherCountry? = null
}

class AccuWeatherRegion {
  @SerializedName("ID")
  var id: String? = null
  @SerializedName("LocalizedName")
  var localizedName: String? = null
  @SerializedName("EnglishName")
  var EnglishName: String? = null
}

class AccuWeatherCountry {
  @SerializedName("ID")
  var id: String? = null
  @SerializedName("LocalizedName")
  var localizedName: String? = null
  @SerializedName("EnglishName")
  var EnglishName: String? = null
}

class AccuWeatherForecast {
  @SerializedName("Date")
  var date: String? = null
  @SerializedName("EpochDate")
  var epochDate: Long = 0
  @SerializedName("Temperature")
  var temperature: AccuWeatherTemperature? = null
  @SerializedName("Day")
  var day: AccuWeatherTime? = null
  @SerializedName("Night")
  var night: AccuWeatherTime? = null
}

class AccuWeatherTemperature {
  @SerializedName("Minimum")
  var min: AccuWeatherSubTemperature? = null
  @SerializedName("Maximum")
  var max: AccuWeatherSubTemperature? = null
}

class AccuWeatherTime {
  @SerializedName("Icon")
  var icon: Int = 0
}

class AccuWeatherSubTemperature {
  @SerializedName("Value")
  var value: Int = 0
  @SerializedName("Unit")
  var unit: String? = null
  @SerializedName("UnitType")
  var unitType: Int = 0
}
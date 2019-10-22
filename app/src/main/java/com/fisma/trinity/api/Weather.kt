package com.fisma.trinity.api

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.fisma.trinity.util.ImageUtil
import java.text.SimpleDateFormat
import java.util.*

class Weather {
  var _date: Date? = null
  var _minTemperature: Int = 0
  var _maxTemperature: Int = 0
  var _icon: Bitmap? = null
  var _temperature: Int = 0
  var _condition: String? = null

  fun date(date: Date) {
    _date = date
  }

  fun date(date: Long) {
    _date = Date(date)
  }

  fun date(date: String) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    _date = dateFormat.parse(date)
  }

  fun condition(condition: String) {
    _condition = condition
  }

  fun temperature(temperature: Int) {
    _temperature = temperature
  }

  fun minTemperature(temp: Int) {
    _minTemperature = temp
  }

  fun maxTemperature(temp: Int) {
    _maxTemperature = temp
  }

  fun icon(bitmap: Bitmap) {
    _icon = bitmap
  }

  fun icon(drawable: Drawable) {
    _icon = ImageUtil.drawableToBitmap(drawable)
  }

  inline fun build(func: Weather.() -> Any): Weather {
    this.func()
    return this
  }
}
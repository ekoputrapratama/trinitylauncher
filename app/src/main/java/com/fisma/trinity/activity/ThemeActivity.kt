package com.fisma.trinity.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fisma.trinity.R
import com.fisma.trinity.util.AppSettings


abstract class ThemeActivity : AppCompatActivity() {

  internal var _appSettings: AppSettings? = null
  private var _currentTheme: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {

    _appSettings = AppSettings.get()
    _currentTheme = _appSettings!!.theme
    if (_appSettings!!.theme == "0") {
      setTheme(R.style.NormalActivity_Light)
    } else if (_appSettings!!.theme == "1") {
      setTheme(R.style.NormalActivity_Dark)
    } else {
      setTheme(R.style.NormalActivity_Black)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = dark(_appSettings!!.primaryColor, 0.8)
      window.navigationBarColor = _appSettings!!.primaryColor
    }
    super.onCreate(savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    if (_appSettings!!.theme != _currentTheme) {
      restart()
    }
  }

  protected fun restart() {
    val intent = Intent(this, javaClass)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    overridePendingTransition(0, 0)
    startActivity(intent)
  }

  fun dark(color: Int, factor: Double): Int {
    val a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    return Color.argb(a, Math.max((r * factor).toInt(), 0), Math.max((g * factor).toInt(), 0), Math.max((b * factor).toInt(), 0))
  }
}

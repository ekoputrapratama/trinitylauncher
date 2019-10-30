package com.fisma.trinity.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.fisma.trinity.R

class WeatherSettings : ThemeActivity() {
  var toolbar: Toolbar? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather_settings)
    toolbar = findViewById(R.id.toolbar)
    toolbar!!.title = getString(R.string.weather_preference)
    setSupportActionBar(toolbar)
//    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.settings, SettingsFragment())
      .commit()

  }

  class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.weather_preferences, rootKey)
    }
  }
}
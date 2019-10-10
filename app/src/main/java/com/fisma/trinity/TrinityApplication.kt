package com.fisma.trinity

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.fisma.trinity.util.AppSettings
import net.gsantner.opoc.util.ContextUtils

class TrinityApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    _instance = this
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    SoLoader.init(this, false)
    if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
      val client = AndroidFlipperClient.getInstance(this)
      client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
      client.addPlugin(DatabasesFlipperPlugin(this))
      client.addPlugin(
        SharedPreferencesFlipperPlugin(this, "preferences_master"))
      client.start()
    }

    val appSettings = AppSettings.get()
    val contextUtils = ContextUtils(applicationContext)
    contextUtils.setAppLanguage(appSettings.language)
  }

  companion object {
    private var _instance: TrinityApplication? = null

    fun get(): TrinityApplication? {
      return _instance
    }
  }
}
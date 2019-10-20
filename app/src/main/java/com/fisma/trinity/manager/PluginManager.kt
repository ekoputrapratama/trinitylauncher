package com.fisma.trinity.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.Log
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.fisma.trinity.R
import com.fisma.trinity.model.Plugin
import com.fisma.trinity.util.DatabaseHelper
import com.fisma.trinity.widgets.DashboardView

class PluginManager {
  var mContext: Context? = null
  var mPackageManager: PackageManager? = null


  companion object {
    const val TAG = "PluginManager"
    const val SLICE_INTENT_ACTION = "androidx.intent.SLICE_ACTION"
    @SuppressLint("StaticFieldLeak")
    private var mInstance: PluginManager? = null
    const val PLUGIN_INTENT_CATEGORY = "com.fisma.trinity.PLUGIN"
    var mAvailablePlugins: ArrayList<Plugin> = ArrayList()

    fun initialize(context: Context) {
      if (mInstance == null) {
        mInstance = PluginManager()
      }
      mInstance!!.mContext = context
      mInstance!!.mPackageManager = context.packageManager
      mInstance!!.init()
    }

    fun onAppsChanged() {
      if (mInstance != null) {
        mInstance!!.init()
      }
    }

    fun initViews(dashboard: DashboardView) {
      Log.d(TAG, "initViews")
      for (plugin in mAvailablePlugins) {
        Log.d(TAG, "init view for plugin ${plugin._label} ${plugin._uri.toString()}")
        dashboard.addPlugin(plugin)
      }
    }
  }

  fun init() {
    Log.d(TAG, "init()")
    mAvailablePlugins.clear()
    val db = DatabaseHelper.getInstance(mContext!!)
    val plugins = db.plugins
    if (plugins.isEmpty()) {
      val intent = Intent(Intent.ACTION_VIEW)
      intent.addCategory(PLUGIN_INTENT_CATEGORY)

      val resolveInfos = mPackageManager!!.queryIntentActivities(intent, PackageManager.GET_META_DATA)
      Log.d(TAG, "plugin size ${resolveInfos.size}")
      for (info in resolveInfos) {

        val metaData = info.activityInfo.metaData
        if (metaData != null) {
          val uri = info.activityInfo.metaData.getString("android.metadata.SLICE_URI")
          if (uri != null) {
            Log.d(TAG, "found new plugin ${info.loadLabel(mPackageManager)} $uri")
            val plugin = Plugin().build {
              label(info.loadLabel(mPackageManager)?.toString())
              packageName(info.activityInfo.packageName)
              uri(uri)
            }
            db.savePlugin(plugin)
            mAvailablePlugins.add(plugin)
          }
        }
      }
    } else {
      Log.d(TAG, "plugin size ${plugins.size}")
      mAvailablePlugins = plugins
    }
  }

  private fun getLayoutForPlugin(packageName: String): XmlResourceParser {
    val resources = getResourceForPlugin(packageName)
    val layoutId = resources.getIdentifier("view_main", "layout", packageName)
    return resources.getLayout(layoutId)
  }

  private fun getResourceForPlugin(packageName: String): Resources {
    return mPackageManager!!.getResourcesForApplication(packageName)
  }
}
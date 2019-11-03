package com.fisma.trinity.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.Log
import com.fisma.trinity.model.Plugin
import com.fisma.trinity.util.DatabaseHelper
import com.fisma.trinity.util.runOnAsyncTask
import com.fisma.trinity.widgets.DashboardView

class PluginManager {
  var mPackageManager: PackageManager? = null
  var mDashboardView: DashboardView? = null
  var db: DatabaseHelper? = null

  companion object {
    const val TAG = "PluginManager"
    const val SLICE_INTENT_ACTION = "androidx.intent.SLICE_ACTION"
    private var mInstance: PluginManager? = null
    const val PLUGIN_INTENT_CATEGORY = "com.fisma.trinity.PLUGIN"
    var mAvailablePlugins: ArrayList<Plugin> = ArrayList()

    fun initialize(context: Context) {
      if (mInstance == null) {
        mInstance = PluginManager()
      }
      mInstance!!.mPackageManager = context.packageManager
      mInstance!!.init(context)
    }

    fun initViews(dashboard: DashboardView) {
      Log.d(TAG, "initViews")
      mInstance?.mDashboardView = dashboard
      for (plugin in mAvailablePlugins) {
        Log.d(TAG, "init view for plugin ${plugin.label} ${plugin.uri.toString()}")
        if (plugin.enabled)
          dashboard.addPlugin(plugin)
      }
    }

    fun getInstance(): PluginManager {
      if (mInstance == null) {
        mInstance = PluginManager()
      }
      return mInstance!!
    }
  }

  fun removePlugin(packageName: String, className: String) {
    mAvailablePlugins = mAvailablePlugins.filter { it.packageName != packageName && it.className != className } as ArrayList<Plugin>

    if (mDashboardView != null) {
      mDashboardView!!.removePlugin(packageName, className)
    }
  }

  fun addPlugin(plugin: Plugin) {
    mAvailablePlugins.add(plugin)
    mDashboardView?.addPlugin(plugin)
  }

  fun addPlugin(packageName: String, className: String) {
    if (mPackageManager != null) {
      val intent = Intent()
      intent.setClassName(packageName, className)
      val infos = mPackageManager!!.queryIntentActivities(intent, PackageManager.GET_META_DATA)

      if (infos.size > 0) {
        val info = infos[0]
        val metaData = info.activityInfo.metaData

        if (metaData != null) {
          val uri = info.activityInfo.metaData.getString("android.metadata.SLICE_URI")

          if (uri != null) {
            val plugin = Plugin.Builder()
              .setClassName(info.activityInfo.name)
              .setPackageName(info.activityInfo.packageName)
              .setLabel(info.loadLabel(mPackageManager)?.toString())
              .setUri(uri)
              .build()

            mAvailablePlugins.add(plugin)
          }
        }
      }
    }
  }

  fun reloadPlugins() {
    if (db == null || mPackageManager == null) return
    mAvailablePlugins.clear()
    db!!.clearTable("plugins")

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
          val plugin = Plugin.Builder()
            .setClassName(info.activityInfo.name)
            .setPackageName(info.activityInfo.packageName)
            .setLabel(info.loadLabel(mPackageManager)?.toString())
            .setUri(uri)
            .build()
          db!!.savePlugin(plugin)
          mAvailablePlugins.add(plugin)
        }
      }
    }
    mDashboardView?.setPluginList(mAvailablePlugins)
  }

  fun init(context: Context) {
    Log.d(TAG, "init()")
    db = DatabaseHelper.getInstance(context)
    val plugins = db!!.plugins
    if (plugins.isEmpty()) {
      reloadPlugins()
    } else {
      Log.d(TAG, "plugin size ${plugins.size}")
      mAvailablePlugins = plugins.toCollection(arrayListOf())
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
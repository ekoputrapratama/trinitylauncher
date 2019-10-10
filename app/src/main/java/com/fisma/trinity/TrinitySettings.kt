package com.fisma.trinity

import android.content.Context
import android.util.Log
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.util.AppManager
import com.fisma.trinity.util.AppSettings
import com.fisma.trinity.util.DatabaseHelper
import com.fisma.trinity.util.HomeEventHandler
import com.fisma.trinity.viewutil.WorkspaceGestureListener

class TrinitySettings(activity: HomeActivity) : Settings() {
  override var appSettings: AppSettings = AppSettings.get()
  override var appContext: Context = TrinityApplication.get() as Context
  override var appLoader: AppManager = AppManager.getInstance(activity)!!
  override var dataManager: DatabaseHelper = DatabaseHelper(activity)
  override lateinit var logger: Logger
  override lateinit var workspaceGestureCallback: WorkspaceGestureListener.WorkspaceGestureCallback
  override var eventHandler: EventHandler = HomeEventHandler()

  init {
    workspaceGestureCallback = HomeActivity.HomeGestureCallback(appSettings)
    logger = object : Logger {
      override fun log(source: Any, priority: Int, tag: String, msg: String, vararg args: Any) {
        Log.println(priority, tag, String.format(msg, *args))
      }
    }
  }
}
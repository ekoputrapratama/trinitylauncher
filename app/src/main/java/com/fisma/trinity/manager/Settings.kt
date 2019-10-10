package com.fisma.trinity.manager

import android.content.Context
import com.fisma.trinity.interfaces.DialogListener
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.AppManager
import com.fisma.trinity.util.AppSettings
import com.fisma.trinity.util.DatabaseHelper
import com.fisma.trinity.viewutil.WorkspaceGestureListener

abstract class Settings {

  abstract val appContext: Context

  abstract val appSettings: AppSettings

  abstract val workspaceGestureCallback: WorkspaceGestureListener.WorkspaceGestureCallback

  abstract val dataManager: DatabaseHelper

  abstract val appLoader: AppManager

  abstract val eventHandler: EventHandler

  abstract val logger: Logger

  interface EventHandler {
    fun showLauncherSettings(context: Context)

    fun showPickAction(context: Context, listener: DialogListener.OnActionDialogListener)

    fun showEditDialog(context: Context, item: Item, listener: DialogListener.OnEditDialogListener)

    fun showDeletePackageDialog(context: Context, item: Item)
  }

  interface Logger {
    fun log(source: Any, priority: Int, tag: String, msg: String, vararg args: Any)
  }

  companion object {
    private var _setup: Settings? = null

    fun wasInitialised(): Boolean {
      return _setup != null
    }

    fun init(setup: Settings) {
      _setup = setup
    }

    fun get(): Settings? {
      if (_setup == null) {
        throw RuntimeException("Settings has not been initialised!")
      }
      return _setup
    }

    fun appContext(): Context {
      return get()!!.appContext
    }

    fun appSettings(): AppSettings {
      return get()!!.appSettings
    }

    fun desktopGestureCallback(): WorkspaceGestureListener.WorkspaceGestureCallback {
      return get()!!.workspaceGestureCallback
    }

    fun dataManager(): DatabaseHelper {
      return get()!!.dataManager
    }

    fun appLoader(): AppManager {
      return get()!!.appLoader
    }

    fun eventHandler(): EventHandler {
      return get()!!.eventHandler
    }

    fun logger(): Logger {
      return get()!!.logger
    }
  }
}

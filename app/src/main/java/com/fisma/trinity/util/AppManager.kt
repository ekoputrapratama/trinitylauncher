package com.fisma.trinity.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.interfaces.AppDeleteListener
import com.fisma.trinity.interfaces.AppUpdateListener
import com.fisma.trinity.model.App
import com.fisma.trinity.model.Item
import java.text.Collator
import java.util.*


class AppManager(context: Context) {

  val _packageManager: PackageManager
  private val _apps = ArrayList<App>()
  private val _nonFilteredApps = ArrayList<App>()
  val _updateListeners: MutableList<AppUpdateListener> = ArrayList()
  val _deleteListeners: MutableList<AppDeleteListener> = ArrayList()
  var _recreateAfterGettingApps: Boolean = false
  private var _task: AsyncTask<*, *, *>? = null
  var _context: Context = context

  val apps: List<App>
    get() = _apps

  val nonFilteredApps: List<App>
    get() = _nonFilteredApps

  init {
    _packageManager = context.packageManager
  }


  fun findApp(intent: Intent?): App? {
    if (intent == null || intent.component == null) return null

    val packageName = intent.component!!.packageName
    val className = intent.component!!.className
    for (app in _apps) {
      if (app.className == className && app.packageName == packageName) {
        return app
      }
    }
    return null
  }

  fun init() {
    getAllApps()
  }

  fun getAllApps() {
    if (_task == null || _task!!.status == AsyncTask.Status.FINISHED)
      _task = AsyncGetApps().execute()
    else if (_task!!.status == AsyncTask.Status.RUNNING) {
      _task!!.cancel(false)
      _task = AsyncGetApps().execute()
    }
  }

  fun getAllApps(context: Context, includeHidden: Boolean): List<App> {
    return if (includeHidden) nonFilteredApps else apps
  }

  fun findItemApp(item: Item): App? {
    return findApp(item.intent)
  }

  fun createApp(intent: Intent): App? {
    try {
      val info = _packageManager.resolveActivity(intent, 0)
      return App(_packageManager, info)
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }

  }

  fun onAppUpdated(context: Context, intent: Intent) {
    getAllApps()
  }

  fun addUpdateListener(updateListener: AppUpdateListener) {
    _updateListeners.add(updateListener)
  }

  fun addDeleteListener(deleteListener: AppDeleteListener) {
    _deleteListeners.add(deleteListener)
  }

  fun notifyUpdateListeners(apps: List<App>) {
    val iter = _updateListeners.iterator()
    while (iter.hasNext()) {
      if (iter.next().onAppUpdated(apps)) {
        iter.remove()
      }
    }
  }

  fun notifyRemoveListeners(apps: List<App>) {
    val iter = _deleteListeners.iterator()
    while (iter.hasNext()) {
      if (iter.next().onAppDeleted(apps)) {
        iter.remove()
      }
    }
  }

  private inner class AsyncGetApps : AsyncTask<Any, Any, Any>() {
    private var tempApps: List<App>? = null

    override fun onPreExecute() {
      tempApps = ArrayList(_apps)
      super.onPreExecute()
    }

    override fun onCancelled() {
      tempApps = null
      super.onCancelled()
    }

    override fun doInBackground(vararg p0: Any?): Any? {
      _apps.clear()
      _nonFilteredApps.clear()

      // work profile support
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val launcherApps = _context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val profiles = launcherApps.profiles
        for (userHandle in profiles) {
          // TODO lots of stuff required with the rest of the app to get this working
          //List<LauncherActivityInfo> apps = launcherApps.getActivityList(null, userHandle);
          //for (LauncherActivityInfo info : apps) {
          //    _nonFilteredApps.add(new App(_packageManager, info.getApplicationInfo()));
          //}
        }
      }

      val intent = Intent(Intent.ACTION_MAIN, null)
      intent.addCategory(Intent.CATEGORY_LAUNCHER)
      val activitiesInfo = _packageManager.queryIntentActivities(intent, 0)
      for (info in activitiesInfo) {
        val app = App(_packageManager, info)
        //if (!_nonFilteredApps.contains(app)) {
        _nonFilteredApps.add(app)
        //}
      }

      // sort the apps by label here
      _nonFilteredApps.sortBy { it.label }

      val hiddenList = AppSettings.get().hiddenAppsList
      if (hiddenList != null) {
        for (i in _nonFilteredApps.indices) {
          var shouldGetAway = false
          for (hidItemRaw in hiddenList) {
            if (_nonFilteredApps[i].packageName + "/" + _nonFilteredApps[i].className == hidItemRaw) {
              shouldGetAway = true
              break
            }
          }
          if (!shouldGetAway) {
            _apps.add(_nonFilteredApps[i])
          }
        }
      } else {
        for (info in activitiesInfo)
          _apps.add(App(_packageManager, info))
      }

      val appSettings = AppSettings.get()
      if (!appSettings.iconPack.isEmpty() && Tool.isPackageInstalled(appSettings.iconPack, _packageManager)) {
        IconPackHelper.applyIconPack(this@AppManager, Tool.dp2px(appSettings.iconSize.toFloat()), appSettings.iconPack, _apps)
      }
      return null
    }

    override fun onPostExecute(result: Any?) {
      notifyUpdateListeners(_apps)

      val removed = getRemovedApps(tempApps!!, _apps)
      if (removed.isNotEmpty()) {
        notifyRemoveListeners(removed)
      }

      if (_recreateAfterGettingApps) {
        _recreateAfterGettingApps = false
        if (_context is HomeActivity)
          (_context as HomeActivity).recreate()
      }

      super.onPostExecute(result)
    }
  }

  companion object {
    @SuppressLint("StaticFieldLeak")
    private var appManager: AppManager? = null

    fun getInstance(context: Context): AppManager? {
      if (appManager == null) {
        appManager = AppManager(context)
      }
      return appManager
    }

    fun getRemovedApps(oldApps: List<App>, newApps: List<App>): List<App> {
      val removed = ArrayList<App>()
      // if this is the first call then return an empty list
      if (oldApps.size == 0) {
        return removed
      }
      for (i in oldApps.indices) {
        if (!newApps.contains(oldApps[i])) {
          removed.add(oldApps[i])
          break
        }
      }
      return removed
    }
  }
}

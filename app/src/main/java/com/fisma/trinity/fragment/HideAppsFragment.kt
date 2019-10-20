package com.fisma.trinity.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.fisma.trinity.R
import com.fisma.trinity.model.App
import com.fisma.trinity.util.AppManager
import com.fisma.trinity.util.AppSettings
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.ArrayList


class HideAppsFragment : Fragment() {

  private val _listActivitiesHidden = ArrayList<String>()
  private val _listActivitiesAll = ArrayList<App>()
  private val _taskList = AsyncWorkerList()
  private var _appInfoAdapter: HideAppsAdapter? = null
  private var _switcherLoad: ViewSwitcher? = null
  private var _grid: ListView? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.request, container, false)
    _switcherLoad = rootView.findViewById(R.id.viewSwitcherLoadingMain)

    val fab = rootView.findViewById<FloatingActionButton>(R.id.fab_rq)
    fab.setOnClickListener { confirmSelection() }

    if (_taskList.status == AsyncTask.Status.PENDING) {
      // task has not started yet
      _taskList.execute()
    }

    if (_taskList.status == AsyncTask.Status.FINISHED) {
      // task is done and onPostExecute has been called
      AsyncWorkerList().execute()
    }

    return rootView
  }

  @SuppressLint("StaticFieldLeak")
  inner class AsyncWorkerList : AsyncTask<Any, Any, Any>() {

    override fun onPreExecute() {
      val hiddenList = AppSettings.get().hiddenAppsList
      _listActivitiesHidden.addAll(hiddenList)

      super.onPreExecute()
    }

    override fun doInBackground(vararg arg0: Any): Any? {
      try {
        // compare to installed apps
        prepareData()
        return null
      } catch (e: Throwable) {
        e.printStackTrace()
      }

      return null
    }

    override fun onPostExecute(result: Any?) {
      populateView()
      // switch from loading screen to the main view
      _switcherLoad!!.showNext()

      super.onPostExecute(result)
    }
  }

  override fun onSaveInstanceState(savedInstanceState: Bundle) {
    if (DEBUG) Log.v(TAG, "onSaveInstanceState")
    super.onSaveInstanceState(savedInstanceState)
  }

  private fun confirmSelection() {
    val actionSend_Thread = object : Thread() {

      override fun run() {
        // update hidden apps
        AppSettings.get().hiddenAppsList = _listActivitiesHidden
        activity!!.finish()
      }
    }

    if (!actionSend_Thread.isAlive) {
      // prevents thread from being executed more than once
      actionSend_Thread.start()
    }
  }

  private fun prepareData() {
    val apps = AppManager.getInstance(context!!)!!.nonFilteredApps
    _listActivitiesAll.addAll(apps)
  }

  private fun populateView() {
    _grid = activity!!.findViewById(R.id.app_grid)

    assert(_grid != null)
    _grid!!.isFastScrollEnabled = true
    _grid!!.isFastScrollAlwaysVisible = false

    _appInfoAdapter = HideAppsAdapter(activity!!, _listActivitiesAll)

    _grid!!.adapter = _appInfoAdapter
    _grid!!.onItemClickListener = AdapterView.OnItemClickListener { AdapterView, view, position, row ->
      val appInfo = AdapterView.getItemAtPosition(position) as App
      val checker = view.findViewById<CheckBox>(R.id.CBappSelect)
      val icon = view.findViewById<ViewSwitcher>(R.id.viewSwitcherChecked)
      val checkedIcon = view.findViewById<ImageView>(R.id.checkedImage)

      checker.toggle()
      if (checker.isChecked) {
        _listActivitiesHidden.add(appInfo.componentName)
        if (DEBUG) Log.v(TAG, "Selected App: " + appInfo.label)

        if (icon.displayedChild == 0) {
          icon.showNext()
        }
      } else {
        _listActivitiesHidden.remove(appInfo.componentName)
        if (DEBUG) Log.v(TAG, "Deselected App: " + appInfo.label)
        if (icon.displayedChild == 1) {
          icon.showPrevious()
        }
      }
    }
  }

  fun updateIconForTheme(imageView: ImageView) {
    val theme = context!!.theme
    val attrs = IntArray(1) { index -> R.attr.iconColor }
    val typedArray = theme.obtainStyledAttributes(attrs)
    val color = typedArray.getColor(0, Color.WHITE)
    DrawableCompat.setTint(imageView.drawable, color)
  }

  private inner class HideAppsAdapter constructor(context: Context, adapterArrayList: ArrayList<App>) : ArrayAdapter<App>(context, R.layout.item_hide_apps, adapterArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      var view = convertView
      val holder: ViewHolder
      if (convertView == null) {
        view = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_hide_apps, parent, false)
        holder = ViewHolder()
        holder._apkIcon = view!!.findViewById(R.id.IVappIcon)
        holder._apkName = view.findViewById(R.id.TVappName)
        holder._apkPackage = view.findViewById(R.id.TVappPackage)
        holder._checker = view.findViewById(R.id.CBappSelect)
        holder._switcherChecked = view.findViewById(R.id.viewSwitcherChecked)
        holder.checkedImage = view.findViewById(R.id.checkedImage)
        view.tag = holder
      } else {
        holder = view!!.tag as ViewHolder
      }
      updateIconForTheme(holder.checkedImage!!)
      val appInfo = getItem(position)

      holder._apkPackage!!.text = appInfo!!.className
      holder._apkName!!.text = appInfo.label
      holder._apkIcon!!.setImageDrawable(appInfo.icon)

      holder._switcherChecked!!.inAnimation = null
      holder._switcherChecked!!.outAnimation = null
      holder._checker!!.isChecked = _listActivitiesHidden.contains(appInfo.componentName)
      if (_listActivitiesHidden.contains(appInfo.componentName)) {
        if (holder._switcherChecked!!.displayedChild == 0) {
          holder._switcherChecked!!.showNext()
        }
      } else {
        if (holder._switcherChecked!!.displayedChild == 1) {
          holder._switcherChecked!!.showPrevious()
        }
      }
      return view
    }
  }

  private inner class ViewHolder {
    internal var _apkName: TextView? = null
    internal var _apkPackage: TextView? = null
    internal var _apkIcon: ImageView? = null
    internal var checkedImage: ImageView? = null
    internal var _checker: CheckBox? = null
    internal var _switcherChecked: ViewSwitcher? = null
  }

  companion object {
    private val TAG = "RequestActivity"
    private val DEBUG = true
  }
}

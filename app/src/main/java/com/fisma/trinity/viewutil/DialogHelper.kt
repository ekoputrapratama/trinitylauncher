package com.fisma.trinity.viewutil

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.Gravity
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItems
import com.fisma.trinity.R
import com.fisma.trinity.activity.HomeActivity
import com.fisma.trinity.model.App
import com.fisma.trinity.model.Item
import com.fisma.trinity.util.AppManager
import com.fisma.trinity.util.AppSettings
import com.fisma.trinity.util.Tool
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import java.util.*


object DialogHelper {
  fun editItemDialog(title: String, defaultText: String, c: Context, listener: OnItemEditListener) {
    MaterialDialog(c).show {
      title(text = title)
      positiveButton(android.R.string.ok)
      negativeButton(android.R.string.cancel)
      input(prefill = defaultText) { _, input ->
        listener.itemLabel(input.toString())
      }
    }
  }

  fun alertDialog(context: Context, title: String, msg: String, onPositive: DialogCallback) {
    MaterialDialog(context).show {
      title(text = title)
      message(text = msg)
      positiveButton(res = android.R.string.ok, click = onPositive)
      negativeButton(android.R.string.cancel)
    }
  }

  fun alertDialog(context: Context, title: String, message: String, positive: String, onPositive: DialogCallback) {

    MaterialDialog(context).show {
      title(text = title)
      message(text = message)
      positiveButton(text = positive, click = onPositive)
      negativeButton(android.R.string.cancel)
    }
  }

  fun selectActionDialog(context: Context, callback: ItemListener) {
    MaterialDialog(context).show {
      title(text = "Action")
      listItems(res = R.array.entries__gesture_action, selection = callback)
    }
  }

  fun selectDesktopActionDialog(context: Context, callback: ItemListener) {
    MaterialDialog(context).show {
      title(text = "Action")
      listItems(res = R.array.entries__desktop_actions, selection = callback)
    }
  }

  fun selectGestureDialog(context: Context, title: String, callback: ItemListener) {
    MaterialDialog(context).show {
      title(text = title)
      listItems(res = R.array.entries__gesture, selection = callback)
    }
  }

  fun selectAppDialog(context: Context, onAppSelectedListener: OnAppSelectedListener?) {
    val fastItemAdapter = FastItemAdapter<IconLabelItem>()

    var dialog: MaterialDialog? = null
    val items = ArrayList<IconLabelItem>()
    val apps = AppManager.getInstance(context)!!.apps
    for (i in apps.indices) {
      items.add(IconLabelItem(apps[i].icon, apps[i].label)
        .withIconSize(context, 50)
        .withIsAppLauncher(true)
        .withIconGravity(Gravity.START)
        .withIconPadding(context, 8))
    }
    fastItemAdapter.set(items)
    fastItemAdapter.withOnClickListener { v, adapter, item, position ->
      onAppSelectedListener?.onAppSelected(apps.get(position))
      dialog!!.dismiss()
      true
    }
    dialog = MaterialDialog(context).show {
      title(R.string.select_app)
      customListAdapter(fastItemAdapter, LinearLayoutManager(context, RecyclerView.VERTICAL, false))
      negativeButton(android.R.string.cancel)
    }
  }

  fun startPickIconPackIntent(context: Context) {
    val packageManager = context.packageManager
    val activity = context as Activity
    val appManager = AppManager.getInstance(context)
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory("com.anddoes.launcher.THEME")

    val fastItemAdapter = FastItemAdapter<IconLabelItem>()

    val resolveInfos = packageManager.queryIntentActivities(intent, 0)
    Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(packageManager))
    var dialog: MaterialDialog? = null

    fastItemAdapter.add(IconLabelItem(activity, R.mipmap.ic_launcher, R.string.default_icons)
      .withIconPadding(context, 16)
      .withIconGravity(Gravity.START)
      .withIconSize(context, 50)
      .withOnClickListener(View.OnClickListener {
        appManager!!._recreateAfterGettingApps = true
        AppSettings.get().iconPack = ""
        appManager.getAllApps()
        dialog!!.dismiss()
      }))
    for (i in resolveInfos.indices) {
      fastItemAdapter.add(IconLabelItem(resolveInfos[i].loadIcon(packageManager), resolveInfos[i].loadLabel(packageManager).toString())
        .withIconPadding(context, 16)
        .withIconSize(context, 50)
        .withIsAppLauncher(true)
        .withIconGravity(Gravity.START)
        .withOnClickListener(View.OnClickListener {
          if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            appManager!!._recreateAfterGettingApps = true
            AppSettings.get().iconPack = resolveInfos[i].activityInfo.packageName
            appManager.getAllApps()
            dialog!!.dismiss()
          } else {
            Tool.toast(context, activity.getString(R.string.toast_icon_pack_error))
            ActivityCompat.requestPermissions(HomeActivity.launcher, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), HomeActivity.REQUEST_PERMISSION_STORAGE)
          }
        }))
    }
    dialog = MaterialDialog(context).show {
      title(text = "Select Icon Pack")
      customListAdapter(fastItemAdapter, null)
    }
  }

  fun deletePackageDialog(context: Context, item: Item) {
    if (item.type == Item.Type.APP) {
      try {
        val packageURI = Uri.parse("package:" + item.intent.component!!.packageName)
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
        context.startActivity(uninstallIntent)
      } catch (e: Exception) {
        e.printStackTrace()
      }

    }
  }

  interface OnAppSelectedListener {
    fun onAppSelected(app: App)
  }

  interface OnItemEditListener {
    fun itemLabel(label: String)
  }
}

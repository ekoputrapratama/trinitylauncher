package com.fisma.trinity.util

import android.content.Context
import com.fisma.trinity.Constants
import com.fisma.trinity.interfaces.DialogListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import com.fisma.trinity.viewutil.DialogHelper


class HomeEventHandler : Settings.EventHandler {
  override fun showLauncherSettings(context: Context) {
    LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context)
  }

  override fun showPickAction(context: Context, listener: DialogListener.OnActionDialogListener) {
    DialogHelper.selectDesktopActionDialog(context) { _, position, _ ->
      if (position == 0) {
        listener.onAdd(Constants.ACTION_LAUNCHER)
      }
    }
  }

  override fun showEditDialog(context: Context, item: Item, listener: DialogListener.OnEditDialogListener) {
    DialogHelper.editItemDialog("Edit Item", item.label, context, object : DialogHelper.OnItemEditListener {
      override fun itemLabel(label: String) {
        listener.onRename(label)
      }
    })
  }

  override fun showDeletePackageDialog(context: Context, item: Item) {
    DialogHelper.deletePackageDialog(context, item)
  }
}

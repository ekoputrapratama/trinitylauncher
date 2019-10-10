package com.fisma.trinity.model

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.os.Bundle
import android.os.Parcelable

class PendingAddWidgetInfo {
  var minWidth: Int = 0
  var minHeight: Int = 0
  var minResizeWidth: Int = 0
  var minResizeHeight: Int = 0
  var previewImage: Int = 0
  var icon: Int = 0
  var info: AppWidgetProviderInfo
  var boundWidget: AppWidgetHostView? = null
  var bindOptions: Bundle? = null

  // Any configuration data that we want to pass to a configuration activity when
  // starting up a widget
  var mimeType: String? = null
  var configurationData: Parcelable? = null
  var componentName: ComponentName? = null
  /**
   * One of [LauncherSettings.Favorites.ITEM_TYPE_APPLICATION],
   * [LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT],
   * [LauncherSettings.Favorites.ITEM_TYPE_FOLDER], or
   * [LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET].
   */
  var itemType: Item.Type?
  /**
   * Indicates the X cell span.
   */
  var spanX = 1

  /**
   * Indicates the Y cell span.
   */
  var spanY = 1

  /**
   * Indicates the minimum X cell span.
   */
  var minSpanX = 1

  /**
   * Indicates the minimum Y cell span.
   */
  var minSpanY = 1

  constructor(i: AppWidgetProviderInfo, dataMimeType: String?, data: Parcelable?) {
    itemType = Item.Type.APPWIDGET
    this.info = i
    componentName = i.provider
    minWidth = i.minWidth
    minHeight = i.minHeight
    minResizeWidth = i.minResizeWidth
    minResizeHeight = i.minResizeHeight
    previewImage = i.previewImage
    icon = i.icon
    if (dataMimeType != null && data != null) {
      mimeType = dataMimeType
      configurationData = data
    }
  }

  // Copy constructor
  constructor(copy: PendingAddWidgetInfo) {
    minWidth = copy.minWidth
    minHeight = copy.minHeight
    minResizeWidth = copy.minResizeWidth
    minResizeHeight = copy.minResizeHeight
    previewImage = copy.previewImage
    icon = copy.icon
    info = copy.info
    boundWidget = copy.boundWidget
    mimeType = copy.mimeType
    configurationData = copy.configurationData
    componentName = copy.componentName
    itemType = copy.itemType
    spanX = copy.spanX
    spanY = copy.spanY
    minSpanX = copy.minSpanX
    minSpanY = copy.minSpanY
    bindOptions = if (copy.bindOptions == null) null else copy.bindOptions!!.clone() as Bundle
  }

  override fun toString(): String {
    return "Widget: " + componentName!!.toShortString()
  }
}
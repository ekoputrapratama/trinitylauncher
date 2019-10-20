package com.fisma.trinity.model

import android.content.Intent
import android.graphics.drawable.Drawable
import com.fisma.trinity.Constants
import com.fisma.trinity.util.IntentUtil
import java.util.*
import kotlin.collections.ArrayList


open class Item {
  // all items need these values
  var icon: Drawable? = null
  var label: String
  var type: Type? = null
  var _id: Int = 0
  var _location: Constants.ItemPosition? = null
  var x = 0
  var y = 0

  // intent for shortcuts and apps
  lateinit var intent: Intent

  // list of items for groups
  lateinit var groupItems: ArrayList<Item>

  // int value for launcher action
  var actionValue: Int = 0

  // widget specific values
  var widgetValue: Int = 0
  var spanX = 1
  var spanY = 1

  val id: Int?
    get() = _id

  init {
    val random = Random()
    _id = random.nextInt()
    label = ""
  }

  override fun equals(`object`: Any?): Boolean {
    if (`object` is Item) {
      val item = `object` as Item?
      return _id == item!!._id
    } else {
      return false
    }
  }

  fun reset() {
    val random = Random()
    _id = random.nextInt()
  }

  fun setId(id: Int) {
    _id = id
  }

  enum class Type {
    APP,
    SHORTCUT,
    GROUP,
    ACTION,
    WIDGET,
    APPWIDGET
  }

  fun getItems(): ArrayList<Item> {
    return groupItems
  }

  fun setItems(items: ArrayList<Item>) {
    groupItems = items
  }

  companion object {

    fun newAppItem(app: App): Item {
      val item = Item()
      item.type = Type.APP
      item.label = app.label
      item.icon = app.icon
      item.intent = IntentUtil.getIntentFromApp(app)
      return item
    }

    fun newShortcutItem(intent: Intent, icon: Drawable, name: String): Item {
      val item = Item()
      item.type = Type.SHORTCUT
      item.label = name
      item.icon = icon
      item.spanX = 1
      item.spanY = 1
      item.intent = intent
      return item
    }

    fun newGroupItem(): Item {
      val item = Item()
      item.type = Type.GROUP
      item.label = ""
      item.spanX = 1
      item.spanY = 1
      item.groupItems = ArrayList()
      return item
    }

    fun newActionItem(action: Int): Item {
      val item = Item()
      item.type = Type.ACTION
      item.spanX = 1
      item.spanY = 1
      item.actionValue = action
      return item
    }

    fun newWidgetItem(widgetValue: Int): Item {
      val item = Item()
      item.type = Type.WIDGET
      item.widgetValue = widgetValue
      item.spanX = 1
      item.spanY = 1
      return item
    }
  }
}

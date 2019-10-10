package com.fisma.trinity.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.fisma.trinity.Constants
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.Item
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.model.ShortcutItem
import java.io.ByteArrayOutputStream


class DatabaseHelper(protected var _context: Context) : SQLiteOpenHelper(_context, DATABASE_NAME, null, 1) {

  private var _db: SQLiteDatabase = writableDatabase

  val desktop: List<List<Item>>
    get() {
      val SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME
      val cursor = _db.rawQuery(SQL_QUERY_DESKTOP, null)
      val desktop = ArrayList<ArrayList<Item>>()
      if (cursor.moveToFirst()) {
        val pageColumnIndex = cursor.getColumnIndex(COLUMN_PAGE)
        val desktopColumnIndex = cursor.getColumnIndex(COLUMN_DESKTOP)
        val stateColumnIndex = cursor.getColumnIndex(COLUMN_STATE)
        do {
          val page = Integer.parseInt(cursor.getString(pageColumnIndex))
          val desktopVar = Integer.parseInt(cursor.getString(desktopColumnIndex))
          val stateVar = Integer.parseInt(cursor.getString(stateColumnIndex))
          while (page >= desktop.size) {
            desktop.add(ArrayList())
          }
          if (desktopVar == Constants.ItemPosition.Desktop.ordinal && stateVar == Constants.ItemState.Visible.ordinal) {
            desktop[page].add(getSelection(cursor))
          }
        } while (cursor.moveToNext())
      }
      cursor.close()
      return desktop
    }

  val dock: ArrayList<Item>
    get() {

      val SQL_QUERY_DESKTOP = SQL_QUERY + TABLE_HOME
      val cursor = _db.rawQuery(SQL_QUERY_DESKTOP, null)
      val dock = ArrayList<Item>()
      if (cursor.moveToFirst()) {
        val desktopColumnIndex = cursor.getColumnIndex(COLUMN_DESKTOP)
        val stateColumnIndex = cursor.getColumnIndex(COLUMN_STATE)
        do {
          val desktopVar = Integer.parseInt(cursor.getString(desktopColumnIndex))
          val stateVar = Integer.parseInt(cursor.getString(stateColumnIndex))
          if (desktopVar == Constants.ItemPosition.Dock.ordinal && stateVar == Constants.ItemState.Visible.ordinal) {
            dock.add(getSelection(cursor))
          }
        } while (cursor.moveToNext())
      }
      cursor.close()
      return dock
    }

  val appWidgets: ArrayList<AppWidget>
    get() {
      val SQL_QUERY_APPWIDGET = SQL_QUERY + TABLE_APPWIDGET
      val cursor = _db.rawQuery(SQL_QUERY_APPWIDGET, null)
      val widgets = ArrayList<AppWidget>()
      if (cursor.moveToFirst()) {
        do {
          widgets.add(getWidgetSelection(cursor))
        } while (cursor.moveToNext())
      }
      cursor.close()
      widgets.sortBy { it.label }
      return widgets
    }

  val shortcuts: ArrayList<ShortcutItem>
    get() {
      val SQL_QUERY_SHORTCUTS = SQL_QUERY + TABLE_SHORTCUTS
      val cursor = _db.rawQuery(SQL_QUERY_SHORTCUTS, null)
      val shortcuts = ArrayList<ShortcutItem>()
      if (cursor.moveToFirst()) {
        do {
          shortcuts.add(getShortcutSelection(cursor))
        } while (cursor.moveToNext())
      }
      cursor.close()
      shortcuts.sortBy { it.index }
      return shortcuts
    }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SQL_CREATE_HOME)
    db.execSQL(SQL_CREATE_APPWIDGET)
    db.execSQL(SQL_CREATE_SHORTCUTS)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // discard the data and start over
    db.execSQL(SQL_DELETE + TABLE_HOME)
    db.execSQL(SQL_DELETE + TABLE_APPWIDGET)
    db.execSQL(SQL_DELETE + TABLE_SHORTCUTS)
    onCreate(db)
  }

  override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    onUpgrade(db, oldVersion, newVersion)
  }

  fun createItem(item: Item?, page: Int, itemPosition: Constants.ItemPosition) {
    val itemValues = ContentValues()
    itemValues.put(COLUMN_TIME, item!!.id)
    itemValues.put(COLUMN_TYPE, item.type.toString())
    itemValues.put(COLUMN_LABEL, item.label)
    itemValues.put(COLUMN_X_POS, item.x)
    itemValues.put(COLUMN_Y_POS, item.y)

    Settings.logger().log(this, Log.INFO, TAG, "createItem: %s (ID: %d)", if (item != null) item.label else "NULL", if (item != null) item.id as Any else -1)

    var concat = ""
    when (item.type) {
      Item.Type.APP, Item.Type.SHORTCUT -> {
        ImageUtil.saveIcon(_context, ImageUtil.drawableToBitmap(item.icon)!!, Integer.toString(item.id!!))
        itemValues.put(COLUMN_DATA, IntentUtil.getIntentAsString(item.intent))
      }
      Item.Type.GROUP -> {
        for (tmp in item.getItems()) {
          if (tmp != null) {
            concat += tmp.id!!.toString() + Constants.DELIMITER
          }
        }
        itemValues.put(COLUMN_DATA, concat)
      }
      Item.Type.ACTION -> itemValues.put(COLUMN_DATA, item.actionValue)
      Item.Type.APPWIDGET -> {
        concat = (Integer.toString(item.widgetValue) + Constants.DELIMITER
          + Integer.toString(item.spanX) + Constants.DELIMITER
          + Integer.toString(item.spanY))
        itemValues.put(COLUMN_DATA, concat)
      }
      else -> {
      }
    }
    itemValues.put(COLUMN_PAGE, page)
    itemValues.put(COLUMN_DESKTOP, itemPosition.ordinal)

    // item will always be visible when first added
    itemValues.put(COLUMN_STATE, 1)
    _db.insert(TABLE_HOME, null, itemValues)
  }

  fun saveItem(item: Item) {
    updateItem(item)
  }

  fun saveItem(item: Item, state: Constants.ItemState) {
    updateItem(item, state)
  }

  fun saveItem(item: Item, page: Int, itemPosition: Constants.ItemPosition) {
    val SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_HOME + " WHERE " + COLUMN_TIME + " = " + item.id
    val cursor = _db.rawQuery(SQL_QUERY_SPECIFIC, null)
    if (cursor.count == 0) {
      createItem(item, page, itemPosition)
    } else if (cursor.count == 1) {
      updateItem(item, page, itemPosition)
    }
  }

  fun deleteItem(item: Item?, deleteSubItems: Boolean) {
    // if the item is a group then remove all entries
    if (deleteSubItems && item!!.type == Item.Type.GROUP) {
      for (i in item.groupItems) {
        deleteItem(i, deleteSubItems)
      }
    }
    // delete the item itself
    _db.delete(TABLE_HOME, "$COLUMN_TIME = ?", arrayOf<String>(item!!.id.toString()))
  }

  fun getItem(id: Int): Item? {
    val SQL_QUERY_SPECIFIC = "$SQL_QUERY$TABLE_HOME WHERE $COLUMN_TIME = $id"
    val cursor = _db.rawQuery(SQL_QUERY_SPECIFIC, null)
    var item: Item? = null
    if (cursor.moveToFirst()) {
      item = getSelection(cursor)
    }
    cursor.close()
    return item
  }

  // update data attribute for an item
  fun updateItem(item: Item?) {
    Settings.logger().log(this, Log.INFO, TAG, "updateItem: %s %d", if (item != null) item.label else "NULL", if (item != null) item.id as Any else -1)
    val itemValues = ContentValues()
    itemValues.put(COLUMN_LABEL, item!!.label)
    itemValues.put(COLUMN_X_POS, item.x)
    itemValues.put(COLUMN_Y_POS, item.y)

    var concat = ""
    when (item.type) {
      Item.Type.APP, Item.Type.SHORTCUT -> {
        ImageUtil.saveIcon(_context, ImageUtil.drawableToBitmap(item.icon)!!, Integer.toString(item.id!!))
        itemValues.put(COLUMN_DATA, IntentUtil.getIntentAsString(item.intent))
      }
      Item.Type.GROUP -> {
        for (tmp in item.getItems()) {
          concat += tmp.id!!.toString() + Constants.DELIMITER
        }
        itemValues.put(COLUMN_DATA, concat)
      }
      Item.Type.ACTION -> itemValues.put(COLUMN_DATA, item.actionValue)
      Item.Type.APPWIDGET -> {
        concat = (Integer.toString(item.widgetValue) + Constants.DELIMITER
          + Integer.toString(item.spanX) + Constants.DELIMITER
          + Integer.toString(item.spanY))
        itemValues.put(COLUMN_DATA, concat)
      }
    }
    _db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item.id, null)
  }

  // update the state of an item
  fun updateItem(item: Item?, state: Constants.ItemState) {
    val itemValues = ContentValues()
    Settings.logger().log(this, Log.INFO, TAG, "updateItem: %s %d", if (item != null) item.label else "NULL", if (item != null) item.id as Any else -1)
    itemValues.put(COLUMN_STATE, state.ordinal)
    _db.update(TABLE_HOME, itemValues, COLUMN_TIME + " = " + item!!.id, null)
  }

  // update the fields only used by the database
  fun updateItem(item: Item?, page: Int, itemPosition: Constants.ItemPosition) {
    Settings.logger().log(this, Log.INFO, TAG, "updateItem: %s %d", if (item != null) item.label else "NULL", if (item != null) item.id as Any else -1)
    deleteItem(item, false)
    createItem(item, page, itemPosition)
  }

  private fun getSelection(cursor: Cursor): Item {
    val item = Item()
    val id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)))
    val type = Item.Type.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)))
    val label = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL))
    val x = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_X_POS)))
    val y = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_Y_POS)))
    val data = cursor.getString(cursor.getColumnIndex(COLUMN_DATA))

    item.setId(id)
    item.label = label
    item.x = x
    item.y = y
    item.type = type

    val dataSplit: Array<String>
    when (type) {
      Item.Type.APP, Item.Type.SHORTCUT -> {
        item.intent = IntentUtil.getIntentFromString(data)!!
        val app = Settings.appLoader().findItemApp(item)
        item.icon = app?.icon
      }
      Item.Type.GROUP -> {
        item.setItems(ArrayList())
        dataSplit = data.split(Constants.DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (string in dataSplit) {
          if (string.isEmpty()) continue
          val groupItem = getItem(Integer.parseInt(string))
          if (groupItem != null) {
            item.getItems().add(groupItem)
          }
        }
      }
      Item.Type.ACTION -> item.actionValue = Integer.parseInt(data)
      Item.Type.WIDGET -> {
        dataSplit = data.split(Constants.DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        item.widgetValue = Integer.parseInt(dataSplit[0])
        item.spanX = Integer.parseInt(dataSplit[1])
        item.spanY = Integer.parseInt(dataSplit[2])
      }
      Item.Type.APPWIDGET -> {
        dataSplit = data.split(Constants.DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        item.widgetValue = Integer.parseInt(dataSplit[0])
        item.spanX = Integer.parseInt(dataSplit[1])
        item.spanY = Integer.parseInt(dataSplit[2])
      }
    }
    return item
  }

  fun addWidgetItem(item: AppWidget) {
    if (item.type != Item.Type.APPWIDGET) return

    val itemValues = ContentValues()
    itemValues.put(COLUMN_TIME, item.id)
    itemValues.put(COLUMN_LABEL, item.label)
    itemValues.put(COLUMN_SPAN_X, item.spanX)
    itemValues.put(COLUMN_SPAN_Y, item.spanY)
    itemValues.put(COLUMN_MIN_SPAN_X, item.minSpanX)
    itemValues.put(COLUMN_MIN_SPAN_Y, item.minSpanY)
    itemValues.put(COLUMN_MIN_WIDTH, item.minWidth)
    itemValues.put(COLUMN_MIN_HEIGHT, item.minHeight)

    if (item.previewImage != null) {
      val stream = ByteArrayOutputStream()
      item.previewImage!!.compress(CompressFormat.PNG, 0, stream)
      itemValues.put(COLUMN_IMAGE, stream.toByteArray())
    }

    Settings.logger().log(this, Log.INFO, TAG, "createItem: %s (ID: %d)", if (item != null) item.label else "NULL", if (item != null) item.id as Any else -1)
    _db.insert(TABLE_APPWIDGET, null, itemValues)
  }

  fun deleteWidgetItem(item: Item) {
    // delete the item itself
    _db.delete(TABLE_APPWIDGET, "$COLUMN_TIME = ?", arrayOf<String>(item.id.toString()))
  }

  fun updateWidgetItem(item: AppWidget) {
    deleteWidgetItem(item)
    addWidgetItem(item)
  }

  private fun getWidgetSelection(cursor: Cursor): AppWidget {
    val item = AppWidget()
    val id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)))
    val label = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL))
    val spanX = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_SPAN_X)))
    val spanY = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_SPAN_Y)))
    val minSpanX = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_MIN_SPAN_X)))
    val minSpanY = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_MIN_SPAN_Y)))
    val minHeight = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_MIN_HEIGHT)))
    val minWidth = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_MIN_WIDTH)))
    val imgBlob = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE))

    item.setId(id)
    item.label = label
    item.spanX = spanX
    item.spanY = spanY
    item.type = Item.Type.APPWIDGET
    item.previewImage = BitmapFactory.decodeByteArray(imgBlob, 0, imgBlob.size)
    item.minWidth = minWidth
    item.minHeight = minHeight
    item.minSpanX = minSpanX
    item.minSpanY = minSpanY

    return item
  }

  fun createShortcut(shortcut: ShortcutItem) {
    val itemValues = ContentValues()
    itemValues.put(COLUMN_TIME, shortcut.id)
    itemValues.put(COLUMN_TYPE, shortcut.type.toString())
    itemValues.put(COLUMN_LABEL, shortcut.label)
    itemValues.put(COLUMN_INDEX, shortcut.index)

    if (shortcut.icon != null) {
      val stream = ByteArrayOutputStream()
      shortcut.icon!!.compress(CompressFormat.PNG, 0, stream)
      itemValues.put(COLUMN_ICON, stream.toByteArray())
    }

    when (shortcut.type) {
      ShortcutItem.Type.ACTION -> {
        itemValues.put(COLUMN_ACTION, shortcut.action.toString())
      }
      ShortcutItem.Type.APP -> {
        itemValues.put(COLUMN_PACKAGE_NAME, shortcut.packageName)
        itemValues.put(COLUMN_CLASS_NAME, shortcut.className)
      }
    }

    _db.insert(TABLE_SHORTCUTS, null, itemValues)
  }

  fun updateShortcut(shortcut: ShortcutItem) {
    val itemValues = ContentValues()
    itemValues.put(COLUMN_LABEL, shortcut.label)
    itemValues.put(COLUMN_TYPE, shortcut.type.toString())
    itemValues.put(COLUMN_LABEL, shortcut.label)
    itemValues.put(COLUMN_INDEX, shortcut.index)

    if (shortcut.icon != null) {
      val stream = ByteArrayOutputStream()
      shortcut.icon!!.compress(CompressFormat.PNG, 0, stream)
      itemValues.put(COLUMN_ICON, stream.toByteArray())
    }

    when (shortcut.type) {
      ShortcutItem.Type.ACTION -> {
        itemValues.put(COLUMN_ACTION, shortcut.action.toString())
      }
      ShortcutItem.Type.APP -> {
        itemValues.put(COLUMN_PACKAGE_NAME, shortcut.packageName)
        itemValues.put(COLUMN_CLASS_NAME, shortcut.className)
      }
    }

    _db.update(TABLE_SHORTCUTS, itemValues, COLUMN_TIME + " = " + shortcut.id, null)
  }

  fun deleteShortcut(shortcut: ShortcutItem) {
    _db.delete(TABLE_SHORTCUTS, "$COLUMN_TIME = ?", arrayOf<String>(shortcut.id.toString()))
  }

  fun saveShortcut(item: ShortcutItem) {
    val SQL_QUERY_SPECIFIC = SQL_QUERY + TABLE_SHORTCUTS + " WHERE " + COLUMN_TIME + " = " + item.id
    val cursor = _db.rawQuery(SQL_QUERY_SPECIFIC, null)
    if (cursor.count == 0) {
      createShortcut(item)
    } else if (cursor.count == 1) {
      updateShortcut(item)
    }
    if (!cursor.isClosed)
      cursor.close()
  }

  fun getShortcutSelection(cursor: Cursor): ShortcutItem {
    val id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)))
    val label = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL))
    val imgBlob = cursor.getBlob(cursor.getColumnIndex(COLUMN_ICON))
    val type = ShortcutItem.Type.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)))
    val index = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_INDEX)))

    val builder = ShortcutItem.Builder()
      .setId(id)
      .setIndex(index)
      .setLabel(label)
      .setType(type)


    if (imgBlob != null) {
      builder.setIcon(BitmapFactory.decodeByteArray(imgBlob, 0, imgBlob.size))
    }
    when (type) {
      ShortcutItem.Type.ACTION -> {
        val actionStr = cursor.getString(cursor.getColumnIndex(COLUMN_ACTION))
        val action = LauncherAction.Action.valueOf(actionStr)
        builder.setAction(action)
      }
      ShortcutItem.Type.APP -> {
        val packageName = cursor.getString(cursor.getColumnIndex(COLUMN_PACKAGE_NAME))
        val className = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME))
        builder.setClassName(className)
        builder.setPackageName(packageName)
      }
    }

    return builder.build()
  }

  companion object {
    const val TAG = "DatabaseHelper"
    const val DATABASE_NAME = "trinity.db"
    const val TABLE_HOME = "home"
    const val TABLE_APPWIDGET = "appwidget"
    const val TABLE_SHORTCUTS = "shortcuts"
    const val COLUMN_PACKAGE_NAME = "package_name"
    const val COLUMN_CLASS_NAME = "class_name"
    const val COLUMN_ACTION = "action"
    const val COLUMN_INDEX = "shortcut_index"
    const val COLUMN_ICON = "icon"
    const val COLUMN_TIME = "time"
    const val COLUMN_TYPE = "type"
    const val COLUMN_LABEL = "label"
    const val COLUMN_X_POS = "x"
    const val COLUMN_Y_POS = "y"
    const val COLUMN_DATA = "data"
    const val COLUMN_PAGE = "page"
    const val COLUMN_DESKTOP = "workspace"
    const val COLUMN_STATE = "state"
    const val COLUMN_SPAN_X = "span_x"
    const val COLUMN_SPAN_Y = "span_y"
    const val COLUMN_IMAGE = "image"
    const val COLUMN_MIN_WIDTH = "min_width"
    const val COLUMN_MIN_HEIGHT = "min_height"
    const val COLUMN_MIN_SPAN_X = "min_span_x"
    const val COLUMN_MIN_SPAN_Y = "min_span_y"

    const val SQL_CREATE_HOME = "CREATE TABLE " + TABLE_HOME + " (" +
      COLUMN_TIME + " INTEGER PRIMARY KEY," +
      COLUMN_TYPE + " VARCHAR," +
      COLUMN_LABEL + " VARCHAR," +
      COLUMN_X_POS + " INTEGER," +
      COLUMN_Y_POS + " INTEGER," +
      COLUMN_DATA + " VARCHAR," +
      COLUMN_PAGE + " INTEGER," +
      COLUMN_DESKTOP + " INTEGER," +
      COLUMN_STATE + " INTEGER)"
    const val SQL_CREATE_APPWIDGET = "create table $TABLE_APPWIDGET (" +
      "$COLUMN_TIME INTEGER PRIMARY KEY," +
      "$COLUMN_LABEL VARCHAR," +
      "$COLUMN_SPAN_X INTEGER," +
      "$COLUMN_SPAN_Y INTEGER," +
      "$COLUMN_MIN_HEIGHT INTEGER," +
      "$COLUMN_MIN_WIDTH INTEGER," +
      "$COLUMN_MIN_SPAN_Y INTEGER," +
      "$COLUMN_MIN_SPAN_X INTEGER," +
      "$COLUMN_IMAGE BLOB)"
    const val SQL_CREATE_SHORTCUTS = "create table $TABLE_SHORTCUTS (" +
      "$COLUMN_TIME INTEGER PRIMARY KEY," +
      "$COLUMN_INDEX INTEGER," +
      "$COLUMN_LABEL VARCHAR," +
      "$COLUMN_TYPE VARCHAR," +
      "$COLUMN_ACTION VARCHAR," +
      "$COLUMN_CLASS_NAME VARCHAR," +
      "$COLUMN_PACKAGE_NAME VARCHAR," +
      "$COLUMN_ICON BLOB)"

    const val SQL_DELETE = "DROP TABLE IF EXISTS "
    const val SQL_QUERY = "SELECT * FROM "
  }
}

package com.fisma.trinity.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.BuildConfig
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.TrinitySettings
import com.fisma.trinity.compat.AppWidgetManagerCompat
import com.fisma.trinity.interfaces.AppDeleteListener
import com.fisma.trinity.interfaces.AppUpdateListener
import com.fisma.trinity.interfaces.DialogListener
import com.fisma.trinity.interfaces.DropTargetListener
import com.fisma.trinity.manager.Settings
import com.fisma.trinity.model.App
import com.fisma.trinity.model.AppWidget
import com.fisma.trinity.model.Item
import com.fisma.trinity.receivers.AppUpdateReceiver
import com.fisma.trinity.receivers.NetworkStateReceiver
import com.fisma.trinity.receivers.ShortcutReceiver
import com.fisma.trinity.util.*
import com.fisma.trinity.viewutil.DialogHelper
import com.fisma.trinity.viewutil.WidgetHost
import com.fisma.trinity.viewutil.WorkspaceGestureListener
import com.fisma.trinity.widgets.*
import com.hoko.blur.HokoBlur
import com.hoko.blur.task.AsyncBlurTask
import net.gsantner.opoc.util.Callback

class HomeActivity : AppCompatActivity(), Workspace.OnDesktopEditListener, WorkspaceOptionView.DesktopOptionViewListener {
  companion object {
    const val TAG = "HomeActivity"
    const val REQUEST_CREATE_APPWIDGET = 0x6475
    const val REQUEST_PERMISSION_STORAGE = 0x3648

    const val REQUEST_BIND_APPWIDGET = 11
    const val REQUEST_RECONFIGURE_APPWIDGET = 12

    lateinit var _WidgetHost: WidgetHost
    lateinit var mAppWidgetManager: AppWidgetManagerCompat
    var ignoreResume: Boolean = false
    var _itemTouchX: Float = 0.toFloat()
    var _itemTouchY: Float = 0.toFloat()

    // static launcher variables
    var _launcher: HomeActivity? = null
    var launcher: HomeActivity
      get() = _launcher!!
      set(v) {
        _launcher = v
      }

    @SuppressLint("StaticFieldLeak")
    lateinit var _db: DatabaseHelper
    // receiver variables
    private val _appUpdateIntentFilter = IntentFilter()
    private val _shortcutIntentFilter = IntentFilter()
    private val _timeChangedIntentFilter = IntentFilter()
    private val _networkChangedIntentFilter = IntentFilter()

    @SuppressLint("StaticFieldLeak")
    var mWorkspaceGrid: DynamicGrid? = null

    init {
      _timeChangedIntentFilter.addAction("android.intent.action.TIME_TICK")
      _timeChangedIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED")
      _timeChangedIntentFilter.addAction("android.intent.action.TIME_SET")
      _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_ADDED")
      _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED")
      _appUpdateIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED")
      _appUpdateIntentFilter.addDataScheme("package")
      _shortcutIntentFilter.addAction("com.android.launcher.action.INSTALL_SHORTCUT")
      _networkChangedIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
    }
  }

  private var _appUpdateReceiver: AppUpdateReceiver? = null
  private var _shortcutReceiver: ShortcutReceiver? = null
  private var _timeChangedReceiver: BroadcastReceiver? = null
  private var _networkChangedReceiver: NetworkStateReceiver? = null

  private var cx: Int = 0
  private var cy: Int = 0

  var isDropAction = false

  val workspace: Workspace
    get() {
      return contentView.findViewById(R.id.workspace)
    }
  private val contentView: View
    get() = findViewById<ContentView>(R.id.content_view).getContentView()

  val widgetPicker: AppWidgetPicker
    get() = contentView.findViewById(R.id.widgetPicker)

  val dock: Dock
    get() = contentView.findViewById(R.id.dock)

  val appDrawerController: AppDrawerController
    get() = contentView.findViewById(R.id.appDrawerController)

  val groupPopup: GroupPopupView
    get() = contentView.findViewById(R.id.groupPopup)

  val background: FrameLayout
    get() = findViewById(R.id.background_frame)

  private val desktopIndicator: PagerIndicator
    get() = contentView.findViewById(R.id.desktopIndicator)

  val desktopOptionView: WorkspaceOptionView
    get() = contentView.findViewById(R.id.desktop_option)

  val dragLayer: DragLayer
    get() = contentView.findViewById(R.id.item_option)

  private val statusView: View
    get() = findViewById(R.id.status_frame)

  private val navigationView: View
    get() = findViewById(R.id.navigation_frame)

  val dashboardView: DashboardView
    get() = findViewById<ContentView>(R.id.content_view).getDashboardView()

  val mLeftDropTarget: View
    get() = contentView.findViewById<View>(R.id.leftDragHandle)
  val mRightDropTarget: View
    get() = contentView.findViewById<View>(R.id.rightDragHandle)

  val mAppWidgetPicker: AppWidgetPicker
    get() = contentView.findViewById(R.id.widgetPicker)

  val mDropActionRemove: View
    get() = contentView.findViewById(R.id.drop_action_remove)

  val mDropActionInfo: View
    get() = contentView.findViewById(R.id.drop_action_info)

  val mDropActionView: View
    get() = contentView.findViewById(R.id.drop_action)

  lateinit var mWallpaperBitmap: Bitmap
  var mWallpaperBlurredBitmap: Bitmap? = null

  @SuppressLint("InflateParams")
  override fun onCreate(savedInstanceState: Bundle?) {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    _launcher = this
    super.onCreate(savedInstanceState)
    if (!Settings.wasInitialised()) {
      Settings.init(TrinitySettings(this))
    }

    _db = Settings.dataManager()
    val appSettings = AppSettings.get()
    if (appSettings.theme == "0") {
      setTheme(R.style.Home_Light)
    } else if (appSettings.theme == "1") {
      setTheme(R.style.Home_Dark)
    }

    askRequiredPermissions()
    if (hasStoragePermission) {
      val wallpaperManager = WallpaperManager.getInstance(this)
      mWallpaperBitmap = ImageUtil.drawableToBitmap(wallpaperManager.drawable.mutate()) as Bitmap
    }
    setContentView(layoutInflater.inflate(R.layout.activity_home, null))

    // transparent status and navigation
    if (VERSION.SDK_INT >= 21) {
      val window = window
      val decorView = window.decorView
      decorView.systemUiVisibility = 1536
    }
    init()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 0) {
      val wallpaperManager = WallpaperManager.getInstance(this)
      mWallpaperBitmap = ImageUtil.drawableToBitmap(wallpaperManager.drawable.mutate()) as Bitmap
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        updateBackground()
      }
    }
  }

  private fun askRequiredPermissions() {
    Log.d(TAG, "askRequredPermissions")
    if (!hasStoragePermission) {
      // Permission is not granted
      // Should we show an explanation?
      ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
        0)
    }
  }


  private fun updateBackground() {
    if (hasStoragePermission) {

      HokoBlur.with(this)
        .scheme(HokoBlur.SCHEME_NATIVE)
        .mode(HokoBlur.MODE_STACK)
        .radius(75)
        .sampleFactor(2f)
        .forceCopy(true)
        .needUpscale(false)
        .asyncBlur(mWallpaperBitmap, object : AsyncBlurTask.Callback {
          override fun onBlurSuccess(bitmap: Bitmap?) {
            val drawable = BitmapDrawable(resources, bitmap)
            mWallpaperBlurredBitmap = bitmap
            background.background = drawable
            appDrawerController._drawerViewGrid.background = drawable
          }

          override fun onBlurFailed(error: Throwable?) {

          }
        })


    } else {
      background.setBackgroundColor(Color.parseColor("#CD000000"))
    }
  }

  private fun init() {
    mAppWidgetManager = AppWidgetManagerCompat.getInstance(applicationContext)
    _WidgetHost = WidgetHost(applicationContext, R.id.app_widget_host)
    _WidgetHost.startListening()


    if (Settings.appSettings().appFirstLaunch) {
      Settings.appSettings().appFirstLaunch = false
      Settings.appSettings().setAppShowIntro(false)

      initDockItems()

    }

    // item drag and drop
    initDragNDrop(this, mLeftDropTarget, mRightDropTarget, dragLayer)

    registerBroadcastReceiver()
    initAppManager()
    initSettings()
    initVirtualGrid()
    initViews()
  }

  private fun initDockItems() {
    val appDrawerBtnItem = Item.newActionItem(8)
    appDrawerBtnItem.x = 2
    _db.saveItem(appDrawerBtnItem, 0, Constants.ItemPosition.Dock)

    var info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.MESSAGING)
    var item = Item.newAppItem(App(packageManager, info!!))
    item.x = 0
    _db.saveItem(item, 0, Constants.ItemPosition.Dock)

    info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.PHONE)
    item = Item.newAppItem(App(packageManager, info!!))
    item.x = 1
    _db.saveItem(item, 0, Constants.ItemPosition.Dock)

    info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.EMAIL)
    item = Item.newAppItem(App(packageManager, info!!))
    item.x = 3
    _db.saveItem(item, 0, Constants.ItemPosition.Dock)

    info = Tool.getDefaultAppInfo(packageManager, Constants.AppCategory.CONTACTS)
    item = Item.newAppItem(App(packageManager, info!!))
    item.x = 4
    _db.saveItem(item, 0, Constants.ItemPosition.Dock)
  }

  private fun initVirtualGrid() {
    DynamicGrid.statusBarHeight = Tool.dp2px(24f)
    if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      DynamicGrid.navBarHeight = Tool.dp2px(42f)
    }

    val columnCount = Settings.appSettings().desktopColumnCount
    val rowCount = Settings.appSettings().desktopRowCount

    val cell = DynamicGrid.CellProfile(null)
    val rowProfile = DynamicGrid.GridOrientationSpec(rowCount, rowCount)
    val columnProfile = DynamicGrid.GridOrientationSpec(columnCount, columnCount)

    var dockHeight = 144 // default dock height in pixel
    val bottomMargin = 40 + dockHeight // desktop indicator height + dock height
    val margin = DynamicGrid.Margin(0, 0, 0, bottomMargin)

    val params = DynamicGrid.DynamicGridParams()
    params.columnProfile = columnProfile
    params.rowProfile = rowProfile
    params.margin = margin
    params.height = -1
    params.width = -1

    mWorkspaceGrid = DynamicGrid(this, params, cell, "Workspace")
    dock.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->

      if (dock.visibility == View.VISIBLE && dock.alpha == 1f && (bottom - top) != dockHeight) {
        dockHeight = bottom - top
        mWorkspaceGrid?.setMargin(DynamicGrid.Margin(0, 0, 0, 40 + dockHeight))
      }
    }
  }

  private fun initAppManager() {
    Settings.appLoader().addUpdateListener(object : AppUpdateListener {
      override fun onAppUpdated(apps: List<App>): Boolean {
        workspace.initWorkspace()
        dock.initDock()
        return false
      }
    })
    Settings.appLoader().addDeleteListener(object : AppDeleteListener {
      override fun onAppDeleted(apps: List<App>): Boolean {
        workspace.initWorkspace()
        dock.initDock()
        return false
      }
    })
    AppManager.getInstance(this)!!.init()
  }

  private fun initViews() {
    appDrawerController.init()
    widgetPicker.init()
    dock.setHome(this)

    workspace.desktopEditListener = this
    workspace.setPageIndicator(desktopIndicator)
    desktopIndicator.setMode(Settings.appSettings().desktopIndicatorMode)

    val appSettings = Settings.appSettings()
    desktopOptionView.setDesktopOptionViewListener(this)
    desktopOptionView.postDelayed({ desktopOptionView.updateLockIcon(appSettings.desktopLock) }, 100)
    workspace.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

      override fun onPageSelected(position: Int) {
        desktopOptionView.updateHomeIcon(appSettings.desktopPageCurrent == position)
      }

      override fun onPageScrollStateChanged(state: Int) {}
    })
    HomeAppDrawer(this).initAppDrawer(appDrawerController)
    HomeWidgetPicker(this).initWidgetPicker(widgetPicker)

    updateBackground()
  }

  private fun initSettings() {
    updateHomeLayout()

    val appSettings = Settings.appSettings()
    if (appSettings.desktopFullscreen) {
      window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    // set background colors
    workspace.setBackgroundColor(appSettings.desktopBackgroundColor)
    dock.setBackgroundColor(appSettings.dockColor)

    // set frame colors
    statusView.setBackgroundColor(appSettings.desktopInsetColor)
    navigationView.setBackgroundColor(appSettings.desktopInsetColor)
  }

  private fun initDragNDrop(_homeActivity: HomeActivity, leftDragHandle: View, rightDragHandle: View, dragNDropView: DragLayer) {
    val dragHandler = Handler()
    var shouldHandleLeftDropTarget = true
//    var isRemoveDropAction = false

    dragNDropView.registerDropTarget(object : DropTargetListener {
      override val view: View
        get() = mDropActionRemove
      var cachedBitmap: Bitmap? = null
      override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
        mDropActionView.animate().alpha(1f)
        return true
      }

      override fun onStartDrag(action: DragAction.Action, location: PointF) {
      }

      override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
        onRemoveItem(item)
        cachedBitmap = null
      }

      override fun onMove(view: View, action: DragAction.Action, location: PointF) {
      }

      override fun onEnter(action: DragAction.Action, location: PointF) {
        isDropAction = true
        if (DragHandler._cachedDragBitmap != null) {
          cachedBitmap = DragHandler._cachedDragBitmap
          DragHandler._cachedDragBitmap = ImageUtil.grayscaleImage(cachedBitmap!!)
        }
      }

      override fun onExit(action: DragAction.Action, location: PointF) {
        isDropAction = false
        if (cachedBitmap != null) {
          DragHandler._cachedDragBitmap = cachedBitmap
          cachedBitmap = null
        }
      }

      override fun onEnd() {
        mDropActionView.animate().alpha(0f)
      }

    })

    dragNDropView.registerDropTarget(object : DropTargetListener {

      var runnable: Runnable = object : Runnable {
        override fun run() {
          val i = workspace.currentItem
          if (i > 0) {
            val prevPage = workspace.pages[i]

            workspace.currentItem = i - 1
            shouldHandleLeftDropTarget = true
            if (prevPage.childCount == 0) {
              workspace.removePage(i)
            }
          } else if (i == 0) {
            // workspace.addPageLeft(true)
            dragHandler.removeCallbacksAndMessages(null)
            leftDragHandle.alpha = 0f
            shouldHandleLeftDropTarget = false
          }
          dragHandler.postDelayed(this, 500)
        }
      }
      override val view: View
        get() = leftDragHandle

      override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
        return true
      }

      override fun onStartDrag(action: DragAction.Action, location: PointF) {
        leftDragHandle.alpha = (0.5f)
      }

      override fun onEnter(action: DragAction.Action, location: PointF) {
        if (shouldHandleLeftDropTarget) {
          leftDragHandle.alpha = 0.9f
          dragHandler.post(runnable)
        }
      }

      override fun onMove(view: View, action: DragAction.Action, location: PointF) {
        // do nothing
      }

      override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
        // do nothing
      }

      override fun onExit(action: DragAction.Action, location: PointF) {
        if (shouldHandleLeftDropTarget) {
          dragHandler.removeCallbacksAndMessages(null)
          leftDragHandle.alpha = 0.5f
        }
      }

      override fun onEnd() {
        dragHandler.removeCallbacksAndMessages(null)
        leftDragHandle.alpha = 0f
      }
    })

    dragNDropView.registerDropTarget(object : DropTargetListener {
      var runnable: Runnable = object : Runnable {
        override fun run() {
          val i = workspace.currentItem
          if (i < workspace.pages.size - 1) {
            workspace.currentItem = i + 1
          } else if (i == workspace.pages.size - 1) {
            workspace.addPageRight(true)
          }
          dragHandler.postDelayed(this, 500)
        }
      }
      override val view: View
        get() = rightDragHandle

      override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
        return true
      }

      override fun onStartDrag(action: DragAction.Action, location: PointF) {
//                rightDragHandle.animate().alpha(0.5f)
      }

      override fun onEnter(action: DragAction.Action, location: PointF) {
        dragHandler.post(runnable)
        rightDragHandle.animate().alpha(0.9f)
      }

      override fun onMove(view: View, action: DragAction.Action, location: PointF) {
        // do nothing
      }

      override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
        // do nothing
      }

      override fun onExit(action: DragAction.Action, location: PointF) {
        dragHandler.removeCallbacksAndMessages(null)
        rightDragHandle.animate().alpha(0.5f)
      }

      override fun onEnd() {
        dragHandler.removeCallbacksAndMessages(null)
        rightDragHandle.animate().alpha(0f)
      }
    })

    // workspace drag event
    dragNDropView.registerDropTarget(object : DropTargetListener {
      override val view: View
        get() = workspace

      override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
        if (DragAction.Action.SEARCH != action && DragAction.Action.APPWIDGET != action)
          dragLayer.showItemPopup(_homeActivity)

        mRightDropTarget.alpha = 0.5f

        if (!Workspace.isAtFirstPage() && mLeftDropTarget.alpha == 0f) {
          mLeftDropTarget.alpha = 0.5f
          shouldHandleLeftDropTarget = true
        }
        return true
      }

      override fun onStartDrag(action: DragAction.Action, location: PointF) {
        _homeActivity.closeAppDrawer()
        _homeActivity.closeWidgetPicker()
        if (Settings.appSettings().desktopShowGrid) {
          dock.setHideGrid(false)
          for (cellContainer in workspace.pages) {
            cellContainer.setHideGrid(false)
          }
        }
      }

      override fun onEnter(action: DragAction.Action, location: PointF) {
        // do nothing
      }

      override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
        // this statement makes sure that adding an app multiple times from the app drawer works
        // the app will get a new id every time
        if (isDropAction) {
          isDropAction = false
          return
        }

        if (DragAction.Action.APPWIDGET == action) {
          mAppWidgetPicker.onCreateWidget(this@HomeActivity, item as AppWidget, location)
          return
        }

        if (DragAction.Action.DRAWER == action) {
          if (appDrawerController._isOpen) {
            return
          }
          item.reset()
        }

        val x = location.x.toInt()
        val y = location.y.toInt()
        if (workspace.addItemToPoint(item, x, y)) {
          workspace.consumeLastItem()
          dock.consumeLastItem()
          Log.d(TAG, "saving item to database")
          // add the item to the database
          _db.saveItem(item, workspace.currentItem, Constants.ItemPosition.Desktop)
        } else {
          val pos = Point()
          workspace.currentPage.touchPosToCoordinate(pos, x, y, item.spanX, item.spanY, false)
          val itemView = workspace.currentPage.coordinateToChildView(pos)
          if (itemView != null && Workspace.handleOnDropOver(item, itemView.tag as Item, itemView, workspace.currentPage, workspace.currentItem, Constants.ItemPosition.Desktop, workspace)) {
            workspace.consumeLastItem()
            dock.consumeLastItem()
          } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space)
            workspace.revertLastItem()
            dock.revertLastItem()
          }
        }
      }

      override fun onMove(view: View, action: DragAction.Action, location: PointF) {
        if (Workspace.isAtFirstPage()) {
          leftDragHandle.alpha = 0f
          shouldHandleLeftDropTarget = false
        } else {
          if (mLeftDropTarget.alpha == 0.0f) {
            leftDragHandle.alpha = (0.5f)
          }
          shouldHandleLeftDropTarget = true
        }
        val item = view.tag
        if ((item is Item && item.type == Item.Type.APPWIDGET) || item is AppWidget) {
          Log.d(TAG, "update widget projection")
          workspace.updateWidgetPreviewProjection(location.x.toInt(), location.y.toInt(), item as Item)
        } else {
          workspace.updateIconProjection(location.x.toInt(), location.y.toInt())
        }
      }

      override fun onExit(action: DragAction.Action, location: PointF) {
        for (page in workspace.pages) {
          page.clearCachedOutlineBitmap()
        }
        dragNDropView.cancelFolderPreview()
      }

      override fun onEnd() {
        for (page in workspace.pages) {
          page.clearCachedOutlineBitmap()
        }
        if (Settings.appSettings().desktopShowGrid) {
          dock.setHideGrid(true)
          for (cellContainer in workspace.pages) {
            cellContainer.setHideGrid(true)
          }
        }
      }
    })

    // dock drag event
    dragNDropView.registerDropTarget(object : DropTargetListener {
      override val view: View
        get() = dock

      override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
        return true
      }

      override fun onStartDrag(action: DragAction.Action, location: PointF) {
        // do nothing
      }

      override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
        if (DragAction.Action.DRAWER == action) {
          if (appDrawerController._isOpen) {
            return
          }
          item.reset()
        }

        val x = location.x.toInt()
        val y = location.y.toInt()
        if (dock.addItemToPoint(item, x, y)) {
          workspace.consumeLastItem()
          dock.consumeLastItem()

          // add the item to the database
          _db.saveItem(item, 0, Constants.ItemPosition.Dock)
        } else {
          val pos = Point()
          dock.touchPosToCoordinate(pos, x, y, item.spanX, item.spanY, false)
          val itemView = dock.coordinateToChildView(pos)
          if (itemView != null) {
            if (Workspace.handleOnDropOver(item, itemView.tag as Item, itemView, dock, 0, Constants.ItemPosition.Dock, dock)) {
              workspace.consumeLastItem()
              dock.consumeLastItem()
            } else {
              Tool.toast(_homeActivity, R.string.toast_not_enough_space)
              workspace.revertLastItem()
              dock.revertLastItem()
            }
          } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space)
            workspace.revertLastItem()
            dock.revertLastItem()
          }
        }
      }

      override fun onMove(view: View, action: DragAction.Action, location: PointF) {
        dock.updateIconProjection(location.x.toInt(), location.y.toInt())
      }

      override fun onEnter(action: DragAction.Action, location: PointF) {
        // do nothing
      }

      override fun onExit(action: DragAction.Action, location: PointF) {
        dock.clearCachedOutlineBitmap()
        dragNDropView.cancelFolderPreview()
      }

      override fun onEnd() {
        dock.clearCachedOutlineBitmap()
      }
    })
  }

  private fun registerBroadcastReceiver() {
    _appUpdateReceiver = AppUpdateReceiver()
    _shortcutReceiver = ShortcutReceiver()
    _networkChangedReceiver = NetworkStateReceiver()

    // register all receivers
    registerReceiver(_networkChangedReceiver, _networkChangedIntentFilter)
    registerReceiver(_appUpdateReceiver, _appUpdateIntentFilter)
    registerReceiver(_shortcutReceiver, _shortcutIntentFilter)
  }

  override fun onRemovePage() {
    if (workspace.isCurrentPageEmpty) {
      workspace.removeCurrentPage()
      return
    }
    DialogHelper.alertDialog(this, getString(R.string.remove), "This page is not empty. Those items will also be removed.") {
      workspace.removeCurrentPage()
    }
  }

  fun onStartApp(context: Context, app: App, view: View?) {
    if (BuildConfig.APPLICATION_ID == app.packageName) {
      LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context)
    } else {
      try {
        val intent = IntentUtil.getIntentFromApp(app)
        context.startActivity(intent, getActivityAnimationOpts(view))
        // close app drawer and other items in advance
        // annoying to wait for app drawer to close
        handleLauncherResume()
      } catch (e: Exception) {
        e.printStackTrace()
        Tool.toast(context, R.string.toast_app_uninstalled)
      }
    }
  }

  fun onUninstallItem(item: Item) {
    ignoreResume = true
    Settings.eventHandler().showDeletePackageDialog(this, item)
  }

  fun onRemoveItem(item: Item) {
    val desktop = workspace
    val coordinateToChildView: View?
    if (item.type != Item.Type.APPWIDGET) {
      if (item._location == Constants.ItemPosition.Desktop) {
        coordinateToChildView = desktop.currentPage.coordinateToChildView(Point(item.x, item.y))!!
        desktop.removeItem(coordinateToChildView, true)
      } else {
        val dock = dock
        coordinateToChildView = dock.coordinateToChildView(Point(item.x, item.y))
        dock.removeItem(coordinateToChildView!!, true)
      }
    }
    _db.deleteItem(item, true)
  }

  fun onInfoItem(item: Item) {
    if (item.type == Item.Type.APP) {
      try {
        val str = "android.settings.APPLICATION_DETAILS_SETTINGS"
        val stringBuilder = StringBuilder()
        stringBuilder.append("package:")
        val intent = item.intent
        val component = intent.component
        stringBuilder.append(component!!.packageName)
        startActivity(Intent(str, Uri.parse(stringBuilder.toString())))
      } catch (e: Exception) {
        Tool.toast(this, R.string.toast_app_uninstalled)
      }
    }
  }

  private fun getActivityAnimationOpts(view: View?): Bundle? {
    var bundle: Bundle? = null
    if (view == null) {
      return null
    }
    var opts: ActivityOptions? = null
    if (VERSION.SDK_INT >= 23) {
      var left = 0
      var top = 0
      var width = view.measuredWidth
      val height = view.measuredHeight
      if (view is AppItemView) {
        width = view.iconSize.toInt()
        left = view.drawIconLeft.toInt()
        top = view.drawIconTop.toInt()
      }
      opts = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height)
    } else if (VERSION.SDK_INT < 21) {
      opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
    }
    if (opts != null) {
      bundle = opts.toBundle()
    }
    return bundle
  }

  override fun onDesktopEdit() {
    AppWidgetResizeFrame.hideResizeFrame()
    Animation.fadeIn(100, desktopOptionView)
    updateDesktopIndicator(false)
    updateDock(false)
  }

  override fun onFinishDesktopEdit() {
    Animation.fadeOut(100, desktopOptionView)
    updateDesktopIndicator(true)
    updateDock(true)
  }

  override fun onSetPageAsHome() {
    val appSettings = Settings.appSettings()
    appSettings.desktopPageCurrent = workspace.currentItem
  }

  override fun onLaunchSettings() {
    Settings.eventHandler().showLauncherSettings(this)
  }

  override fun onPickDesktopAction() {
    Settings.eventHandler().showPickAction(this, (object : DialogListener.OnActionDialogListener {
      override fun onAdd(type: Int) {
        val pos = workspace.currentPage.findFreeSpace()
        if (pos != null) {
          workspace.addItemToCell(Item.newActionItem(type), pos.x, pos.y)
        } else {
          Tool.toast(applicationContext, R.string.toast_not_enough_space)
        }
      }
    }))
  }

  override fun onPickWidget(view: View?) {
    ignoreResume = false
    openWidgetPicker(view)
  }

  fun clearRoomForPopUp() {
    Animation.fadeOut(200, workspace)
    updateDesktopIndicator(false)
    updateDock(false)
  }

  fun unClearRoomForPopUp() {
    Animation.fadeIn(200, workspace)
    updateDesktopIndicator(true)
    updateDock(true)
  }

  fun updateDock(show: Boolean) {
    val appSettings = Settings.appSettings()
    if (appSettings.dockEnable && show) {
      Animation.fadeIn(100, dock)
    } else {
      if (appSettings.dockEnable) {
        Animation.fadeOut(100, dock)
      } else {
        Animation.goneViews(100, dock)
      }
    }
  }

  fun updateDesktopIndicator(show: Boolean) {
    val appSettings = Settings.appSettings()
    if (appSettings.desktopShowIndicator && show) {
      Animation.fadeIn(100, desktopIndicator)
    } else {
      Animation.fadeOut(100, desktopIndicator)
    }
  }

  private fun updateHomeLayout() {
    updateDock(true)
    updateDesktopIndicator(true)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_BIND_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET) {
      if (resultCode == Activity.RESULT_OK) {
        mAppWidgetPicker.onWidgetBinded(launcher, data!!)
      }
    }
  }

  override fun onBackPressed() {
    handleLauncherResume()
  }

  override fun onStart() {
    _WidgetHost.startListening()
    _launcher = this

    super.onStart()
  }

  override fun onResume() {
    super.onResume()
    _WidgetHost.startListening()
    _launcher = this

    // handle restart if something needs to be reset
    val appSettings = Settings.appSettings()
    if (appSettings.appRestartRequired) {
      appSettings.appRestartRequired = false
      recreate()
      return
    }

    // handle launcher rotation
    if (appSettings.desktopOrientationMode == 2) {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    } else if (appSettings.desktopOrientationMode == 1) {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    } else {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    handleLauncherResume()
  }

  override fun onDestroy() {
    _WidgetHost.stopListening()
    _launcher = null

    unregisterReceiver(_appUpdateReceiver)
    unregisterReceiver(_shortcutReceiver)
    unregisterReceiver(_timeChangedReceiver)
    super.onDestroy()
  }

  private fun handleLauncherResume() {
    if (ignoreResume) {
      // only triggers when a new activity is launched that should leave launcher state alone
      // uninstall package activity and pick widget activity
      ignoreResume = false
    } else {

      groupPopup.collapse()
      // close app option menu
      dragLayer.collapse()
      when {
        workspace.inEditMode -> // exit workspace edit mode
          workspace.currentPage.performClick()
        appDrawerController.visibility == View.VISIBLE -> closeAppDrawer()
        findViewById<ContentView>(R.id.content_view).currentItem == 0 -> findViewById<ContentView>(R.id.content_view).currentItem = 1
        widgetPicker.visibility == View.VISIBLE -> closeWidgetPicker()
        workspace.currentItem != 0 -> {
          val appSettings = Settings.appSettings()
          workspace.currentItem = appSettings.desktopPageCurrent
        }
      }
    }
  }

  private fun openWidgetPicker(view: View? = null) {
    val pos = IntArray(2)
    view!!.getLocationInWindow(pos)
    cx = pos[0]
    cy = pos[1]

    cx += (view.width / 2f).toInt()
    cy += (view.height / 2f).toInt()

    cy -= widgetPicker.paddingTop
    workspace.currentPage.performClick()
    widgetPicker.open(cx, cy)
  }

  fun closeWidgetPicker() {
    widgetPicker.close(cx, cy)
  }

  fun openAppDrawer(view: View? = null, x: Int = 0, y: Int = 0) {
    if (!(x > 0 && y > 0) && view != null) {
      val pos = IntArray(2)
      view.getLocationInWindow(pos)
      cx = pos[0]
      cy = pos[1]

      cx += (view.width / 2f).toInt()
      cy += (view.height / 2f).toInt()
      if (view is AppItemView) {
        val appItemView = view as AppItemView?
        if (appItemView != null && appItemView.showLabel) {
          cy -= (Tool.dp2px(14f) / 2f).toInt()
        }
      }
      cy -= appDrawerController.paddingTop
    } else {
      cx = x
      cy = y
    }
    appDrawerController.open(cx, cy)
  }

  fun closeAppDrawer() {
    appDrawerController.close(cx, cy)
  }

  inner class HomeWidgetPicker(private val homeActivity: HomeActivity) : Callback.a2<Boolean, Boolean> {
    override fun callback(shouldOpenPicker: Boolean, shouldStartAnimation: Boolean) {
      if (shouldOpenPicker) {
        if (shouldStartAnimation) {
          widgetPicker.postDelayed({
            Animation.fadeOut(200, homeActivity.workspace)
            homeActivity.updateDesktopIndicator(false)
            homeActivity.updateDock(false)
          }, 100)
        }
      } else {
        if (!shouldStartAnimation) {
          // the end of app widget picker animation
          homeActivity.widgetPicker.reset()
        } else {
          Animation.fadeIn(200, homeActivity.workspace)
          homeActivity.updateDesktopIndicator(true)
          homeActivity.updateDock(true)
        }
      }
    }

    fun initWidgetPicker(widgetPicker: AppWidgetPicker) {
      widgetPicker.setCallBack(this)
    }
  }

  inner class HomeAppDrawer(private val homeActivity: HomeActivity) : Callback.a2<Boolean, Boolean> {

    fun initAppDrawer(appDrawerController: AppDrawerController) {
      appDrawerController.setCallBack(this)
    }

    override fun callback(shouldOpenDrawer: Boolean, shouldStartAnimation: Boolean) {
      if (shouldOpenDrawer) {
        if (shouldStartAnimation) {
          appDrawerController.postDelayed({
            Animation.fadeOut(200, homeActivity.workspace)
            homeActivity.updateDesktopIndicator(false)
            homeActivity.updateDock(false)
          }, 100)
        }
      } else {
        if (shouldStartAnimation) {
          Animation.fadeIn(200, homeActivity.workspace)
          homeActivity.updateDesktopIndicator(true)
          homeActivity.updateDock(true)
        } else {
          // the end of app drawer animation
          if (!Settings.appSettings().drawerRememberPosition) {
            homeActivity.appDrawerController.reset()
          }
          homeActivity.updateDesktopIndicator(true)
        }
      }
    }
  }

  class HomeGestureCallback(private val _appSettings: AppSettings) : WorkspaceGestureListener.WorkspaceGestureCallback {

    override fun onDrawerGesture(desktop: Workspace, event: WorkspaceGestureListener.Type): Boolean {
      var gesture: Any? = null
      when (event) {
        WorkspaceGestureListener.Type.SwipeUp -> gesture = _appSettings.gestureSwipeUp
        WorkspaceGestureListener.Type.SwipeDown -> gesture = _appSettings.gestureSwipeDown
        WorkspaceGestureListener.Type.SwipeLeft, WorkspaceGestureListener.Type.SwipeRight -> {
        }
        WorkspaceGestureListener.Type.Pinch -> gesture = _appSettings.gesturePinch
        WorkspaceGestureListener.Type.Unpinch -> gesture = _appSettings.gestureUnpinch
        WorkspaceGestureListener.Type.DoubleTap -> gesture = _appSettings.gestureDoubleTap
//        else -> Log.e(javaClass.toString(), "gesture error")
      }
      if (gesture != null) {
        if (_appSettings.gestureFeedback) {
          Tool.vibrate(desktop)
        }
        if (gesture is Intent) {
          val intent = gesture as Intent?
          Tool.startApp(launcher, Settings.appLoader().findApp(intent)!!, null)
        } else if (gesture is LauncherAction.ActionDisplayItem) {
          LauncherAction.RunAction(gesture, desktop.context)
        }
        return true
      }
      return false
    }
  }

  class HomeAppEditApplier(private val _homeActivity: HomeActivity) : DialogListener.OnEditDialogListener {
    private var _item: Item? = null

    fun onEditItem(item: Item) {
      _item = item
      Settings.eventHandler().showEditDialog(_homeActivity, item, this)
    }

    override fun onRename(name: String) {
      _item!!.label = name
      Settings.dataManager().saveItem(_item!!)
      val point = Point(_item!!.x, _item!!.y)

      if (_item!!._location == Constants.ItemPosition.Desktop) {
        val desktop = _homeActivity.workspace
        desktop.removeItem(desktop.currentPage.coordinateToChildView(point)!!, false)
        desktop.addItemToCell(_item!!, _item!!.x, _item!!.y)
      } else {
        val dock = _homeActivity.dock
        _homeActivity.dock.removeItem(dock.coordinateToChildView(point)!!, false)
        dock.addItemToCell(_item!!, _item!!.x, _item!!.y)
      }
    }
  }
}


package com.fisma.trinity.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import com.fisma.trinity.fragment.SettingsMasterFragment
import com.fisma.trinity.util.BackupHelper
import com.nononsenseapps.filepicker.Utils
import net.gsantner.opoc.util.ContextUtils
import java.io.File
import kotlin.system.exitProcess


class SettingsActivity : ThemeActivity() {
  var toolbar: Toolbar? = null

  private var prefFrag: SettingsMasterFragment? = null

  override fun onCreate(b: Bundle?) {
    // must be applied before setContentView
    super.onCreate(b)
    val contextUtils = ContextUtils(this)
    contextUtils.setAppLanguage(_appSettings!!.language)

    setContentView(R.layout.activity_settings)

    toolbar = findViewById(R.id.toolbar)
    toolbar!!.setTitle(R.string.pref_title__settings)
    setSupportActionBar(toolbar)
    toolbar!!.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_white_24px)
    toolbar!!.setNavigationOnClickListener { v -> onBackPressed() }

    prefFrag = SettingsMasterFragment()
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(R.id.fragment_holder, prefFrag!!).commit()

    // if system exit is called the app will open settings activity again
    // this pushes the user back out to the home activity
    if (_appSettings!!.appRestartRequired) {
      startActivity(Intent(this, HomeActivity::class.java))
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      val files = Utils.getSelectedFilesFromResult(data!!)
      when (requestCode) {
        Constants.INTENT_BACKUP -> BackupHelper.backupConfig(this, File(Utils.getFileForUri(files[0]).absolutePath + "/openlauncher.zip").toString())
        Constants.INTENT_RESTORE -> {
          BackupHelper.restoreConfig(this, Utils.getFileForUri(files[0]).toString())
          exitProcess(0)
        }
      }
    }
  }

  override fun onBackPressed() {
    if (prefFrag != null && prefFrag!!.canGoBack()) {
      prefFrag!!.goBack()
      return
    }
    super.onBackPressed()
  }
}

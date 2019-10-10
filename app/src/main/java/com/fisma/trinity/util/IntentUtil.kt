package com.fisma.trinity.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.fisma.trinity.Constants
import com.fisma.trinity.model.App

class IntentUtil {

  companion object {
    fun isIntentActionAvailable(context: Context, action: String): Boolean {
      val packageManager = context.packageManager
      val intent = Intent(action)
      val resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
      return resolveInfo.size > 0
    }

    fun getIntentAsString(intent: Intent?): String {
      return if (intent == null) {
        ""
      } else {
        intent!!.toUri(0)
      }
    }

    fun getIntentFromString(string: String): Intent? {
      try {
        return Intent.parseUri(string, 0)
      } catch (e: Exception) {
        e.printStackTrace()
        return null
      }
    }

    fun getIntentFromApp(app: App): Intent {
      val intent = Intent(Intent.ACTION_MAIN)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.setClassName(app.packageName, app.className)
      return intent
    }

    fun getDefaultAppIntent(category: Constants.AppCategory): Intent? {
      return when (category) {
        Constants.AppCategory.PHONE -> {
          val intent = Intent(Intent.ACTION_MAIN)
          intent.addCategory(Intent.CATEGORY_LAUNCHER)
          intent.`package` = "com.android.dialer"
          intent
        }
        Constants.AppCategory.MESSAGING -> {
          val intent = Intent(Intent.ACTION_MAIN)
          intent.addCategory(Intent.CATEGORY_DEFAULT)
          intent.type = "vnd.android-dir/mms-sms"
          intent
        }
        Constants.AppCategory.EMAIL -> {
          val intent = Intent(Intent.ACTION_MAIN)
          intent.addCategory(Intent.CATEGORY_APP_EMAIL)
          intent.addCategory(Intent.CATEGORY_DEFAULT)
          intent
        }
        Constants.AppCategory.CONTACTS -> {
          val intent = Intent(Intent.ACTION_MAIN)
          intent.addCategory(Intent.CATEGORY_APP_CONTACTS)
          intent.addCategory(Intent.CATEGORY_DEFAULT)
          intent
        }
        else -> null
      }
    }
  }
}
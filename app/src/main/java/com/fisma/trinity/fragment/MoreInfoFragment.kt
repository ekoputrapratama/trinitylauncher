package com.fisma.trinity.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.fisma.trinity.R
import com.fisma.trinity.activity.SettingsActivity
import com.fisma.trinity.util.AppSettings
import net.gsantner.opoc.format.markdown.SimpleMarkdownParser
import net.gsantner.opoc.preference.GsPreferenceFragmentCompat
import net.gsantner.opoc.util.ActivityUtils
import net.gsantner.opoc.util.ShareUtil
import java.io.IOException
import java.util.*


class MoreInfoFragment : GsPreferenceFragmentCompat<AppSettings>() {

  override fun getPreferenceResourceForInflation(): Int {
    return R.xml.prefactions__more_information
  }

  override fun getFragmentTag(): String {
    return TAG
  }

  override fun getAppSettings(context: Context): AppSettings {
    return if (_appSettings != null) _appSettings else AppSettings(context)
  }

  override fun onPreferenceClicked(preference: Preference, key: String, keyResId: Int): Boolean? {
    val au = ActivityUtils(activity)
    if (isAdded && preference.hasKey()) {
      when (keyToStringResId(preference)) {
        R.string.pref_key__more_info__app -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_web_url))
          return true
        }
        R.string.pref_key__more_info__settings -> {
          au.animateToActivity(SettingsActivity::class.java, false, 124)
          return true
        }
        R.string.pref_key__more_info__rate_app -> {
          au.showGooglePlayEntryForThisApp()
          return true
        }
        R.string.pref_key__more_info__join_community -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_community_url))
          return true
        }
        R.string.pref_key__more_info__donate -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_donate_url))
          return true
        }
        R.string.pref_key__more_info__bug_reports -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_bugreport_url))
          return true
        }
        R.string.pref_key__more_info__translate -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_translate_url))
          return true
        }
        R.string.pref_key__more_info__project_contribution_info -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_contribution_info_url))
          return true
        }
        R.string.pref_key__more_info__android_contribution_guide -> {
          _cu.openWebpageInExternalBrowser(
            String.format("https://gsantner.net/android-contribution-guide/?packageid=%s&name=%s&web=%s",
              _cu.context().packageName, getString(R.string.app_name), getString(R.string.app_web_url).replace("=", "%3D")))
          return true
        }
        R.string.pref_key__more_info__source_code -> {
          _cu.openWebpageInExternalBrowser(getString(R.string.app_source_code_url))
          return true
        }
        R.string.pref_key__more_info__project_license -> {
          try {
            au.showDialogWithHtmlTextView(R.string.licenses, SimpleMarkdownParser().parse(
              resources.openRawResource(R.raw.license),
              "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).html)
          } catch (e: IOException) {
            e.printStackTrace()
          }

          return true
        }
        R.string.pref_key__more_info__open_source_licenses -> {
          try {
            au.showDialogWithHtmlTextView(R.string.licenses, SimpleMarkdownParser().parse(
              resources.openRawResource(R.raw.licenses_3rd_party),
              "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).html)
          } catch (e: IOException) {
            e.printStackTrace()
          }

          return true
        }
        R.string.pref_key__more_info__contributors_public_info -> {
          try {
            au.showDialogWithHtmlTextView(R.string.contributors, SimpleMarkdownParser().parse(
              resources.openRawResource(R.raw.contributors),
              "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).html)
          } catch (e: IOException) {
            e.printStackTrace()
          }

          return true
        }
        R.string.pref_key__more_info__copy_build_information -> {
          ShareUtil(context).setClipboard(preference.summary)
          val smp = SimpleMarkdownParser()
          try {
            val html = smp.parse(resources.openRawResource(R.raw.changelog), "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, SimpleMarkdownParser.FILTER_CHANGELOG).html
            au.showDialogWithHtmlTextView(R.string.changelog, html)
          } catch (ex: Exception) {

          }

          return true
        }
      }
    }
    return null
  }

  override fun isAllowedToTint(pref: Preference): Boolean {
    return getString(R.string.pref_key__more_info__app) != pref.key
  }

  @Synchronized
  override fun doUpdatePreferences() {
    super.doUpdatePreferences()
    val context = context ?: return
    val locale = Locale.getDefault()
    var tmp: String
    var pref: Preference?
    updateSummary(R.string.pref_key__more_info__project_license, getString(R.string.app_license_name))

    pref = findPreference(R.string.pref_key__more_info__app)
    // Basic app info
    if (pref != null && pref.summary == null) {
      pref.setIcon(R.mipmap.ic_launcher)
      pref.summary = String.format(locale, "%s\nVersion v%s (%d)", _cu.packageIdReal, _cu.appVersionName, _cu.bcint("VERSION_CODE", 0))
    }

    pref = findPreference(R.string.pref_key__more_info__copy_build_information)
    // Extract some build information and publish in summary
    if (pref != null && pref.summary == null) {
      var summary = String.format(locale, "\n<b>Package:</b> %s\n<b>Version:</b> v%s (%d)", _cu.packageIdReal, _cu.appVersionName, _cu.bcint("VERSION_CODE", 0))
      tmp = _cu.bcstr("FLAVOR", "")
      summary += if (tmp.isEmpty()) "" else "\n<b>Flavor:</b> " + tmp.replace("flavor", "")
      tmp = _cu.bcstr("BUILD_TYPE", "")
      summary += if (tmp.isEmpty()) "" else " ($tmp)"
      tmp = _cu.bcstr("BUILD_DATE", "")
      summary += if (tmp.isEmpty()) "" else "\n<b>Build date:</b> $tmp"
      tmp = _cu.appInstallationSource
      summary += if (tmp.isEmpty()) "" else "\n<b>ISource:</b> $tmp"
      tmp = _cu.bcstr("GITHASH", "")
      summary += if (tmp.isEmpty()) "" else "\n<b>VCS Hash:</b> $tmp"
      pref.summary = _cu.htmlToSpanned(summary.trim { it <= ' ' }.replace("\n", "<br/>"))
    }

    // Extract project team from raw ressource, where 1 person = 4 lines
    // 1) Name/Title, 2) Description/Summary, 3) Link/View-IntentUtil, 4) Empty line
    pref = findPreference(R.string.pref_key__more_info__project_team)
    if (pref != null && (pref as PreferenceGroup).preferenceCount == 0) {
      val data = (_cu.readTextfileFromRawRes(R.raw.project_team, "", "").trim { it <= ' ' } + "\n\n").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      var i = 0
      while (i + 2 < data.size) {
        val person = Preference(context)
        person.title = data[i]
        person.summary = data[i + 1]
        person.setIcon(R.drawable.ic_person_black_24dp)
        try {
          val uri = Uri.parse(data[i + 2])
          val intent = Intent(Intent.ACTION_VIEW, uri)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          person.intent = intent
        } catch (ignored: Exception) {
        }

        appendPreference(person, pref as PreferenceGroup?)
        i += 4
      }
    }
  }

  companion object {
    val TAG = "MoreInfoFragment"

    fun newInstance(): MoreInfoFragment {
      return MoreInfoFragment()
    }
  }
}

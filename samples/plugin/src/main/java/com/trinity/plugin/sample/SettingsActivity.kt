package com.trinity.plugin.sample

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

import java.net.URLDecoder

class SettingsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    val defaultUriEncoded = "content://com.trinity.plugin.sample/default"

    // Decode for special characters that may appear in URI. Review Android documentation on
    // special characters for more information:
    // https://developer.android.com/guide/topics/resources/string-resource#FormattingAndStyling
    val defaultUriDecoded = URLDecoder.decode(defaultUriEncoded, "UTF-8")

    // Grants permission for default slice.
    grantSlicePermissions(defaultUriDecoded.toUri())

    setContentView(R.layout.settings_activity)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.settings, SettingsFragment())
      .commit()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private fun grantSlicePermissions(uri: Uri, notifyIndexOfChange: Boolean = true) {
    // Grant permissions to AGSA
//    SliceManager.getInstance(this).grantSlicePermission(
//      "com.google.android.googlequicksearchbox",
//      uri
//    )
//
//    // grant permission to GmsCore
//    SliceManager.getInstance(this).grantSlicePermission(
//      "com.google.android.gms",
//      uri
//    )

    if (notifyIndexOfChange) {
      // Notify change. Ensure that it does not happen on every onCreate()
      // calls as notify change triggers reindexing which can clear usage
      // signals of your app and hence impact your app’s ranking. One way to
      // do this is to use shared preferences.
      val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)

      if (!sharedPreferences.getBoolean(PREF_GRANT_SLICE_PERMISSION, false)) {
        contentResolver.notifyChange(uri, null /* content observer */)
        sharedPreferences.edit {
          putBoolean(PREF_GRANT_SLICE_PERMISSION, true)
        }
      }
    }
  }

  class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
  }

  companion object {
    private const val PREF_GRANT_SLICE_PERMISSION = "permission_slice_status"

    fun getPendingIntent(context: Context): PendingIntent {
      val intent = Intent(context, SettingsActivity::class.java)
      return PendingIntent.getActivity(context, 0, intent, 0)
    }
  }
}
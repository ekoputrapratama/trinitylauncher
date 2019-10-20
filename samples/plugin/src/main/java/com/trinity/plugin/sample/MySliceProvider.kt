package com.trinity.plugin.sample

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.*
import com.trinity.plugin.sample.util.LazyFunctionMap
import com.trinity.plugin.sample.util.buildUriWithAuthority

/**
 * Examples of using slice template builders.
 */
class MySliceProvider : SliceProvider() {

  private lateinit var hostNameUrl: String

  private lateinit var defaultPath: String
  private lateinit var loadListPath: String
  private lateinit var loadGridPath: String
  private lateinit var contentNotifiers: LazyFunctionMap<Uri, Unit>

  override fun onCreateSliceProvider(): Boolean {

    Log.d(TAG, "onCreateSliceProvider()")

    val contextNonNull = context ?: return false
    // Initialize Slice URL and all possible slice paths.

    defaultPath = "/default"

    return true
  }

  /*
   * Takes an Intent (as specified by the intent-filter in the manifest) with data
   * ("https://interactivesliceprovider.android.example.com/<your_path>") and returns a content
   * URI ("content://com.example.android.interactivesliceprovider/<your_path>").
   */
  override fun onMapIntentToUri(intent: Intent): Uri {

    val intentPath = intent.data?.path ?: "/"
    val uriWithoutPathSlash = intentPath.replace("/", "")

    val uri = context!!.buildUriWithAuthority(uriWithoutPathSlash)

    Log.d(TAG, "onMapIntentToUri(): \nintentPath: $intentPath \nuri:$uri")

    return uri
  }

  override fun onBindSlice(sliceUri: Uri?): Slice? {
    Log.d(TAG, "onBindSlice(): $sliceUri")
    if (sliceUri == null || sliceUri.path == null) {
      return null
    }

    return getSliceBuilder(sliceUri)?.buildSlice()
  }

  private fun getSliceBuilder(sliceUri: Uri) = when (sliceUri.path) {
    defaultPath -> DefaultSliceBuilder(
      context = context!!,
      sliceUri = sliceUri
    )
    else -> {
      Log.e(TAG, "Unknown URI: $sliceUri")
      null
    }
  }

  override fun onSlicePinned(sliceUri: Uri?) {
    super.onSlicePinned(sliceUri)
    Log.d(TAG, "onSlicePinned - ${sliceUri?.path}")
//    when (sliceUri?.path) {
//      loadListPath -> repo.registerListSliceDataCallback(contentNotifiers[sliceUri])
//      loadGridPath -> repo.registerGridSliceDataCallback(contentNotifiers[sliceUri])
//      else -> Log.d(TAG, "No pinning actions for URI: $sliceUri")
//    }
  }

  override fun onSliceUnpinned(sliceUri: Uri?) {
    super.onSliceUnpinned(sliceUri)
    Log.d(TAG, "onSliceUnpinned - ${sliceUri?.path}")
//    when (sliceUri?.path) {
//      loadListPath -> repo.unregisterListSliceDataCallbacks()
//      loadGridPath -> repo.unregisterGridSliceDataCallbacks()
//      else -> Log.d(TAG, "No unpinning actions for URI: $sliceUri")
//    }
  }


  class DefaultSliceBuilder(
    val context: Context,
    sliceUri: Uri
  ) : SliceBuilder(sliceUri) {

    override fun buildSlice() = list(context, sliceUri, ListBuilder.INFINITY) {
      val activityAction = SliceAction.create(
        SettingsActivity.getPendingIntent(context),
        IconCompat.createWithResource(context, R.mipmap.ic_launcher),
        ListBuilder.LARGE_IMAGE,
        "Open app"
      )
      return list(context, sliceUri, ListBuilder.INFINITY) {
        //        setAccentColor(ContextCompat.getColor(context, R.color.colorAccent))
        // The first row can be a header or a row. If it's a header then it'll be styled
        // slightly differently -- you might not always want the content in the top row to be
        // bigger or differentiated if that doesn't make sense for the content.
        header {
          title = "Default app launcher slice!"

          // Subtitle is displayed when the slice is large (or small if a summary isn't set).
          subtitle = "Subtitle displayed in large mode."

          // Summary (optional) is displayed when the slice is small. It allows developers
          // to provide a summary when the rest of the slice isn't visible (other rows
          // outside of the header / first row aren't shown in small mode).
          summary = "Summary displayed in small mode."

          // Content description (optional) is what accessibility (talk back) will use to
          // describe the slice when the header is focused. If this isn't set, talkback will
          // read whatever text is in the header (title / subtitle etc.)
          contentDescription = "Content Description used for accessibility."

          primaryAction = activityAction
        }



        row {
          subtitle = "Subtitle for row 2!"
          contentDescription = "Row 2 Content Description"
          // Places icon/action at the front of the slice.
          // Note, these sorts of icons aren't allowed on the first row in a slice (usually
          // reserved for the header).
          setTitleItem(activityAction)
          primaryAction = activityAction
        }
        row {
          title = "Try this third row!"
          subtitle = "Subtitle for row 3!"
          contentDescription = "Row 3 Content Description"
          addEndItem(activityAction)
          primaryAction = activityAction
        }
      }
    }

    companion object {
      const val TAG = "DefaultSliceBuilder"
    }
  }

  companion object {
    const val TAG = "SliceProvider"

    const val ACTION_WIFI_CHANGED = "com.example.androidx.slice.action.WIFI_CHANGED"
    const val ACTION_TOAST = "com.example.androidx.slice.action.TOAST"
    const val EXTRA_TOAST_MESSAGE = "com.example.androidx.extra.TOAST_MESSAGE"
    const val ACTION_TOAST_RANGE_VALUE = "com.example.androidx.slice.action.TOAST_RANGE_VALUE"

    fun getPendingIntent(context: Context, action: String): PendingIntent {
      val intent = Intent(action)
      return PendingIntent.getActivity(context, 0, intent, 0)
    }
  }
}

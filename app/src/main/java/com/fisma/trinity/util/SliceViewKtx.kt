package com.fisma.trinity.util


import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.slice.SliceMetadata
import androidx.slice.core.SliceHints
import androidx.slice.widget.SliceLiveData
import androidx.slice.widget.SliceView
import androidx.slice.widget.SliceView.OnSliceActionListener
import com.fisma.trinity.activity.HomeActivity

const val TAG = "SliceViewKtx"
fun SliceView.bind(
  context: Context,
  uri: Uri,
  onSliceActionListener: OnSliceActionListener = OnSliceActionListener { _, _ -> },
  onClickListener: OnClickListener = OnClickListener { },
  onLongClickListener: OnLongClickListener = OnLongClickListener { false },
  scrollable: Boolean = false
) {
  setOnSliceActionListener(onSliceActionListener)
  setOnClickListener(onClickListener)
  isScrollable = scrollable
  setOnLongClickListener(onLongClickListener)
  if (uri.scheme == null) {
    Log.w(TAG, "Scheme is null for URI $uri")
    return
  }
  // If someone accidentally prepends the "slice-" prefix to their scheme, let's remove it.
  val scheme =
    if (uri.scheme.startsWith("slice-")) {
      uri.scheme.replace("slice-", "")
    } else {
      uri.scheme
    }
  if (scheme == ContentResolver.SCHEME_CONTENT ||
    scheme.equals("https", true) ||
    scheme.equals("http", true)
  ) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val sliceLiveData = SliceLiveData.fromIntent(context, intent)
    sliceLiveData.removeObservers(HomeActivity.launcher)
    try {
      sliceLiveData.observe(HomeActivity.launcher, Observer { updatedSlice ->
        if (updatedSlice == null) return@Observer
        slice = updatedSlice
        val expiry = SliceMetadata.from(context, updatedSlice).expiry
        if (expiry != SliceHints.INFINITY) {
          // Shows the updated text after the TTL expires.
          postDelayed(
            { slice = updatedSlice },
            expiry - System.currentTimeMillis() + 15
          )
        }
        Log.d(HomeActivity.TAG, "Update Slice: $updatedSlice")
      })
    } catch (e: Exception) {
      Log.e(
        HomeActivity.TAG,
        "Failed to find a valid ContentProvider for authority: $uri"
      )
    }
  } else {
    Log.w(HomeActivity.TAG, "Invalid uri, skipping slice: $uri")
  }
}
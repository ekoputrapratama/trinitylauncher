package com.fisma.trinity.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri


fun Context.buildUriWithAuthority(path: String): Uri {
  return Uri.Builder()
    .scheme(ContentResolver.SCHEME_CONTENT)
    .authority(packageName)
    .appendPath(path)
    .build()
}

/**
 * Copy a URI but remove the "slice-" prefix from its scheme.
 */
fun Uri.convertToOriginalScheme(): Uri {
  var builder = Uri.Builder()
    .authority(authority)
    .path(path)
    .encodedQuery(query)
    .fragment(fragment)
  builder = when (scheme) {
    "slice-http" -> builder.scheme("http")
    "slice-https" -> builder.scheme("https")
    "slice-content" -> builder.scheme("content")
    else -> builder
  }
  return builder.build()
}

/**
 * Copy a URI but add a "slice-" prefix to its scheme.
 */
fun Uri.convertToSliceViewerScheme(): Uri {
  var builder = Uri.Builder()
    .authority(authority)
    .path(path)
    .encodedQuery(query)
    .fragment(fragment)
  builder = when (scheme) {
    "http" -> builder.scheme("slice-http")
    "https" -> builder.scheme("slice-https")
    "content" -> builder.scheme("slice-content")
    else -> builder
  }
  return builder.build()
}

/**
 * We have to have an explicit list of schemes in our manifest that our SingleSliceViewer listens
 * to. Right now, these are "http", "https", and "content"; likely the only schemes used in the vast
 * majority of cases.
 */
fun Uri.hasSupportedSliceScheme(): Boolean {
  return scheme != null && (scheme.equals("slice-http", true)
    || scheme.equals("slice-https", true)
    || scheme.equals("slice-content", true))
}
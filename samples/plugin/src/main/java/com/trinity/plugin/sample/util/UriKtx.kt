package com.trinity.plugin.sample.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri


/**
 * @return Uri with the provided path.
 */
fun Context.buildUriWithAuthority(path: String): Uri {
  return Uri.Builder()
    .scheme(ContentResolver.SCHEME_CONTENT)
    .authority(packageName)
    .appendPath(path)
    .build()
}
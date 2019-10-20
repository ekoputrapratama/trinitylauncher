package com.fisma.trinity.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
  return { it -> creator(it.applicationContext) }
}

val Context.hasStoragePermission
  get() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
    this, Manifest.permission.READ_EXTERNAL_STORAGE)

val Context.hasCameraPermission
  get() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
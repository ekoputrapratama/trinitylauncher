package com.fisma.trinity.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


inline fun <T> Iterable<T>.safeForEach(action: (T) -> Unit) {
  val tmp = ArrayList<T>()
  tmp.addAll(this)
  for (element in tmp) action(element)
}

/**
 * Ensures that a value is within given bounds. Specifically:
 * If value is less than lowerBound, return lowerBound; else if value is greater than upperBound,
 * return upperBound; else return value unchanged.
 */
fun boundToRange(value: Int, lowerBound: Int, upperBound: Int): Int {
  return Math.max(lowerBound, Math.min(value, upperBound))
}
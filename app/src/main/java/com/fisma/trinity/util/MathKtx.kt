package com.fisma.trinity.util

import kotlin.math.ceil
import kotlin.math.roundToInt

fun Float.clamp(min: Float, max: Float): Float {
  if (this <= min) return min
  if (this >= max) return max
  return this
}

fun Float.round() = roundToInt().toFloat()

fun Float.ceilToInt() = ceil(this).toInt()

fun Double.ceilToInt() = ceil(this).toInt()

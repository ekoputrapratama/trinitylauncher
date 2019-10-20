package com.trinity.plugin.sample

import android.net.Uri
import androidx.slice.Slice

abstract class SliceBuilder(val sliceUri: Uri) {
  abstract fun buildSlice(): Slice
}
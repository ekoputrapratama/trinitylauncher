package com.fisma.trinity.compat

import android.content.Context
import android.os.Build

abstract class TorchCompat {

  abstract fun turnOnFlashLight()
  abstract fun tornOffFlashlight()
  abstract fun isEnabled(): Boolean

  companion object {
    private val sInstanceLock = Any()
    private var sInstance: TorchCompat? = null

    fun getInstance(context: Context): TorchCompat {
      synchronized(sInstanceLock) {
        if (sInstance == null) {
          sInstance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TorchCompatVM(context)
          } else {
            TorchCompatPreM(context)
          }
        }
        return sInstance!!
      }
    }
  }
}
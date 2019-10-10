package com.fisma.trinity.compat

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.hardware.camera2.CameraAccessException
import android.os.Handler


@TargetApi(Build.VERSION_CODES.M)
class TorchCompatVM(val context: Context) : TorchCompat() {
  var camManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
  var flashlightEnabled = false

  init {
    val handler = Handler()
    camManager.registerTorchCallback(object : CameraManager.TorchCallback() {
      override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
        super.onTorchModeChanged(cameraId, enabled)
        flashlightEnabled = enabled
      }
    }, handler)
  }

  override fun isEnabled(): Boolean {
    return flashlightEnabled
  }

  override fun turnOnFlashLight() {
    try {
      var cameraId: String? = null; // Usually front camera is at 0 position.
      if (camManager != null) {
        cameraId = camManager!!.cameraIdList[0]
        camManager!!.setTorchMode(cameraId, true)
      }
    } catch (e: CameraAccessException) {
      Log.e("", e.toString())
    }
  }

  override fun tornOffFlashlight() {
    try {
      val cameraId: String
      if (camManager != null) {
        cameraId = camManager!!.getCameraIdList()[0] // Usually front camera is at 0 position.
        camManager!!.setTorchMode(cameraId, false)
      }
    } catch (e: CameraAccessException) {
      e.printStackTrace()
    }

  }

}
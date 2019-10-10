package com.fisma.trinity.compat

import android.content.Context
import android.hardware.Camera


class TorchCompatPreM(val context: Context) : TorchCompat() {


  var mCamera: Camera? = null
  var parameters: Camera.Parameters? = null

  override fun isEnabled(): Boolean {
    return parameters != null && parameters!!.flashMode == Camera.Parameters.FLASH_MODE_TORCH
  }

  override fun turnOnFlashLight() {
    mCamera = Camera.open()
    parameters = mCamera!!.parameters
    parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
    mCamera!!.parameters = parameters
    mCamera!!.startPreview()
  }

  override fun tornOffFlashlight() {
    mCamera = Camera.open()
    parameters = mCamera!!.parameters
    parameters!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
    mCamera!!.parameters = parameters
    mCamera!!.stopPreview()
  }

}
package com.fisma.trinity.util

import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.fisma.trinity.manager.Settings


class Animation {
  companion object {
    fun fadeIn(duration: Long, vararg views: View) {
      if (views == null) return
      for (view in views) {
        if (view == null) continue
        view.animate().alpha(1f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withStartAction { view.visibility = View.VISIBLE }
      }
    }

    fun fadeOut(duration: Long, vararg views: View) {
      if (views == null) return
      for (view in views) {
        if (view == null) continue
        view.animate().alpha(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.INVISIBLE }
      }
    }

    fun goneViews(duration: Long, vararg views: View) {
      if (views == null) return
      for (view in views) {
        if (view == null) continue
        view.animate().alpha(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.GONE }
      }
    }

    fun createScaleInScaleOutAnim(view: View, action: Runnable) {
      val animTime = Settings.appSettings().animationSpeed.times(4)
      val animateScaleIn = view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(animTime.toLong())
      animateScaleIn.interpolator = AccelerateDecelerateInterpolator()
      Handler().postDelayed(Runnable {
        val animateScaleOut = view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animTime.toLong())
        animateScaleOut.interpolator = AccelerateDecelerateInterpolator()
        Handler().postDelayed(Runnable { action.run() }, animTime.toLong())
      }, animTime.toLong())
    }
  }
}


package com.fisma.trinity.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle


class AutoFinishActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    finish()
  }

  companion object {

    fun start(c: Context) {
      c.startActivity(Intent(c, AutoFinishActivity::class.java))
    }
  }
}

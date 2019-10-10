package com.fisma.trinity.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.fisma.trinity.R
import com.fisma.trinity.fragment.MoreInfoFragment


class MoreInfoActivity : ThemeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_more)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    toolbar.setBackgroundColor(_appSettings!!.primaryColor)

    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    val transaction = supportFragmentManager.beginTransaction()
    val moreInfoFragment = MoreInfoFragment.newInstance()
    transaction.replace(R.id.fragment_holder, moreInfoFragment, MoreInfoFragment.TAG).commit()
  }
}

package com.fisma.trinity.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fisma.trinity.R
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide


class OnBoardActivity : IntroActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // TODO remove after sufficient time has passed
    if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
      getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(resources.getString(R.string.pref_key__show_intro), false).commit()
    }
    getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(resources.getString(R.string.pref_key__show_intro), false).apply()
    if (!getSharedPreferences("app", Context.MODE_PRIVATE).getBoolean(resources.getString(R.string.pref_key__show_intro), false)) {
      skipStart()
      return
    }

//    addSlide(OnBoardActivity.CustomSlid)
//
    addSlide(SimpleSlide.Builder()
      .background(R.color.materialRed)
      .image(R.drawable.intro_2)
      .title(getString(R.string.minibar))
      .description(getString(R.string.intro2_text))
      .build())

    addSlide(SimpleSlide.Builder()
      .background(R.color.materialGreen)
      .image(R.drawable.intro_3)
      .title(getString(R.string.pref_title__app_drawer))
      .description(getString(R.string.intro3_text))
      .build())

    addSlide(SimpleSlide.Builder()
      .background(R.color.materialBlue)
      .image(R.drawable.intro_4)
      .title(getString(R.string.pref_title__search_bar))
      .description(getString(R.string.intro4_text))
      .build())
  }


  private fun skipStart() {
    setState()
    finish()
  }

  override fun onStop() {
    super.onStop()
    setState()
  }

  private fun setState() {
    getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(resources.getString(R.string.pref_key__show_intro), false).apply()

    val intent = Intent(this, HomeActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    startActivity(intent)
  }
}

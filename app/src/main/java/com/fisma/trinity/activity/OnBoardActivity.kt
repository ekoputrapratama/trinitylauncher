package com.fisma.trinity.activity

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.SlideFragment
import agency.tango.materialintroscreen.SlideFragmentBuilder
import agency.tango.materialintroscreen.widgets.OverScrollViewPager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fisma.trinity.R


class OnBoardActivity : MaterialIntroActivity() {

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

    val overScrollLayout = findViewById<OverScrollViewPager>(agency.tango.materialintroscreen.R.id.view_pager_slides)
    val viewPager = overScrollLayout.overScrollView
    viewPager.overScrollMode = View.OVER_SCROLL_NEVER

    addSlide(CustomSlide())

    addSlide(SlideFragmentBuilder()
      .backgroundColor(R.color.materialRed)
      .buttonsColor(R.color.introButton)
      .image(R.drawable.intro_2)
      .title(getString(R.string.minibar))
      .description(getString(R.string.intro2_text))
      .build())

    addSlide(SlideFragmentBuilder()
      .backgroundColor(R.color.materialGreen)
      .buttonsColor(R.color.introButton)
      .image(R.drawable.intro_3)
      .title(getString(R.string.pref_title__app_drawer))
      .description(getString(R.string.intro3_text))
      .build())

    addSlide(SlideFragmentBuilder()
      .backgroundColor(R.color.materialBlue)
      .buttonsColor(R.color.introButton)
      .image(R.drawable.intro_4)
      .title(getString(R.string.pref_title__search_bar))
      .description(getString(R.string.intro4_text))
      .build())
  }

  override fun onFinish() {
    super.onFinish()
    setState()
  }

  private fun skipStart() {
    setState()
    finish()
  }


  private fun setState() {
    getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(resources.getString(R.string.pref_key__show_intro), false).apply()

    val intent = Intent(this, HomeActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    startActivity(intent)
  }

  class CustomSlide : SlideFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return inflater.inflate(R.layout.view_intro, container, false)
    }

    override fun backgroundColor(): Int {
      return R.color.materialBlue
    }

    override fun buttonsColor(): Int {
      return R.color.introButton
    }
  }
}

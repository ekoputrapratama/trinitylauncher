package com.fisma.trinity.activity

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fisma.trinity.R
import com.fisma.trinity.fragment.HideAppsFragment
import com.fisma.trinity.util.AppManager
import java.util.ArrayList


class HideAppsActivity : ThemeActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_hide_apps)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
    }

    val viewPager = findViewById<ViewPager>(R.id.viewpager)
    setupViewPager(viewPager)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar.title = getString(R.string.pref_title__hide_apps)
    setSupportActionBar(toolbar)

    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    supportActionBar!!.setDisplayShowHomeEnabled(true)
  }

  override fun onDestroy() {
    AppManager.getInstance(this)!!._recreateAfterGettingApps = true
    AppManager.getInstance(this)!!.init()
    super.onDestroy()
  }

  private fun setupViewPager(viewPager: ViewPager) {
    val adapter = ViewPagerAdapter(supportFragmentManager)
    adapter.addFragment(HideAppsFragment(), "Skip")
    viewPager.adapter = adapter
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
      return mFragmentList[position]
    }

    override fun getCount(): Int {
      return mFragmentTitleList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
      return mFragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
      mFragmentList.add(fragment)
      mFragmentTitleList.add(title)
    }
  }
}

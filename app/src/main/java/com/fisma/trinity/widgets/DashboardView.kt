package com.fisma.trinity.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.cardview.widget.CardView
import com.fisma.trinity.R

class DashboardView : ListView {
  var mAdapter: DashboardAdapter? = null

  constructor(context: Context) : this(context, null) {

  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    var list = ArrayList<CardView>()

    var inflater = LayoutInflater.from(context)
    var item = inflater.inflate(R.layout.view_dashboard_shortcut, null, false) as DashboardShortcut

    list.add(item)

    adapter = DashboardAdapter(list)
    setPadding(20, 50, 20, 50)
  }


  class DashboardAdapter : BaseAdapter {
    var list: ArrayList<CardView> = ArrayList()

    constructor() {}

    constructor(l: ArrayList<CardView>) {
      list = l
    }

    override fun getCount(): Int {
      return list.size
    }

    override fun getItem(position: Int): Any {
      return list[position]
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      var view = convertView

      if (view == null) {
        view = list[position]
      } else {
        view = convertView as View
      }

      return view
    }
  }
}
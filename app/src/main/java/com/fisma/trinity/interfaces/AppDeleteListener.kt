package com.fisma.trinity.interfaces

import com.fisma.trinity.model.App

interface AppDeleteListener {
  fun onAppDeleted(apps: List<App>): Boolean
}

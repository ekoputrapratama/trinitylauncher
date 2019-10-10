package com.fisma.trinity.interfaces

import com.fisma.trinity.model.App


interface AppUpdateListener {
  fun onAppUpdated(apps: List<App>): Boolean
}

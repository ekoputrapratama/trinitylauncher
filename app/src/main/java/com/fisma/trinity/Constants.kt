package com.fisma.trinity

class Constants {

  // DO NOT REARRANGE
  // enum ordinal used for db
  enum class ItemPosition {
    Dock,
    Desktop
  }

  enum class ItemState {
    Hidden,
    Visible
  }

  enum class WallpaperScroll {
    Normal,
    Inverse,
    Off
  }

  enum class AppCategory {
    PHONE, EMAIL, MESSAGING, CONTACTS,
    GALLERY, BROWSER, MARKET
  }

  companion object {
    val BUFFER_SIZE = 2048
    val INTENT_BACKUP = 5
    val INTENT_RESTORE = 3
    val ACTION_LAUNCHER = 8

    // separates a list of integers
    val DELIMITER = "#"
  }
}
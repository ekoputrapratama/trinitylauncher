package com.fisma.trinity.compat

import android.graphics.drawable.Drawable
import android.os.Process.myUserHandle


open class UserManagerCompatV16 internal constructor() : UserManagerCompat() {

  override val userProfiles: List<UserHandleCompat>
    get() {
      val profiles = ArrayList<UserHandleCompat>(1)
      profiles.add(UserHandleCompat.myUserHandle())
      return profiles
    }

  override fun getUserForSerialNumber(serialNumber: Long): UserHandleCompat? {
    return UserHandleCompat.myUserHandle()
  }

  override fun getBadgedDrawableForUser(unbadged: Drawable,
                                        user: UserHandleCompat): Drawable {
    return unbadged
  }

  override fun getSerialNumberForUser(user: UserHandleCompat): Long {
    return 0
  }

  override fun getBadgedLabelForUser(label: CharSequence, user: UserHandleCompat): CharSequence {
    return label
  }
}
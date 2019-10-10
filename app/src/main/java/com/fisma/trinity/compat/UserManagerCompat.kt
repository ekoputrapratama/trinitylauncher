package com.fisma.trinity.compat

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build


abstract class UserManagerCompat protected constructor() {

  abstract val userProfiles: List<UserHandleCompat>
  abstract fun getSerialNumberForUser(user: UserHandleCompat): Long
  abstract fun getUserForSerialNumber(serialNumber: Long): UserHandleCompat?
  abstract fun getBadgedDrawableForUser(unbadged: Drawable, user: UserHandleCompat): Drawable
  abstract fun getBadgedLabelForUser(label: CharSequence, user: UserHandleCompat): CharSequence

  companion object {

    fun getInstance(context: Context): UserManagerCompat {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        UserManagerCompatVL(context)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        UserManagerCompatV17(context)
      } else {
        UserManagerCompatV16()
      }
    }
  }
}
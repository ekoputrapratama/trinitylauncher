package com.fisma.trinity.compat

import android.content.Context
import android.os.UserManager


open class UserManagerCompatV17(context: Context) : UserManagerCompatV16() {
  protected var mUserManager: UserManager

  init {
    mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
  }

  override fun getSerialNumberForUser(user: UserHandleCompat): Long {
    return mUserManager.getSerialNumberForUser(user.getUser())
  }

  override fun getUserForSerialNumber(serialNumber: Long): UserHandleCompat? {
    return UserHandleCompat.fromUser(mUserManager.getUserForSerialNumber(serialNumber))
  }
}

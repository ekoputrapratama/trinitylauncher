package com.fisma.trinity.compat

import android.content.Intent
import android.os.UserHandle
import android.os.Build


class UserHandleCompat {
  internal var user: UserHandle? = null

  private constructor(user: UserHandle) {
    this.user = user
  }

  private constructor() {}

  fun getUser(): UserHandle? {
    return user
  }

  override fun toString(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      user!!.toString()
    } else {
      ""
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is UserHandleCompat && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || user == other.user)
  }

  override fun hashCode(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      user!!.hashCode()
    } else {
      0
    }
  }

  /**
   * Adds [UserHandle] to the intent in for L or above.
   * Pre-L the launcher doesn't support showing apps for multiple
   * profiles so this is a no-op.
   */
  fun addToIntent(intent: Intent, name: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && user != null) {
      intent.putExtra(name, user)
    }
  }

  companion object {

    fun myUserHandle(): UserHandleCompat {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        UserHandleCompat(android.os.Process.myUserHandle())
      } else {
        UserHandleCompat()
      }
    }

    internal fun fromUser(user: UserHandle?): UserHandleCompat? {
      return if (user == null) {
        null
      } else {
        UserHandleCompat(user)
      }
    }
  }
}
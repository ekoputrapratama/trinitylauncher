package com.fisma.trinity.compat

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.content.pm.PackageManager
import android.os.Build
import java.util.*
import kotlin.collections.ArrayList

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class UserManagerCompatVL(context: Context) : UserManagerCompatV17(context) {
  private val mPm: PackageManager

  override val userProfiles: List<UserHandleCompat>
    get() {
      val users = mUserManager.getUserProfiles() ?: return emptyList()
      val compatUsers = ArrayList<UserHandleCompat>(
        users.size)
      for (user in users) {
        compatUsers.add(UserHandleCompat.fromUser(user)!!)
      }
      return compatUsers
    }

  init {
    mPm = context.getPackageManager()
  }

  override fun getBadgedDrawableForUser(unbadged: Drawable, user: UserHandleCompat): Drawable {
    return mPm.getUserBadgedIcon(unbadged, user.getUser())
  }

  override fun getBadgedLabelForUser(label: CharSequence, user: UserHandleCompat): CharSequence {
    return if (user == null) {
      label
    } else mPm.getUserBadgedLabel(label, user.getUser())
  }
}

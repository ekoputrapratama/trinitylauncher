package com.fisma.trinity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.util.HashSet


class NetworkStateReceiver : BroadcastReceiver() {
  var listeners: MutableSet<NetworkStateReceiverListener> = HashSet()
  var connected: Boolean = false

  companion object {
    var mInstance: NetworkStateReceiver? = null

    fun isConnected(): Boolean {
      val instance = mInstance ?: return false

      return instance.connected
    }
  }

  init {
    mInstance = this
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null || intent.extras == null)
      return

    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val ni = manager.activeNetworkInfo

    if (ni != null && ni.state == NetworkInfo.State.CONNECTED) {
      connected = true
    } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, java.lang.Boolean.FALSE)) {
      connected = false
    }

    notifyStateToAll()
  }

  private fun notifyStateToAll() {
    for (listener in listeners)
      notifyState(listener)
  }

  private fun notifyState(listener: NetworkStateReceiverListener?) {
    if (listener == null)
      return

    if (connected)
      listener.networkAvailable()
    else
      listener.networkUnavailable()
  }

  fun addListener(l: NetworkStateReceiverListener) {
    listeners.add(l)
    notifyState(l)
  }

  fun removeListener(l: NetworkStateReceiverListener) {
    listeners.remove(l)
  }

  interface NetworkStateReceiverListener {
    fun networkAvailable()

    fun networkUnavailable()
  }
}

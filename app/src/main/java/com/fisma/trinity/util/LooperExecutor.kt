package com.fisma.trinity.util


import android.os.Handler
import android.os.Looper
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/**
 * Extension of [AbstractExecutorService] which executed on a provided looper.
 */
open class LooperExecutor(looper: Looper) : AbstractExecutorService() {

  val handler: Handler

  init {
    handler = Handler(looper)
  }

  override fun execute(runnable: Runnable) {
    if (handler.looper == Looper.myLooper()) {
      runnable.run()
    } else {
      handler.post(runnable)
    }
  }

  /**
   * Not supported and throws an exception when used.
   */
  @Deprecated("")
  override fun shutdown() {
    throw UnsupportedOperationException()
  }

  /**
   * Not supported and throws an exception when used.
   */
  @Deprecated("")
  override fun shutdownNow(): List<Runnable> {
    throw UnsupportedOperationException()
  }

  override fun isShutdown(): Boolean {
    return false
  }

  override fun isTerminated(): Boolean {
    return false
  }

  /**
   * Not supported and throws an exception when used.
   */
  @Deprecated("")
  @Throws(InterruptedException::class)
  override fun awaitTermination(l: Long, timeUnit: TimeUnit): Boolean {
    throw UnsupportedOperationException()
  }
}
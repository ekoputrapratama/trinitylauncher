package com.fisma.trinity.util

import android.os.Handler
import android.os.Looper
import com.fisma.trinity.MainThreadExecutor
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException


val mainHandler by lazy { Handler(Looper.getMainLooper()) }
//val uiWorkerHandler by lazy { Handler(LauncherModel.getUiWorkerLooper()) }
//val iconPackUiHandler by lazy { Handler(LauncherModel.getIconPackUiLooper()) }

//fun runOnUiWorkerThread(r: () -> Unit) {
//  runOnThread(uiWorkerHandler, r)
//}

fun runOnMainThread(r: () -> Unit) {
  runOnThread(mainHandler, r)
}

fun runOnThread(handler: Handler, r: () -> Unit) {
  if (handler.looper.thread.id == Looper.myLooper()?.thread?.id) {
    r()
  } else {
    handler.post(r)
  }
}

fun <T, A> ensureOnMainThread(creator: (A) -> T): (A) -> T {
  return { it ->
    if (Looper.myLooper() == Looper.getMainLooper()) {
      creator(it)
    } else {
      try {
        MainThreadExecutor().submit(Callable { creator(it) }).get()
      } catch (e: InterruptedException) {
        throw RuntimeException(e)
      } catch (e: ExecutionException) {
        throw RuntimeException(e)
      }

    }
  }
}
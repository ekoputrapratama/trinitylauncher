package com.fisma.trinity

import android.os.Looper
import com.fisma.trinity.util.LooperExecutor

/**
 * An executor service that executes its tasks on the main thread.
 *
 * Shutting down this executor is not supported.
 */
class MainThreadExecutor : LooperExecutor(Looper.getMainLooper())
package com.trinity.plugin.sample.util

/**
 * Generic map, useful for keeping a map of lambdas that are lazily converted to Runnables.
 */
class LazyFunctionMap<K, V>(val method: (key: K) -> V) {
  val map = hashMapOf<K, Runnable>()
  operator fun get(key: K): Runnable {
    var value = map[key]
    if (value == null) {
      value = Runnable {
        method(key)
      }
      map[key] = value
    }
    return value
  }
}
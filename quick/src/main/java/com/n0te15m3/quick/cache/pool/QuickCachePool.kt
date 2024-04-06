package com.n0te15m3.quick.cache.pool


interface QuickCachePool<T> {
    operator fun get(key: String): T?
    fun put(key: String, value: T): Pair<String, T?>?
    fun has(key: String): Boolean
}
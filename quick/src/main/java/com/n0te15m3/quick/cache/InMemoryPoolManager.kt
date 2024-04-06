package com.n0te15m3.quick.cache

import com.n0te15m3.quick.cache.InMemoryCacheStrategy
import com.n0te15m3.quick.cache.pool.QuickCachePool


class InMemoryPoolManager(cacheStrategy: InMemoryCacheStrategy = InMemoryCacheStrategy()) {
    private var pool: QuickCachePool<ByteArray>

    init {
        pool = cacheStrategy.getPool()
    }

    fun getData(key: String): ByteArray? = pool[key]
    fun putData(key: String, data: ByteArray) {
        pool.put(key, data)
    }

    fun has(key: String): Boolean = pool.has(key)

    /**
     * Change strategy for cache
     * Notice that this operation will clear the cache pool
     */
    fun changeCacheStrategy(cacheStrategy: InMemoryCacheStrategy) {
        pool = cacheStrategy.getPool()
    }
}
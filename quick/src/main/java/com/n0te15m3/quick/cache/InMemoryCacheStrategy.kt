package com.n0te15m3.quick.cache

import com.n0te15m3.quick.cache.pool.LFUPool
import com.n0te15m3.quick.cache.pool.LRUPool
import com.n0te15m3.quick.cache.pool.QuickCachePool

data class InMemoryCacheStrategy(
    val strategy: CacheStrategy = CacheStrategy.LRU_CACHE, val cacheCapacity: Int = DEFAULT_CAPACITY
) {
    companion object {
        const val DEFAULT_CAPACITY = 1000
    }

    fun getPool(): QuickCachePool<ByteArray> {
        return when (strategy) {
            CacheStrategy.LFU_CACHE -> LFUPool(cacheCapacity)
            CacheStrategy.LRU_CACHE -> LRUPool(cacheCapacity)
        }
    }
}

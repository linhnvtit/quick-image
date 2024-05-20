package com.n0te15m3.quick

import android.content.Context
import com.n0te15m3.quick.cache.CacheStrategy
import com.n0te15m3.quick.cache.InMemoryCacheStrategy
import com.n0te15m3.quick.utils.QuickLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map


internal class Quick private constructor() {
    companion object {
        @Volatile
        private lateinit var quick: Quick

        fun get(): Quick {
            if (Companion::quick.isInitialized) return quick
            else throw Exception("Quick has not initialized")
        }

        @Synchronized
        fun get(context: Context): Quick {
            if (Companion::quick.isInitialized) return quick
            return getNew(
                context, InMemoryCacheStrategy(
                    CacheStrategy.LRU_CACHE, InMemoryCacheStrategy.DEFAULT_CAPACITY
                )
            )
        }

        @Synchronized
        fun getNew(
            context: Context, inMemoryCacheStrategy: InMemoryCacheStrategy
        ): Quick {
            return Quick(
                context = context.applicationContext, inMemoryCacheStrategy = inMemoryCacheStrategy
            ).also {
                quick = it
            }
        }
    }

    constructor(
        context: Context, inMemoryCacheStrategy: InMemoryCacheStrategy
    ) : this() {
        requestManager = RequestManager(
            context = context, shareSource = shareSource, scope = scope, inMemoryCacheStrategy = inMemoryCacheStrategy
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val shareSource = MutableSharedFlow<Pair<String, FetchingState>>()

    private lateinit var requestManager: RequestManager

    fun enableLog() {
        QuickLog.enabled.set(true)
    }

    fun disableLog() {
        QuickLog.enabled.set(false)
    }

    fun getFilteredSource(url: String): Flow<FetchingState> {
        return shareSource.filter { it.first == url }.map { it.second }
    }

    suspend fun fetchImage(url: String) {
        requestManager.request(url)
    }
}
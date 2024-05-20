package com.n0te15m3.quick

import android.content.Context
import com.n0te15m3.quick.cache.ImageDownloader
import com.n0te15m3.quick.cache.InMemoryCacheStrategy
import com.n0te15m3.quick.cache.InMemoryPoolManager
import com.n0te15m3.quick.disk_manager.DiskManager
import com.n0te15m3.quick.disk_manager.StorageHelper
import com.n0te15m3.quick.utils.QuickLog
import com.n0te15m3.quick.utils.isValidUrl
import com.n0te15m3.quick.utils.sha256
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RequestManager(
    private val context: Context,
    private val shareSource: MutableSharedFlow<Pair<String, FetchingState>>,
    private val scope: CoroutineScope,
    inMemoryCacheStrategy: InMemoryCacheStrategy = InMemoryCacheStrategy()
) {
    private var downloader: ImageDownloader
    private var inMemoryManager: InMemoryPoolManager = InMemoryPoolManager()
    private var diskManager: DiskManager

    private val channel = Channel<Pair<String, LoadingState>>()

    private val requestMutex = Mutex()

    private val fetchingSet: HashSet<String> = hashSetOf()

    init {
        inMemoryManager.changeCacheStrategy(inMemoryCacheStrategy)
        downloader = ImageDownloader(channel)
        diskManager = DiskManager(StorageHelper(), context)

        scope.launch {
            while (true) {
                val data = channel.receive()

                when (data.second) {
                    is LoadingState.Success -> handleLoadingSuccess(data)
                    is LoadingState.Loading -> handleLoading(data)
                    is LoadingState.Failure -> handleLoadingFail(data)
                }
            }
        }
    }

    suspend fun request(url: String) {
        if (url in fetchingSet) return
        requestMutex.withLock {
            when (url) {
                !in fetchingSet -> {
                    fetchingSet.add(url)
                }

                else -> {
                    return
                }
            }
        }.let {
            scope.launch {
                when {
                    url.isValidUrl() -> {
                        var data: ByteArray? = inMemoryManager.getData(url)
                        var dataSource = DataSource.IN_MEMORY

                        if (data == null) {
                            getDataFromDisk(url)?.let {
                                dataSource = DataSource.DIR_CACHE
                                data = it.first
                                if (it.second) {
                                    downloader.fetchImage(url = url, inSilentMode = true)
                                }
                            }
                        }

                        if (data != null) {
                            channel.send(url to LoadingState.Success(data!!, dataSource))
                        } else {
                            downloader.fetchImage(url)
                        }
                    }

                    else -> {
                        channel.send(url to LoadingState.Failure("Invalid url"))
                    }
                }
            }
        }
    }

    private suspend fun handleLoadingSuccess(data: Pair<String, LoadingState>) {
        try {
            (data.second as LoadingState.Success).let {
                saveDataToCache(data.first, it.data, it.source == DataSource.REMOTE)
                sendData(data.first to FetchingState.from(it))

                requestMutex.withLock {
                    fetchingSet.remove(data.first)
                }
            }
        } catch (e: Exception) {
            // If data in cache is tempered, then delete it and fetch from remote source
            if ((data.second as LoadingState.Success).source != DataSource.DIR_CACHE) {
                QuickLog.d("Cache data invalid. Fetching source from remove")
                diskManager.deleteCacheFile(context, sha256(data.first))
                downloader.fetchImage(data.first)
            } else {
                handleLoadingFail(data.first to LoadingState.Failure("${e.message}"))
            }
        }
    }

    private suspend fun handleLoading(data: Pair<String, LoadingState>) {
        sendData(data.first to FetchingState.from(data.second))
    }

    private suspend fun handleLoadingFail(data: Pair<String, LoadingState>) {
        sendData(data.first to FetchingState.from(data.second))

        requestMutex.withLock {
            fetchingSet.remove(data.first)
        }
    }

    private suspend fun sendData(data: Pair<String, FetchingState>) {
        shareSource.emit(data)
    }

    private fun saveDataToCache(url: String, data: ByteArray, shouldSaveToDisk: Boolean) = scope.launch(Dispatchers.IO) {
        // Save it to memory
        inMemoryManager.putData(url, data)

        // Also save it to cache file
        if (shouldSaveToDisk) {
            val hashedUrl = sha256(url)
            diskManager.createCacheFile(context, hashedUrl, data)
        }
    }

    private fun getDataFromDisk(url: String): Pair<ByteArray, Boolean>? {
        val hashedUrl = sha256(url)
        return diskManager.loadCacheFile(context, hashedUrl)
    }
}